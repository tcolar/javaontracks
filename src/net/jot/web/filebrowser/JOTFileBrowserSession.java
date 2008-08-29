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
import java.util.Vector;

import net.jot.utils.JOTUtilities;

/**
 * This reprsents a user current "view" of a file manager. (ie: which folder he is in, how the file listing is ordered etc...)
 * It stores that "filemanager view" data into the user session.
 * This "view" is originally configured by the programmer to allow certain actions and expect certain results.
 * It is a lightweight object, just varibales and get/setters.
 * Most of the "Logic" going with this is in JotFileBrowserHelper
 * @author thibautc
 *
 */
public class JOTFileBrowserSession
{
	//TODO: image password verification t protect from robots ??
	
	/** user can not browse "higher" than this folder.*/
	protected File rootFolder=null;
	/** which folder to start in (rootFolder if null)*/
	protected File startFolder=null;
	/** You will want to set this depending what you want the user to do: ie: upload a file, select a file, just browse etc....*/
	protected int browseType=JOTFileBrowserHelper.TYPE_BROWSE;
	/** if you want to set a title (ie: "Please choose a file")*/
	protected String title=null;
	/** where the temporary upload data will be stored during a file upload.*/
	protected String tempUploadFolder=JOTUtilities.getStandardTmpDir().getAbsolutePath();
	
	// flags
	/** If false then user won't se the existing files at all.*/
	protected boolean allowListFiles=true;
	/** wether stuck in exisiting folder or can browse around.*/
	protected boolean allowBrowsing=true;
	/** wether to show the current directory full path or not (ex: /home/blah/files/)**/
	protected boolean allowShowPath=true;
	protected boolean allowListHiddenFiles=false;
	protected boolean allowCreateFolders=false;
	protected boolean allowDelete=false;
        /** allow renaming files/folders */
	protected boolean allowRenaming=false;
        /** allow deleting recursively a filled folder or not */
	protected boolean allowDeleteFilledFolders=false;
	protected boolean allowUploadFile=false;
	/** allow updating/uplaoding a new version of an existing file ?*/
	protected boolean allowUpdateFile=false;
	/** allo view/download files ?*/
	protected boolean allowDownloadFile=false;
	/** when selecting a folder, can the user pick the root folder itself (or only a subfolder of it) ?*/
	protected boolean allowPickRootFolder=false;

	/** maximum TOTAL size in bytes of file(s) uploaded. Default : ~5MB*/
	protected long maxUploadSize=5000000;
	protected long maxFolderNameLength=200;
	/** If file uploads are enabled, how many file do we allow to upload at a time ?*/
	protected Integer nbOfUploadFields=new Integer(1);
	/** pattern for new dir names (for security reasons), default:letters,numbers,-,_ only**/
	protected String newDirPattern="[ a-zA-Z0-9_-]+";
	/** pattern for new/uploaded file names (for security reasons), default:letters,numbers,-,_,. only*/
	protected String newFilePattern="[\\.a-zA-Z0-9_-]+";
	/** by default sort by filename in alphabetical order*/
	protected int sortBy=JOTFileBrowserHelper.SORT_BY_NAME_ASC;
		
	/** warning messages : null = no warning, not null. Warning if a file with same name already exists*/
	protected String updateWarning="This file already exists: $1<br>Check the 'Overwrite Existing files'  to replace.";
	protected String fullFolderWarning="The folder is not empty: $1";
	protected String forbiddenWarning="You are not allowed to do this. $1";
	protected String failedWarning="Operation failed.<br>$1";
	/** warning messages : null = no warning, not null. File/folder name contains unallowed characters*/
	protected String fileNameWarning="File name can only contain: a-z,0-9,-,_";
	/** warning messages : null = no warning, not null. The session timed out, need to start over*/
	protected String sessionTimeoutWarning="Your session timed out, please try again.";
	// brosweType warnings:
	/** warning messages : null = no warning, not null. We want the user to pick only 1 file*/
	protected String oneFileWarning="Please select 1 and only 1 file.";
	/** warning messages : null = no warning, not null. We want the user to pick 1 or more file(s)*/
	protected String multipleFilesWarning="Please select at least 1 file.";
	/** warning messages : null = no warning, not null. We want the user to pick a folder*/
	protected String oneFolderWarning="Please select 1 and only 1 folder.";
	
	
	/** internal value*/
	protected File currentFolder=null;
	/** internal value*/
	protected Vector fileListing=null; 
	/** internal value*/
	protected String currentWarning=null;
	/** internal value*/
	protected File upFolder=null;
	
	
	/**
	 * Generic constructor, setting "Default" permissions
	 * @param rootFolder
	 * @param startFolder
	 */
	public JOTFileBrowserSession(File rootFolder, File startFolder, int browseType)
	{
		this.rootFolder=rootFolder;
		this.startFolder=startFolder;
		this.currentFolder=startFolder;
		this.browseType=browseType;
		if(browseType==JOTFileBrowserHelper.TYPE_UPLOAD_1_FILE)
		{
			setAllowUploadFile(true);
			setNbOfUploadFields(1);
			setAllowListFiles(false);
		}
	}



	public boolean isAllowBrowsing()
	{
		return allowBrowsing;
	}



	public void setAllowBrowsing(boolean allowBrowsing)
	{
		this.allowBrowsing = allowBrowsing;
	}



	public boolean isAllowCreateFolders()
	{
		return allowCreateFolders;
	}



	public void setAllowCreateFolders(boolean allowCreateFolders)
	{
		this.allowCreateFolders = allowCreateFolders;
	}



	public boolean isAllowDelete()
	{
		return allowDelete;
	}



	public void setAllowDelete(boolean allowDelete)
	{
		this.allowDelete = allowDelete;
	}



	public boolean isAllowDeleteFilledFolders()
	{
		return allowDeleteFilledFolders;
	}



	public void setAllowDeleteFilledFolders(boolean allowDeleteFilledFolders)
	{
		this.allowDeleteFilledFolders = allowDeleteFilledFolders;
	}



	public boolean isAllowRenaming()
	{
		return allowRenaming;
	}



	public void setAllowRenaming(boolean allowRenaming)
	{
		this.allowRenaming = allowRenaming;
	}



	public boolean isAllowUpdateFile()
	{
		return allowUpdateFile;
	}



	public void setAllowUpdateFile(boolean allowUpdateFile)
	{
		this.allowUpdateFile = allowUpdateFile;
	}



	public boolean isAllowUploadFile()
	{
		return allowUploadFile;
	}



	public void setAllowUploadFile(boolean allowUploadFile)
	{
		this.allowUploadFile = allowUploadFile;
	}



	public int getBrowseType()
	{
		return browseType;
	}



	public void setBrowseType(int browseType)
	{
		this.browseType = browseType;
	}



	public File getCurrentFolder()
	{
		return currentFolder;
	}



	public void setCurrentFolder(File currentFolder)
	{
		this.currentFolder = currentFolder;
	}



	public String getCurrentWarning()
	{
		return currentWarning;
	}



	public void setCurrentWarning(String currentWarning)
	{
		this.currentWarning = currentWarning;
	}



	public String getFailedWarning()
	{
		return failedWarning;
	}



	public void setFailedWarning(String failedWarning)
	{
		this.failedWarning = failedWarning;
	}



	public String getNewDirPattern()
	{
		return newDirPattern;
	}



	public void setNewDirPattern(String pattern)
	{
		newDirPattern = pattern;
	}



	public String getFileNameWarning()
	{
		return fileNameWarning;
	}



	public void setFileNameWarning(String fileNameWarning)
	{
		this.fileNameWarning = fileNameWarning;
	}



	public Vector getFileListing()
	{
		return fileListing;
	}



	public void setFileListing(Vector folderListing)
	{
		this.fileListing = folderListing;
	}



	public String getForbiddenWarning()
	{
		return forbiddenWarning;
	}



	public void setForbiddenWarning(String forbiddenWarning)
	{
		this.forbiddenWarning = forbiddenWarning;
	}



	public long getMaxFolderNameLength()
	{
		return maxFolderNameLength;
	}



	public void setMaxFolderNameLength(long maxFolderNameLength)
	{
		this.maxFolderNameLength = maxFolderNameLength;
	}



	public long getMaxUploadSize()
	{
		return maxUploadSize;
	}



	public void setMaxUploadSize(long maxUploadSize)
	{
		this.maxUploadSize = maxUploadSize;
	}



	public File getRootFolder()
	{
		return rootFolder;
	}



	public void setRootFolder(File rootFolder)
	{
		this.rootFolder = rootFolder;
	}



	public String getSessionTimeoutWarning()
	{
		return sessionTimeoutWarning;
	}



	public void setSessionTimeoutWarning(String sessionTimeoutWarning)
	{
		this.sessionTimeoutWarning = sessionTimeoutWarning;
	}



	public boolean isAllowListHiddenFiles()
	{
		return allowListHiddenFiles;
	}



	public void setAllowListHiddenFiles(boolean showHiddenFiles)
	{
		this.allowListHiddenFiles = showHiddenFiles;
	}



	public File getStartFolder()
	{
		return startFolder;
	}



	public void setStartFolder(File startFolder)
	{
		this.startFolder = startFolder;
	}



	public String getTempUploadFolder()
	{
		return tempUploadFolder;
	}



	public void setTempUploadFolder(String tempUploadFolder)
	{
		this.tempUploadFolder = tempUploadFolder;
	}



	public String getTitle()
	{
		return title;
	}



	public void setTitle(String title)
	{
		this.title = title;
	}



	public String getUpdateWarning()
	{
		return updateWarning;
	}



	public void setUpdateWarning(String updateWarning)
	{
		this.updateWarning = updateWarning;
	}



	public File getUpFolder()
	{
		return upFolder;
	}



	public void setUpFolder(File upFolder)
	{
		this.upFolder = upFolder;
	}



	public boolean isAllowDownloadFile()
	{
		return allowDownloadFile;
	}



	public void setAllowDownloadFile(boolean allowDownloadFile)
	{
		this.allowDownloadFile = allowDownloadFile;
	}



	public boolean isAllowListFiles()
	{
		return allowListFiles;
	}



	public void setAllowListFiles(boolean allowListFiles)
	{
		this.allowListFiles = allowListFiles;
	}



	public Integer getNbOfUploadFields()
	{
		return nbOfUploadFields;
	}



	public void setNbOfUploadFields(int nb)
	{
		this.nbOfUploadFields = new Integer(nb);
	}



	public boolean isAllowShowPath()
	{
		return allowShowPath;
	}



	public void setAllowShowPath(boolean allowShowPath)
	{
		this.allowShowPath = allowShowPath;
	}



	public boolean isAllowPickRootFolder()
	{
		return allowPickRootFolder;
	}



	public void setAllowPickRootFolder(boolean allowPickRootFolder)
	{
		this.allowPickRootFolder = allowPickRootFolder;
	}



	public String getNewFilePattern()
	{
		return newFilePattern;
	}



	public void setNewFilePattern(String newFilePattern)
	{
		this.newFilePattern = newFilePattern;
	}



	public int getSortBy()
	{
		return sortBy;
	}



	public void setSortBy(int sortBy)
	{
		this.sortBy = sortBy;
	}



	public String getFullFolderWarning()
	{
		return fullFolderWarning;
	}



	public void setFullFolderWarning(String fullFolderWarning)
	{
		this.fullFolderWarning = fullFolderWarning;
	}



	public String getMultipleFilesWarning()
	{
		return multipleFilesWarning;
	}



	public void setMultipleFilesWarning(String multipleFilesWarning)
	{
		this.multipleFilesWarning = multipleFilesWarning;
	}



	public String getOneFileWarning()
	{
		return oneFileWarning;
	}



	public void setOneFileWarning(String oneFileWarning)
	{
		this.oneFileWarning = oneFileWarning;
	}



	public String getOneFolderWarning()
	{
		return oneFolderWarning;
	}



	public void setOneFolderWarning(String oneFolderWarning)
	{
		this.oneFolderWarning = oneFolderWarning;
	}

	public Boolean lookingForFile()
	{
		return new Boolean(browseType==JOTFileBrowserHelper.TYPE_CHOOSE_1_FILE || browseType==JOTFileBrowserHelper.TYPE_CHOOSE_1PLUS_FILE);
	}
	public Boolean lookingForFolder()
	{
		return new Boolean(browseType==JOTFileBrowserHelper.TYPE_CHOOSE_1_FOLDER);
	}

	public Boolean lookingForSingleFileUpload()
	{
		return new Boolean(browseType==JOTFileBrowserHelper.TYPE_UPLOAD_1_FILE);
	}
}
