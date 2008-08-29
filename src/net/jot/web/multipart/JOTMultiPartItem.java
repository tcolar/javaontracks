/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.web.multipart;

import java.io.File;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
When a multipart request is processed, we can find one or more MultiPart items.
This might be either form fields (text,password etc..) or uploaded files.
@author thibautc
*/
public class JOTMultiPartItem
{
	public static final int TYPE_FILE = 1;
	public static final int TYPE_VARIABLE = 2;
	
	File file=null;
	String name=null;
	long dataStart=-1;
	long dataEnd=-1;
	int type=-1;
	String filePath=null;
	
/**
File path provided by browser for uploaded file
@param filePath 
*/
	public void setFilePath(String filePath)
	{
		this.filePath = filePath;
	}

	public JOTMultiPartItem(File file, int type, String name)
	{
		this.file=file;
		this.type=type;
		setName(name);
	}

/**
Index of file/field data end in raw multipart temp file.
@return 
*/	
	protected long getDataEnd()
	{
		return dataEnd;
	}
	protected void setDataEnd(long dataEnd)
	{
		this.dataEnd = dataEnd;
	}
/**
Index of file/field data start in raw multipart temp file.
@return 
*/	
	protected long getDataStart()
	{
		return dataStart;
	}
	protected void setDataStart(long dataStart)
	{
		this.dataStart = dataStart;
	}
	public String getName()
	{
		return name;
	}
	protected int getType()
	{
		return type;
	}

	protected void setName(String name)
	{
		this.name = name;
	}

/**
Returns the RAW multipart data File handle.
@return 
*/
	protected File getRawFile()
	{
		return file;
	}
	
	/**
	 * Returns the data as a string, this should be use for variables only 
	 * Not for files.
	 * @return
	 */
	public String getDataAsString() throws Exception
	{
		return new String(getDataAsBytes());
	}
	
	/**
	 * Returns the data as a byte array
	 * WARNING: for a file this might be a huge byte array, using lots of memory,
	 * it is best to use copyDataTo(os).
	 * @return
	 */
	public byte[] getDataAsBytes() throws Exception
	{
		long dataLength=dataEnd-dataStart;
		byte[] buffer=new byte[(int)dataLength];
		RandomAccessFile f=new RandomAccessFile(file,"r");
		f.seek(dataStart);
		f.read(buffer);
		f.close();	
		return buffer;
	}
	
	/**
	 * Saves the data to a stream (ie: File) without using much memory.
	 * @param os
	 */
	public void copyDataTo(OutputStream os) throws Exception
	{
		// use 30k buffer
		long dataLength=dataEnd-dataStart;
		long bytesRead=0;
		byte[] buffer=new byte[30000];
		long bytesLeft;
		RandomAccessFile f=new RandomAccessFile(file,"r");
		f.seek(dataStart);
		do
		{
			bytesLeft=dataLength-bytesRead;
			long bytesToRead=bytesLeft>buffer.length?buffer.length:bytesLeft;
			f.read(buffer,0,(int)bytesToRead);
			bytesRead+=bytesToRead;
			os.write(buffer,0,(int)bytesToRead);
			os.flush();
		}
		while(bytesLeft>0);

		f.close();				
	}

	/**
	 * For a file returns the name of the file provided by the user
	 * Note: some browsers give a full path, some others give only the file name (no path)
	 * you probably want to use getFileName() instead;
	 * @return
	 */
	public String getFilePath()
	{
		return filePath;
	}

	/**
	 * return the file name only (remove the path if the browser gave one like ie does.)
	 * @return
	 */
	public String getFileName()
	{
		String fileName=filePath;
                
		if(fileName.lastIndexOf("\\")!=-1)
			fileName=fileName.substring(fileName.lastIndexOf("\\")+1,fileName.length());
		else if(fileName.lastIndexOf("/")!=-1)
			fileName=fileName.substring(fileName.lastIndexOf("/")+1,fileName.length());
		return fileName;
	}


}

