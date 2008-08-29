/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */

package net.jot.image.writers;


import net.jot.image.JOTAbstractImageWriterInterface;
import net.jot.image.JOTAbstractPixel;
import net.jot.image.JOTAbstractImage;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.io.OutputStream;
import java.util.Hashtable;

/**
 * Implementation to write an AbstractImage into a BMP format file.
 * Note that it support 24Bits colors max, 8 bits per color (and no alpha)
 * If alpha is present in AbstractImage, it will be ignored.
 * (Does not support compressed BMP)
 * @author thibautc
 */
public class JOTBMPImageWriter implements JOTAbstractImageWriterInterface
{
    private static final int FILE_HEADER_SIZE=14;
    private static final int BITMAP_HEADER_SIZE=40;
    
    public int writeToStream(JOTAbstractImage abstractImage, OutputStream out) throws Exception
    {
        return writeImage(out, abstractImage);
    }

    private int determineFileSize(JOTAbstractImage abstractImage, int paletteSize)
    {
        int size=FILE_HEADER_SIZE+BITMAP_HEADER_SIZE;
        /** each palette entry(RGBQUAD) is 4 bytes (Blue, Green, Red, 0);
         * NOTE the order !! Blue, Green, Red, 0
         */
        int paletteEntries=0;
        if(paletteSize==2)
            paletteEntries=2;
        if(paletteSize==4)
            paletteEntries=16;
        if(paletteSize==8)
            paletteEntries=256;
        size+=paletteEntries*4;
        // data size
        // size if no palette: 3 byte per pixel (RGB)
        float bytePerPixel=3;
        if(paletteSize==2)
            bytePerPixel=1f/8;
        if(paletteSize==4)
            bytePerPixel=1f/2;
        if(paletteSize==8)
           bytePerPixel=1;
 
        float rs=bytePerPixel*abstractImage.getWidth();
        int rowSize=(int)Math.ceil(rs);
        // rosSize must be multiple of 4 as says BMP spec
        while(rowSize%4!=0)
            rowSize++;
        int imageSize=rowSize*abstractImage.getHeight();
        
        size+=imageSize;
        
        return size;
    }

    private int writeImage(OutputStream os, JOTAbstractImage abstractImage) throws Exception
    {
        // color depth(nmber of colors), ie 2,4,16,256.   0 = no palette
        int paletteDepth=0;
        try
        {
            paletteDepth=abstractImage.getColorDepth(256);
        }
        catch(Exception e)
        {
            // if exception it means > 256 colors, which means we should not use a palette, but straight rgb values
        }
        
        int fileSize=determineFileSize(abstractImage, paletteDepth);
        //palette size in bytes
        int paletteSize=0;
        int bitsPerPixel=24;
        if(paletteDepth==2)
        {
            bitsPerPixel=1;
            paletteSize=2;
        }
        if(paletteDepth==4)
        {
            bitsPerPixel=4;
            paletteSize=16;
        }
        if(paletteDepth==8)
        {
           bitsPerPixel=8;
           paletteSize=256;
        }
        // offset of image data in bytes
        int bmOffset=(FILE_HEADER_SIZE+BITMAP_HEADER_SIZE+paletteSize*4);
        
        // write the fileHeader
        DataOutputStream dos=new DataOutputStream(os);
        //BM (bitmap signature)
        writeNumber(dos,0x42,1);
        writeNumber(dos,0x4D,1);
        // filesize
        writeNumber(dos,fileSize,4);
        // Empty 4 bytes (according to spec)
        writeNumber(dos, 0, 4);
        // bitmap data offest (after palette)
        writeNumber(dos, bmOffset, 4);

        // write the bitmap header
        // header size : (std = 40)
        writeNumber(dos, 40, 4);
        // image width, height
        writeNumber(dos, abstractImage.getWidth() ,4);
        writeNumber(dos, abstractImage.getHeight() ,4);
        // nb of planes (1=default)
        writeNumber(dos, 1 ,2);
        // bit count
        writeNumber(dos, bitsPerPixel ,2);
        //compression (0 = none)
        writeNumber(dos, 0 ,4);
        // size of image (leave at 0 for auto)
        writeNumber(dos, 0 ,4);
        //pixelPerMeter stuff (not needed)
        writeNumber(dos, 0 ,4);
        writeNumber(dos, 0 ,4);
        // biclrused (0 = use bit count)
        writeNumber(dos, 0 ,4);
        // important color (0=all)
        writeNumber(dos, 0 ,4);
        
        //write the palette (RGBSQUAD):  Blue/Green/Red/0
        // Color => Index in palette
        Hashtable palette=new Hashtable();
        if(paletteDepth>0)
        {
            int paletteIndex=0;
            for(int y=0;y!=abstractImage.getHeight();y++)
            {
                for(int x=0;x!=abstractImage.getWidth();x++)
                {
                    JOTAbstractPixel pixel=abstractImage.getPixel(x, y);
                    if( ! palette.containsKey(pixel))
                    {
                        palette.put(pixel,new Integer(paletteIndex));
                        paletteIndex++;
                        // Blue/Green/Red/0
                        writeNumber(dos, pixel.getBlue(), 1);
                        writeNumber(dos, pixel.getGreen(), 1);
                        writeNumber(dos, pixel.getRed(), 1);
                        writeNumber(dos, 0, 1);
                    }
                }
            }
            // Pad the rest of the palette with zeroes
            for(int i=paletteIndex;i!=paletteSize;i++)
            {
                writeNumber(dos, 0, 4);
                paletteIndex++;
            }
        }
        
        //write the image data
        //Note: inn BMP image, last row goes first !
        for(int y=abstractImage.getHeight()-1;y>=0;y--)
            {
                int rowSize=0;
                byte tempByte=0;
                int tempByteCpt=0;
                for(int x=0;x!=abstractImage.getWidth();x++)
                {
                    JOTAbstractPixel pixel=abstractImage.getPixel(x, y);
                    if(paletteDepth==0)
                    {
                        // no palette, simply write RGB values
                        writeNumber(dos, pixel.getRed(), 1);
                        writeNumber(dos, pixel.getGreen(), 1);
                        writeNumber(dos, pixel.getBlue(), 1);
                        rowSize+=3;
                    }
                    else
                    {
                        // we use a palette
                        if(paletteDepth==8)
                        {
                            // simple one byte per pixel
                           writeNumber(dos, ((Integer)palette.get(pixel)).intValue(), 1); 
                           rowSize++;
                        }
                        else
                        {
                            // need to feel a byte with several pixels
                            int index=((Integer)palette.get(pixel)).intValue();
                            tempByte=(byte) ((tempByte << bitsPerPixel) + index); 
                            tempByteCpt++;
                            if(tempByteCpt==8/bitsPerPixel)
                            {
                                writeNumber(dos, tempByte, 1); 
                                rowSize++;
                                tempByte=0;
                                tempByteCpt=0;
                            }
                        }
                        
                    }
                }
                // finish incomplete bytes.
                if(tempByteCpt!=0)
                {
                    while(tempByteCpt!=8/bitsPerPixel)
                    {
                        tempByte=(byte) (tempByte << bitsPerPixel);
                        tempByteCpt++;
                    }
                    writeNumber(dos, tempByte, 1);
                    rowSize++;
                }
                // a row must be a power of 4(bytes), padding with 0's as necessary
                while(rowSize%4!=0)
                {
                    rowSize++;
                    writeNumber(dos, 0, 1);
                }
        }
        return fileSize;
    }

    /**
     * write a number (up to 8bytes/long into the given number of bytes)
     * Of course if the number passed doesn't fit in the given number of bytes, it will be truncated (head)
     * @param dos
     * @param number
     * @param bytes
     * @throws java.lang.Exception
     */
    private void writeNumber(DataOutputStream dos, long number, int bytes) throws Exception
    {
        byte[] buffer=new byte[bytes];
        if(bytes>7)
            buffer[7]=(byte)(number >> 56 & 0xff);
        if(bytes>6)
            buffer[6]=(byte)(number >> 48 & 0xff);
        if(bytes>5)
            buffer[5]=(byte)(number >> 40 & 0xff);
        if(bytes>4)
            buffer[4]=(byte)(number >> 32 & 0xff);
        if(bytes>3)
            buffer[3]=(byte)(number >> 24 & 0xff);
        if(bytes>2)        
            buffer[2]=(byte)(number >> 16 & 0xff);
        if(bytes<1)        
            buffer[1]=(byte)(number >> 8 & 0xff);
        buffer[0]=(byte)(number & 0xff);
        
        dos.write(buffer);
    }

    public String getContentType()
    {
       return "image/bmp";
    }

}
