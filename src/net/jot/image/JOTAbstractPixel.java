/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */

package net.jot.image;

/**
 * Represent the (color) data for a pixel (RGBA)
 * @author thibautc
 */
public class JOTAbstractPixel 
{
    int red;
    int blue;
    int green;
    int alpha;
    
    public JOTAbstractPixel(int red, int green, int blue, int alpha)
    {
        this.red=red;
        this.blue=blue;
        this.green=green;
        this.alpha=alpha;
    }

    public int getAlpha()
    {
        return alpha;
    }

    public int getBlue()
    {
        return blue;
    }

    public int getGreen()
    {
        return green;
    }

    public int getRed()
    {
        return red;
    }

    public void setAlpha(int alpha)
    {
        this.alpha = alpha;
    }

    public void setBlue(int blue)
    {
        this.blue = blue;
    }

    public void setGreen(int green)
    {
        this.green = green;
    }

    public void setRed(int red)
    {
        this.red = red;
    }

    public int hashCode()
    {
        int hash = 5;
        hash = 41 * hash + this.red;
        hash = 41 * hash + this.blue;
        hash = 41 * hash + this.green;
        hash = 41 * hash + this.alpha;
        return hash;
    }
    
    /**
     * Compares two pixels (macthing color)
     * @param comp
     * @return
     */
    public boolean equals(Object comp)
    {
        if( ! (comp instanceof JOTAbstractPixel))
            return false;
        JOTAbstractPixel compPixel=(JOTAbstractPixel)comp;
        boolean same= compPixel.getRed() == getRed() &&
                compPixel.getBlue() == getBlue() &&
                compPixel.getGreen() == getGreen() &&
                compPixel.getAlpha() == getAlpha();
        return same;
    }

    void asByteArray()
    {
        byte[] b=new byte[16];
        b[0]=(byte)(getRed() & 0xff);
        b[1]=(byte)(getRed() >> 8& 0xff);
        b[2]=(byte)(getRed() >> 16 & 0xff);
        b[3]=(byte)(getRed() >> 24 & 0xff);
        b[4]=(byte)(getGreen() & 0xff);
        b[5]=(byte)(getGreen() >> 8& 0xff);
        b[6]=(byte)(getGreen() >> 16 & 0xff);
        b[7]=(byte)(getGreen() >> 24 & 0xff);
        b[8]=(byte)(getBlue() & 0xff);
        b[9]=(byte)(getBlue() >> 8& 0xff);
        b[10]=(byte)(getBlue() >> 16 & 0xff);
        b[11]=(byte)(getBlue() >> 24 & 0xff);
        b[12]=(byte)(getAlpha() & 0xff);
        b[13]=(byte)(getAlpha() >> 8& 0xff);
        b[14]=(byte)(getAlpha() >> 16 & 0xff);
        b[15]=(byte)(getAlpha() >> 24 & 0xff);
    }
}
