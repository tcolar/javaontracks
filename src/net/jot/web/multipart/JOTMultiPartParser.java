/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.web.multipart;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.util.Vector;

import net.jot.logger.JOTLogger;
import net.jot.web.JOTFlowRequest;

/**
 * This class is designed to parse a request coming from a multipart encoded form (ie: file upload form)
 * This does not support the full multipart spec, but support mixed form fields and files (multiple ok).
 * After calling request.parseMultiPartContent() you will get the following in the request:
 * The eventual regular form fields(text) will go automatically in the request so you can use them as ususal with request.getParameter()
 * If their are files, they will be stored in the request as well, so you can later use request.getFile("name"); and save/process the data.
 * Example of use:
 * <code>
 		request.parseMultiPartContent("/tmp", 50000000);
 		String someField=request.getParameter("htmlField1");
                JOTMultiPartItem item=request.getFile("htmlFile1");
		File f=new File("/tmp",item.getFileName());
		FileOutputStream fos=new FileOutputStream(f);
		item.copyDataTo(fos);
		fos.flush();
		fos.close();
	</code>
 * @author thibautc
 *
 */
public class JOTMultiPartParser
{
	private static final String BOUNDARY = "boundary=";
	private static final String FILENAME="filename=\"";
	private static final String VARNAME="name=\"";

	/**
	 * Parses the multipart form data, not called by default on regular requests, so you need to manually call request.parseMultiPartContent() .
	 * @param request
	 * @param tempFile  : a temporary file where the raw multipart data will be written.
	 * @param maxContentSize : maximum size (total) to accept 
	 * @throws Exception
	 */
	public static void parse(JOTFlowRequest request, File tempFile, long maxContentSize) throws Exception
	{
		String contentType = request.getContentType();
		if ((contentType != null) && (contentType.indexOf("multipart/form-data") >= 0)) 
		{
			int boundaryIndex=contentType.indexOf(BOUNDARY);
			long contentLength=request.getContentLength();
			
			if(contentLength>maxContentSize)
			{
				throw new JOTMPException("Content length is over maximum allowed.");
			}
			
			if(boundaryIndex==-1)
			{
				throw new JOTMPException("Boundary not found in contentType of multipart request.");
			}
			int endOfBoundary=contentType.indexOf(" ",boundaryIndex+BOUNDARY.length());
			if(endOfBoundary==-1)
				endOfBoundary=contentType.length();
			String boundary=contentType.substring(boundaryIndex+BOUNDARY.length(),endOfBoundary);
			JOTLogger.log(JOTLogger.DEBUG_LEVEL,JOTMultiPartParser.class,"MultiPart boundary: '" +boundary+"'");

			DataInputStream dis=new DataInputStream(request.getInputStream());
			saveRawData(dis,tempFile,maxContentSize);
			
			JOTMultiPartItem[] items=findParts(boundary, tempFile);
			
			updateRequest(items, request);
			
		}
	}

        /**
Update the requests by adding fields/files found in the multipart request
@param items 
@param request 
@throws java.lang.Exception 
*/
	private static void updateRequest(JOTMultiPartItem[] items, JOTFlowRequest request) throws Exception
	{
		for(int i=0;i!=items.length;i++)
		{
			JOTMultiPartItem item=items[i];
			if(item.getType()==JOTMultiPartItem.TYPE_VARIABLE)
			{
				JOTLogger.log(JOTLogger.DEBUG_LEVEL,JOTMultiPartParser.class,"Adding parameter to request: "+item.getName());
				request.setParameter(item.getName(),item.getDataAsString());
			}
			else if(item.getType()==JOTMultiPartItem.TYPE_FILE)
			{
				JOTLogger.log(JOTLogger.DEBUG_LEVEL,JOTMultiPartParser.class,"Adding file to request: "+item.getName());
				request.addFile(item);
			}
		}
	}

/**
Parse the multipart data, looking for boundaries and split the items found into JOTMultiPartItem(s)
@param boundary 
@param f 
@return 
@throws java.lang.Exception 
*/
	private static JOTMultiPartItem[] findParts(String boundary, File f) throws Exception
	{
		Vector items=new Vector();
		RandomAccessFile rand=new RandomAccessFile(f,"r");
		String str="";
		Vector boundaries=new Vector();
		long start=rand.getFilePointer();
		int newLineSize=-1;
		// first find all the bundaries.
		while((str=rand.readLine())!=null)
		{
			if(str.indexOf(boundary)>-1)
			{
				JOTLogger.log(JOTLogger.DEBUG_LEVEL,JOTMultiPartParser.class,"Found a Bundary @ "+start);
				boundaries.add(new Long(start));
				if(newLineSize<0)
				{
					//try to figure out the size of a new line by looking at the end of the first bundary line.
					rand.seek(rand.getFilePointer()-2);
					// 13 is the value of CarriageReturn
					if(rand.readByte()==13)
					// we have CR-LF
						newLineSize=2;
					else
						// we have only LF
						newLineSize=1;	
					
					JOTLogger.log(JOTLogger.DEBUG_LEVEL,JOTMultiPartParser.class,"NewLine Size:  "+newLineSize);
					// skip one byte to go back where we where at before starting this if.
					rand.skipBytes(1);
				}
			}
			start=rand.getFilePointer();
		}
		// now scan all the bundaries (only things BETWEEN bundaries is used - before or after is not used.). we don't the stuff after the last bounary
		for(int i=0; i<boundaries.size();i++)
		{
			JOTMultiPartItem item=null;
			// in between boundaries
			long pos=((Long)boundaries.get(i)).longValue();
			// go to the beginning of bundary
			rand.seek(pos);
			// read the bundary
			rand.readLine();
			// read the next lines, which should be Content- stuff,until an empty line
			String line=rand.readLine();
			while(line!=null && line.length()>0)
			{
				if(line.startsWith("Content-Disposition:"))
				{
					JOTLogger.log(JOTLogger.DEBUG_LEVEL,JOTMultiPartParser.class,"Found a boundary disposition: " +line);
					int fileStart=line.indexOf(FILENAME);
					if(fileStart!=-1)
					{
						// we have a file
						String filename=line.substring(fileStart+FILENAME.length(),line.indexOf("\"",fileStart+FILENAME.length()));
						item=new JOTMultiPartItem(f,JOTMultiPartItem.TYPE_FILE,"");
						item.setFilePath(filename);
					}

					int varStart=line.indexOf(VARNAME);
					if(varStart!=-1)
					{
						//we have a variable
						String varname=line.substring(varStart+VARNAME.length(),line.indexOf("\"",varStart+VARNAME.length()));
						if(item==null)
							item=new JOTMultiPartItem(f,JOTMultiPartItem.TYPE_VARIABLE,varname);
						else
							// adding the name of the file.
							item.setName(varname);
					}
					
				}
				line=rand.readLine();
			}
			long dataStart=rand.getFilePointer();
			if(item!=null)
			{
				long dataEnd=((Long)boundaries.get(i+1)).longValue();
				// data ends BEFORE the newline.
				if(dataEnd>dataStart+newLineSize)
					dataEnd-=newLineSize;
				item.setDataStart(dataStart);
				item.setDataEnd(dataEnd);
				items.add(item);

				JOTLogger.log(JOTLogger.DEBUG_LEVEL,JOTMultiPartParser.class,"Found a multipart item: " +item.getName()+" data from: "+dataStart+"-"+dataEnd);
			}
		}
		rand.close();
		return (JOTMultiPartItem[])items.toArray(new JOTMultiPartItem[0]);
	}

/**
Saves the raw multipart data into a temporary file, so we can later access it to retrieve the file(s) data.
@param dis 
@param tempFile 
@param maxSize 
@throws java.lang.Exception 
*/
	private static void saveRawData(DataInputStream dis, File tempFile, long maxSize) throws Exception
	{
		JOTLogger.log(JOTLogger.DEBUG_LEVEL,JOTMultiPartParser.class,"Saving multipart raw data to: "+tempFile.getAbsolutePath());
		// 30KB buffer
		byte[] buffer=new byte[30000];
		FileOutputStream raw = new FileOutputStream(tempFile);
		int read;
		int totalRead=0;
		do
		{
			read=dis.read(buffer);
			if(read!=-1)
			{
				totalRead+=read;
				raw.write(buffer,0,read);
				raw.flush();
			}
		}
		while(read!=-1 && totalRead<=maxSize);		
		//if the file is to big (content-length must have been faked), then just truncating it.
		raw.close();
	}

}
