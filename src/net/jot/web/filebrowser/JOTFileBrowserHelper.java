/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.web.filebrowser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


import net.jot.logger.JOTLogger;
import net.jot.utils.JOTConstants;
import net.jot.utils.JOTUtilities;
import net.jot.web.JOTFlowRequest;
import net.jot.web.ctrl.JOTController;
import net.jot.web.multipart.JOTMultiPartItem;

/**
 * "Logic" /processing methods of the FileManager, using a user's current FileBrowserSession
 * @author thibautc
 *
 */
public class JOTFileBrowserHelper
{

    public static final String ACTION_BROWSE_TO = "BROWSE_TO";
    public static final String ACTION_RENAME = "RENAME";
    public static final String ACTION_CREATE_FOLDER = "CREATE_FOLDER";
    public static final String ACTION_UPLOAD = "UPLOAD";
    public static final String ACTION_SORT_BY = "SORT_BY";
    public static final String ACTION_DOWNLOAD = "DOWNLOAD";
    // could be multiple ?
    public static final String ACTION_SELECT = "SELECT";
    public static final String ACTION_DELETE = "DELETE";

    // the user can browse / upload etc... but nothing needs to be returned.
    public static final int TYPE_BROWSE = 1;
    // we want  (force)1 file to be chosen (returned)
    public static final int TYPE_CHOOSE_1_FILE = 2;
    // we want  (force)1 or more file(s) to be chosen (returned)
    public static final int TYPE_CHOOSE_1PLUS_FILE = 3;
    // we want (force) 1 folder to be chosen (returned), you might want to set allowPickRootFolder accordingly
    public static final int TYPE_CHOOSE_1_FOLDER = 4;
    // we want (force) 1 file to be uploaded and returned, note this defaults nbUploadFields=1 and listFiles=false
    public static final int TYPE_UPLOAD_1_FILE = 5;

    /**
     * Update the filebrowser view (ie: files listings etc...)
     * @param request
     * @param response
     * @return
     */
    public static String updateFileManagerView(JOTFlowRequest request, HttpServletResponse response)
    {
        HttpSession session = request.getSession();
        JOTFileBrowserSession fbSession = getFbSession(request);

        // clear messages
        fbSession.currentWarning = null;

        boolean justReset = request.getParameter(JOTFileBrowserController.JUST_RESET) != null;
        // if this is a file upload, it will be multipart, so trying to parse that (only if file uploads enabled)
        if (fbSession.isAllowUploadFile() && !justReset)
        {
            try
            {
                request.parseMultiPartContent(fbSession.tempUploadFolder, fbSession.maxUploadSize);
            } catch (Exception e)
            {
                fbSession.setCurrentWarning(e.getMessage());
                JOTLogger.logException(JOTLogger.ERROR_LEVEL, JOTFileBrowserHelper.class, "Failed parsing FileManager request.", e);
            }
        }

        String action = request.getParameter(JOTConstants.SESSION_FB_ACTION);
        String value = request.getParameter(JOTConstants.SESSION_FB_VALUE);



        if (action != null)
        {
            if (action.equals(ACTION_BROWSE_TO))
            {
                browseTo(fbSession, value);
            } else if (action.equals(ACTION_CREATE_FOLDER))
            {
                createFolder(fbSession, value);
            } else if (action.equals(ACTION_SORT_BY))
            {
                orderBy(fbSession, value);
            } else if (action.equals(ACTION_RENAME))
            {
                rename(fbSession, request, value);
            } else if (action.equals(ACTION_DELETE))
            {
                delete(fbSession, request, value);
            } else if (action.equals(ACTION_DOWNLOAD))
            {
                download(fbSession, value, request, response);
            } else if (action.equals(ACTION_SELECT))
            {
                String result = select(fbSession, request, value);
                //if complete return "Completed" ... done.
                if (result != null && result.equals(JOTController.RESULT_COMPLETED))
                {
                    //TODO: should we get rid of fbSession in session ??
                    return result;
                }
            } else if (action.equals(ACTION_UPLOAD))
            {
                upload(fbSession, request);
            }
        }

        // update view
        updateFolderListing(fbSession);

        // sort the files
        Vector v = fbSession.getFileListing();
        File[] files = (File[]) v.toArray(new File[0]);
        files = JOTUtilities.sortFolderListing(files, fbSession.getSortBy());
        Collection coll = Arrays.asList(files);
        v = new Vector(coll);
        fbSession.setFileListing(v);

        session.setAttribute(JOTConstants.SESSION_FILE_BROWSER, fbSession);

        return fbSession.currentWarning == null ? JOTController.RESULT_SUCCESS : JOTController.RESULT_FAILURE;
    }

    /**
     * Handles the user file(s) selection
     * @param fbSession
     * @param request
     * @param value
     * @return
     */
    private static String select(JOTFileBrowserSession fbSession, JOTFlowRequest request, String value)
    {
        boolean oneFile = false;
        File[] files = new File[0];
        switch (fbSession.getBrowseType())
        {
            case TYPE_BROWSE:
                // nothing to validate
                break;
            case TYPE_CHOOSE_1_FILE:
                oneFile = true;
            //no break, bundling together with 1+files
            case TYPE_CHOOSE_1PLUS_FILE:
                files = getSelectedFiles(fbSession, request, 2);
                if (oneFile)
                {
                    if (files.length != 1)
                    {
                        fbSession.setCurrentWarning(fbSession.getOneFileWarning());
                        return null;
                    }
                } else
                {
                    if (files.length < 1)
                    {
                        fbSession.setCurrentWarning(fbSession.getMultipleFilesWarning());
                        return null;
                    }
                }
                break;
            case TYPE_CHOOSE_1_FOLDER:
                files = getSelectedFiles(fbSession, request, 3);
                if (files.length != 1)
                {
                    fbSession.setCurrentWarning(fbSession.getOneFolderWarning());
                    return null;
                }
                break;
            case TYPE_UPLOAD_1_FILE:
                // upload the file first
                File[] uploadedFiles = upload(fbSession, request);
                if (uploadedFiles == null || uploadedFiles.length != 1)
                {
                    fbSession.setCurrentWarning("Please upload only 1 file.");
                    return null;
                }
                files = uploadedFiles;
                break;
        }
        // seem like we got expected results;
        request.setAttribute(JOTFileBrowserController.RESULTS_ATTRIBUTE, files);
        return JOTController.RESULT_COMPLETED;
    }

    /**
     * After completion of the file selection process,
     * this can be called to retrieve the list of files
     * picked by the user.
     * @return
     */
    public static File[] getChosenFiles(HttpServletRequest request)
    {
        File[] results = (File[]) request.getAttribute(JOTFileBrowserController.RESULTS_ATTRIBUTE);
        return results;
    }

    /**
     * Internal method finding the selected file(s)
     * @param fbSession
     * @param request
     * @param type
     * @return
     */
    private static File[] getSelectedFiles(JOTFileBrowserSession fbSession, JOTFlowRequest request, int type)
    {
        // type=1:any files/folers
        // type=2: any files
        // type=3: any folders
        Vector v = new Vector();
        Vector fileListing = fbSession.getFileListing();
        int nbFiles = fileListing.size();
        for (int i = 0; i <= nbFiles; i++)
        {
            boolean checkboxChecked = request.getParameter(JOTConstants.SESSION_FILE_SELECT + "_" + i) != null && request.getParameter(JOTConstants.SESSION_FILE_SELECT + "_" + i).equalsIgnoreCase("on");

            if (checkboxChecked)
            {
                File f = (File) fileListing.get(i);
                if (type == 1 || (type == 2 && f.isFile()) || (type == 3 && f.isDirectory()))
                {
                    v.add(f);
                }
            }
        }
        return (File[]) v.toArray(new File[0]);
    }

    /**
     * Handle a file download.
     * @param fbSession
     * @param value
     * @param request
     * @param response
     */
    private static void download(JOTFileBrowserSession fbSession, String value, HttpServletRequest request, HttpServletResponse response)
    {
        if (!fbSession.isAllowDownloadFile())
        {
            fbSession.currentWarning = buildWarning(fbSession.forbiddenWarning, "");
            return;
        }
        int val = new Integer(value).intValue();
        File file = (File) fbSession.getFileListing().get(val);
        if (file.isFile())
        {
            sendFile(file, request, response);
        }
    }

    /**
     * Sends the file as an attachment to the request
     * @param file
     * @param request
     * @param response
     */
    private static void sendFile(File file, HttpServletRequest request, HttpServletResponse response)
    {
        try
        {
            OutputStream os = response.getOutputStream();
            FileInputStream fis = new FileInputStream(file);
            long dataLength = file.length();
            response.setContentLength((int) dataLength);
            response.addHeader("Content-Disposition", "attachment; filename=" + file.getName());
            long bytesRead = 0;
            byte[] buffer = new byte[30000];
            long bytesLeft;
            do
            {
                bytesLeft = dataLength - bytesRead;
                long bytesToRead = bytesLeft > buffer.length ? buffer.length : bytesLeft;
                fis.read(buffer, 0, (int) bytesToRead);
                bytesRead += bytesToRead;
                os.write(buffer, 0, (int) bytesToRead);
                os.flush();
            } while (bytesLeft > 0);
            fis.close();
            response.flushBuffer();
        } catch (Exception e)
        {
            try
            {
                response.getOutputStream().write("Reading the file failed!".getBytes());
                response.flushBuffer();
            } catch (Exception e2)
            {
            }
        }
    }

    /**
     * Orders the file listings
     * @param fbSession
     * @param value
     */
    private static void orderBy(JOTFileBrowserSession fbSession, String value)
    {
        int sort = fbSession.getSortBy();
        if (value.equalsIgnoreCase("name"))
        {
            if (sort == JOTUtilities.SORT_BY_NAME_ASC || sort == JOTUtilities.SORT_BY_NAME_DESC)
            {
                sort = 0 - sort;
            } else
            //default: alphabetical order
            {
                sort = JOTUtilities.SORT_BY_NAME_ASC;
            }
        } else if (value.equalsIgnoreCase("size"))
        {
            if (sort == JOTUtilities.SORT_BY_SIZE_ASC || sort == JOTUtilities.SORT_BY_SIZE_DESC)
            {
                sort = 0 - sort;
            } else
            //default: newer first
            {
                sort = JOTUtilities.SORT_BY_SIZE_DESC;
            }
        } else if (value.equalsIgnoreCase("timestamp"))
        {
            if (sort == JOTUtilities.SORT_BY_TSTAMP_ASC || sort == JOTUtilities.SORT_BY_TSTAMP_DESC)
            {
                sort = 0 - sort;
            } else
            // default: most recent first
            {
                sort = JOTUtilities.SORT_BY_TSTAMP_DESC;
            }
        }
        fbSession.setSortBy(sort);
    }

    /**
     * Returns the fbSession object (The current user "view" in the file manager)
     * @param request
     * @return
     */
    public static JOTFileBrowserSession getFbSession(JOTFlowRequest request)
    {
        HttpSession session = request.getSession();
        JOTFileBrowserSession fbSession = (JOTFileBrowserSession) session.getAttribute(JOTConstants.SESSION_FILE_BROWSER);
        return fbSession;
    }

    /**
     * Uploads the file locally and return array of "files"
     * @param fbSession
     * @param request
     * @return
     */
    private static File[] upload(JOTFileBrowserSession fbSession, JOTFlowRequest request)
    {
        Vector uploadedFiles = new Vector();
        if (fbSession.isAllowUploadFile() == false)
        {
            fbSession.currentWarning = fbSession.forbiddenWarning;
            return null;
        }
        int maxExpectedFiles = fbSession.getNbOfUploadFields().intValue();
        for (int i = 1; i <= maxExpectedFiles; i++)
        {
            JOTMultiPartItem item = request.getFile("file" + i);
            if (item != null)
            {
                String fileName = item.getFileName();

                if (item.getFileName().trim().length() > 0)
                {
                    if (!Pattern.matches(fbSession.getNewFilePattern(), fileName))
                    {
                        fbSession.currentWarning = fbSession.fileNameWarning;
                        return null;
                    }
                    File f = new File(fbSession.getCurrentFolder(), fileName);
                    if (f.exists())
                    {
                        boolean checkboxChecked = request.getParameter(JOTConstants.SESSION_FORCE_UPDATE) != null && request.getParameter(JOTConstants.SESSION_FORCE_UPDATE).equalsIgnoreCase("on");
                        if (!fbSession.isAllowUpdateFile())
                        {
                            fbSession.currentWarning = buildWarning(fbSession.failedWarning, "Not allowed to replace: " + fileName);
                            return null;
                        }
                        if (!checkboxChecked)
                        {
                            fbSession.currentWarning = buildWarning(fbSession.updateWarning, fileName);
                            return null;
                        }
                    }
                    FileOutputStream fos = null;
                    try
                    {
                        fos = new FileOutputStream(f);
                        item.copyDataTo(fos);
                        fos.close();
                        // saved successfully
                        uploadedFiles.add(f);
                    } catch (Exception e)
                    {
                        fbSession.currentWarning = buildWarning(fbSession.failedWarning, e.toString());
                        return null;
                    } finally
                    {
                        if (fos != null)
                        {
                            try
                            {
                                fos.close();
                            } catch (Exception e2)
                            {
                            }
                        }
                    }
                }
            }
        }
        return (File[]) uploadedFiles.toArray(new File[0]);
    }

    /**
     * User warnings if they picked different things than requested.
     * @param warning
     * @param var
     * @return
     */
    private static String buildWarning(String warning, String var)
    {
        return warning.replaceFirst("\\$1", var);
    }

    /**
     * Handles deleting a file
     * @param fbSession
     * @param request
     * @param value
     */
    private static void delete(JOTFileBrowserSession fbSession, JOTFlowRequest request, String value)
    {
        if (fbSession.isAllowDelete() == false)
        {
            fbSession.setCurrentWarning(fbSession.getForbiddenWarning());
            return;
        }
        int val = new Integer(value).intValue();
        File file = (File) fbSession.fileListing.get(val);
        boolean checkboxChecked = request.getParameter(JOTConstants.SESSION_FORCE_DELETE) != null && request.getParameter(JOTConstants.SESSION_FORCE_DELETE).equalsIgnoreCase("on");
        if (isFilledFolder(file) && (!fbSession.isAllowDeleteFilledFolders() || !checkboxChecked))
        {
            fbSession.setCurrentWarning(buildWarning(fbSession.getFullFolderWarning(), file.getName()));
            return;
        }

        if (isFilledFolder(file) && fbSession.isAllowDeleteFilledFolders())
        {
            JOTUtilities.deleteFolderContent(file);
        }
        file.delete();
    }

    /**
     * wether the folder contains anything or not.
     * @param file
     * @return
     */
    private static boolean isFilledFolder(File file)
    {
        return file.isDirectory() && file.listFiles().length > 0;
    }

    /**
     * Handles renaming a file/folder
     * @param fbSession
     * @param request
     * @param value
     */
    private static void rename(JOTFileBrowserSession fbSession, JOTFlowRequest request, String value)
    {
        if (fbSession.allowRenaming == false)
        {
            fbSession.currentWarning = fbSession.forbiddenWarning;
            return;
        }
        // check the pattern so that user cannot try to create thinhs like ../../something or unicode dir traversal etc...
        int val = new Integer(value).intValue();

        String newName = request.getParameter("newname");

        if (newName != null && newName.length() < fbSession.maxFolderNameLength && Pattern.matches(fbSession.getNewFilePattern(), newName))
        {
            File oldFile = (File) fbSession.fileListing.get(val);
            File newFile = new File(oldFile.getParentFile(), newName);
            oldFile.renameTo(newFile);
        } else
        {
            fbSession.currentWarning = fbSession.fileNameWarning;
        }
    }

    /**
     * handles creating a folder
     * @param fbSession
     * @param value
     */
    private static void createFolder(JOTFileBrowserSession fbSession, String value)
    {
        if (fbSession.allowCreateFolders == false)
        {
            fbSession.currentWarning = fbSession.forbiddenWarning;
            return;
        }
        // check the pattern so that user cannot try to create thinhs like ../../something or unicode dir traversal etc...
        if (value.length() < fbSession.maxFolderNameLength && Pattern.matches(fbSession.getNewDirPattern(), value))
        {
            File newFolder = new File(fbSession.currentFolder, value);
            newFolder.mkdir();
        } else
        {
            fbSession.currentWarning = fbSession.fileNameWarning;
        }
    }

    /**
     * Handles browsing / moving to a different directory
     * @param fbSession
     * @param value
     */
    private static void browseTo(JOTFileBrowserSession fbSession, String value)
    {
        if (fbSession.allowBrowsing == false)
        {
            fbSession.currentWarning = fbSession.forbiddenWarning;
            return;
        }
        if (value != null)
        {
            File newFolder;
            if (value.equalsIgnoreCase("up"))
            {
                newFolder = fbSession.getUpFolder();
            } else
            {
                int val = new Integer(value).intValue();
                newFolder = (File) fbSession.getFileListing().get(val);
            }
            // check once more, just to make sure :-)

            if (newFolder.isDirectory() && JOTUtilities.isWithinFolder(newFolder, fbSession.rootFolder))
            {
                fbSession.currentFolder = newFolder;
                // success: leave now
                return;
            }
        }
        // if we get here something failed.
        fbSession.currentWarning = buildWarning(fbSession.failedWarning, "");
    }

    /**
     * File names are mapped to numbers.
     * This way the user can only access/browse to/rename file by numbers
     * This is much safer than having the user send the file name, as that can easily be treaked and
     * Enable directory traversal hacks and stuff like that.
     * @param fbSession
     */
    private static void updateFolderListing(JOTFileBrowserSession fbSession)
    {
        Vector newListing = new Vector();
        File folder = fbSession.currentFolder;
        File[] files = folder.listFiles();
        File parent = fbSession.currentFolder.getParentFile();
        fbSession.upFolder = null;
        if (fbSession.allowListFiles)
        {
            if (fbSession.allowBrowsing && (JOTUtilities.isWithinFolder(parent, fbSession.rootFolder) || fbSession.rootFolder.equals(parent)))
            {
                fbSession.upFolder = parent;
            }
            for (int i = 0; i != files.length; i++)
            {
                File file = files[i];
                if (!file.isHidden() || fbSession.isAllowListHiddenFiles())
                {
                    newListing.add(file);
                }
            }
        }
        fbSession.setFileListing(newListing);
    }

    /**
     * sets the updated FbSession (user current view)
     * @param request
     * @param fbSession
     */
    public static void setFbSession(JOTFlowRequest request, JOTFileBrowserSession fbSession)
    {
        HttpSession session = request.getSession();
        session.setAttribute(JOTConstants.SESSION_FILE_BROWSER, fbSession);
    }
}
