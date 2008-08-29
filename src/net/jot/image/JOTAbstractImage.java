/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.image;

import java.io.OutputStream;
import java.util.Vector;

/**
 * Represent an image data in a plain format, decoupled from storage format and AWT.
 * 
 * Data is stored in simple byteArray, RGBA(4 ints per pixel)  ~ potentially a memory hog.
 * @author thibautc
 */
public class JOTAbstractImage
{

    /** Default: Alpha values are ignored, but much faster perfomance*/
    public static final int ALPHA_SUPPORT_NONE = 1;
    /**
     * If a pixel is set a new color, we will mix the new color with the old one.
     * ie: if ALPHA is 25% for the new color we will do:
     * TODO: revise formula
     * R(G,B)=(oldR*75/100 + newR*25/100)/2
     * A=newA 
     * 
     * TODO:
     */
    public static final int ALPHA_SUPPORT_SIMPLE_BLEND = 2;
    private int[] data;
    private int width;
    private int height;
    private int alphaSupport = ALPHA_SUPPORT_NONE;

    /**
     * 
     * @param width
     * @param height
     * @param alphaSupport: default is alpha support disabled(faster)
     */
    public JOTAbstractImage(int width, int height, int alphaSupport)
    {
        this.width = width;
        this.height = height;
        data = new int[width * height * 4];
        this.alphaSupport = alphaSupport;
    }

    public JOTAbstractImage(int width, int height)
    {
        this.width = width;
        this.height = height;
        data = new int[width * height * 4];
    }

    public void setPixel(int x, int y, JOTAbstractPixel pixel)
    {
        if (x > -1 && y > -1 && y < height && x < width)
        {
            int offset = y * width * 4 + x * 4;
            data[offset] = pixel.getRed();
            data[offset + 1] = pixel.getGreen();
            data[offset + 2] = pixel.getBlue();
            data[offset + 3] = pixel.getAlpha();
        }
    }

    public JOTAbstractPixel getPixel(int x, int y)
    {
        int offset = y * width * 4 + x * 4;
        int red = data[offset];
        int green = data[offset + 1];
        int blue = data[offset + 2];
        int alpha = data[offset + 3];

        return new JOTAbstractPixel(red, green, blue, alpha);
    }

    public int[] getData()
    {
        return data;
    }

    public int getHeight()
    {
        return height;
    }

    public int getWidth()
    {
        return width;
    }

    /** 
     * return the color depth in bits (ie: how big the palette needs to be)
     * ie:
     * 1: 2 colors or less
     * 4: 16 colors or less
     * 8: 256 colors or less
     * 16: 65536 colors or less
     * 24: 16 777 216 colors or less
     * 32: > 16 777 216 colors
     * resource intensive
     * 
     * Note: maxColors, is the maximum you want to look for, if more that "maxColor" colors are found, an exception will be thrown.
     * @return
     */
    public int getColorDepth(long maxColors) throws Exception
    {
        Vector differentPixels = new Vector();
        // scans the image to count colors
        for (int y = 0; y != height; y++)
        {
            for (int x = 0; x != width; x++)
            {
                JOTAbstractPixel pixel = getPixel(x, y);
                if (!differentPixels.contains(pixel))
                {
                    differentPixels.add(pixel);
                }
                if (differentPixels.size() > maxColors)
                {
                    throw new Exception("Too many colors found : > " + maxColors);
                }
            }
        }
        if (differentPixels.size() < 2)
        {
            return 2;
        }
        if (differentPixels.size() < 16)
        {
            return 4;
        }
        if (differentPixels.size() < 256)
        {
            return 8;
        }
        if (differentPixels.size() < 65536)
        {
            return 16;
        }
        if (differentPixels.size() < 16777216)
        {
            return 24;
        }
        return 32;
    }

    public void drawRectangle(int lineThickness, int x, int y, int x2, int y2, JOTAbstractPixel pixel)
    {
        drawHorizontalLine(lineThickness, x, x2, y, pixel);
        drawHorizontalLine(lineThickness, x, x2, y2, pixel);
        drawVerticalLine(lineThickness, x, y, y2, pixel);
        drawVerticalLine(lineThickness, x2, y, y2, pixel);
    }

    public void drawFilledRectangle(int x, int y, int x2, int y2, JOTAbstractPixel pixel)
    {
        for (int i = x; i < x2; i++)
        {
            drawVerticalLine(1, i, y, y2, pixel);
        }
    }

    /**
     * fill the whole image with the same pixel
     * ie: set a background
     * @param pixel
     */
    public void fillImage(JOTAbstractPixel pixel)
    {
        for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                setPixel(x, y, pixel);
            }
        }
    }

    public void drawLine(int lineThickness, int x, int y, int x2, int y2, JOTAbstractPixel pixel)
    {
        //System.out.println(new Date()+" "+x+":" +y+"-"+x2+":"+y2);
        int xdir=1;
        int ydir=1;
        if (x > x2)
        {
            xdir=-1;
        }
        if (y > y2)
        {
            ydir=-1;
        }
        if (y == y2)
        {
           //horiz
            drawHorizontalLine(lineThickness, x, x2, y, pixel);
        } else if (x == x2)
        {
           //vert
            drawVerticalLine(lineThickness, x, y, y2, pixel);
        } else
        {
            // "seideways" line, time to do some math :-)
            int w = x2 - x;
            int h = y2 - y;

            // first pixel
            for (int j = 0; j != lineThickness; j++)
            {
                int thickOff = (j + 1) / 2;
                if (thickOff != 0)
                {
                    if ((j + 1) % 2 != 0)
                    {
                        thickOff = -thickOff;
                    }
                }
                setPixel(x + (w>h?0:thickOff), y  + (w>h?thickOff:0), pixel);
            }
            if (w != 0)
            {
                if(w>h)
                {
                //mostly horizontal
                float m = (float) h / (float) w;
                float b = y - m * x;
                while (x != x2)
                {
                    x+=xdir;
                    y = Math.round(m * x + b);
                    for (int j = 0; j != lineThickness; j++)
                    {
                        int thickOff = (j+1) / 2;
                        if (thickOff != 0)
                        {
                            if((j+1)%2!=0)
                                thickOff=-thickOff;
                        }
                        setPixel(x, y+thickOff, pixel);
                    }
                }
                }
                else
                {
                //mostly vertical
                float m = (float) w / (float) h;
                float b = x - m * y;
                while (y != y2)
                {
                    y+=ydir;
                    x = Math.round(m * y + b);
                    for (int j = 0; j != lineThickness; j++)
                    {
                        int thickOff = (j+1) / 2;
                        if (thickOff != 0)
                        {
                            if((j+1)%2!=0)
                                thickOff=-thickOff;
                        }
                        setPixel(x+thickOff, y, pixel);
                    }
                }
                }
            }
        }
    }

    protected void drawHorizontalLine(int lineThickness, int x, int x2, int y, JOTAbstractPixel pixel)
    {
        int xdir=x2>x?1:-1;
        for (int i = x; i < x2; i+=xdir)
        {
            for (int j = 0; j != lineThickness; j++)
            {
                int thickOff = (j + 1) / 2;
                if ((j + 1) % 2 == 0)
                {
                    thickOff = -thickOff;
                }
                setPixel(i, y + thickOff, pixel);
            }
        }
    }

    protected void drawVerticalLine(int lineThickness, int x, int y, int y2, JOTAbstractPixel pixel)
    {
        int ydir=y2>y?1:-1;
        for (int i = y; i != y2; i+=ydir)
        {
            for (int j = 0; j != lineThickness; j++)
            {
                int thickOff = (j + 1) / 2;
                if ((j + 1) % 2 == 0)
                {
                    thickOff = -thickOff;
                }
                setPixel(x + thickOff, i, pixel);
            }
        }
    }

    public int writeToStream(JOTAbstractImageWriterInterface imageWriter, OutputStream stream) throws Exception
    {
        return imageWriter.writeToStream(this, stream);
    }
}
