/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.captcha;

import java.util.Random;
import net.jot.image.JOTAbstractImage;
import net.jot.image.JOTAbstractPixel;

/**
 * Implements a 'CAPTCHA' image.
 * 'CAPTCHA' gotta be one of the worst acronysm i ever came across !
 * it stands for "Completely Automated Public Turing test to tell Computers and Humans Apart"
 * Anyway our Captcha here, is the common image with letters/numbers inside which is hard for a computer to parse
 * but easy for a human.
 * This is use mostly to prevent "robotized" use of a form to fill your website with junk/spam.
 * 
 * @author thibautc
 */
public class JOTCaptchaImage extends JOTAbstractImage
{

    private int waviness = 3;
    private Random randomizer = new Random();

    public JOTCaptchaImage(int width, int height)
    {
        super(width, height);
    }

    public JOTCaptchaImage(int width, int height, int ALPHA_TYPE)
    {
        super(width, height, ALPHA_TYPE);
    }

    /**
     * Draw a wavy line with a thickness of 1 pixel (uses wavyness)
     * @param x
     * @param y
     * @param x2
     * @param y2
     * @param pixel
     */
    public void drawWavyLine(int x, int y, int x2, int y2, JOTAbstractPixel pixel, boolean holes)
    {
        this.drawWavyLine(1, x, y, x2, y2, pixel, holes);
    }

    /**
     * Draw a wavy line with given thickness (uses wavyness)
     * If holes is enabled, some blank pixel with be mixed, leaving holes
     * @param lineThickness
     * @param x
     * @param y
     * @param x2
     * @param y2
     * @param pixel
     */
    public void drawWavyLine(int lineThickness, int x, int y, int x2, int y2, JOTAbstractPixel pixel, boolean holes)
    {
        int wavy = waviness;
        int xdir = 1;
        int ydir = 1;
        if (x > x2)
        {
            xdir = -1;
        }
        if (y > y2)
        {
            ydir = -1;
        }
        double lineLength = Math.sqrt(Math.pow(x2 - x, 2) + Math.pow(y2 - y, 2));
        if (wavy > lineLength)
        {
            wavy = (int) Math.floor(lineLength - 1);
        }
        if (wavy < 0)
        {
            wavy = 0;
        }
        if (y == y2)
        {
            drawWavyHorizontalLine(lineThickness, x, x2, y, pixel, wavy, holes);
        } else if (x == x2)
        {
            drawWavyVerticalLine(lineThickness, x, y, y2, pixel, wavy, holes);
        } else
        {
            // "seideways" line, time to do some math :-)
            int w = x2 - x;
            int h = y2 - y;
            int offset = 0;
            // first pixel.
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
                int extra = offset + thickOff + (holes ? thickOff : 0);
                setPixel(x + (w > h ? 0:extra), y + (w > h ? extra:0), pixel);
            }
            if (w != 0)
            {
                if (w > h)
                {
                    //mostly horizontal
                    float m = (float) h / (float) w;
                    float b = y - m * x;
                    while (x != x2)
                    {
                        offset = getNextWaveOffset(wavy, offset, x2-x, w);
                        x += xdir;
                        y = Math.round(m * x + b);
                        for (int j = 0; j != lineThickness; j++)
                        {
                            int thickOff = (j + 1) / 2;
                            if (thickOff != 0)
                            {
                                if ((j + 1) % 2 != 0)
                                {
                                    thickOff = - thickOff;
                                }
                            }
                            setPixel(x, y + offset + thickOff + (holes ? thickOff : 0), pixel);
                        }
                    }
                } else
                {
                    //mostly vertical
                    float m = (float) w / (float) h;
                    float b = x - m * y;
                    while (y != y2)
                    {
                        offset = getNextWaveOffset(wavy, offset, y2 - y, h);
                        y += ydir;
                        x = Math.round(m * y + b);
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
                            setPixel(x + offset + thickOff + (holes ? thickOff : 0), y, pixel);
                        }
                    }
                }
            }
        }
    }

    protected void drawWavyHorizontalLine(int lineThickness, int x, int x2, int y, JOTAbstractPixel pixel, int wavyness, boolean holes)
    {
        int offset = 0;
        int xdir = x2 > x ? 1 : -1;
        for (int i = x; i < x2; i += xdir)
        {
            offset = getNextWaveOffset(wavyness, offset, i - x, x2 - x);
            for (int j = 0; j != lineThickness; j++)
            {
                int thickOff = (j + 1) / 2;
                if ((j + 1) % 2 == 0)
                {
                    thickOff = -thickOff;
                }
                setPixel(i + (holes ? thickOff : 0), y + offset + thickOff, pixel);
            }
        }
    }

    protected void drawWavyVerticalLine(int lineThickness, int x, int y, int y2, JOTAbstractPixel pixel, int wavyness, boolean holes)
    {
        int offset = 0;
        int ydir = y2 > y ? 1 : -1;
        for (int i = y; i != y2; i += ydir)
        {
            offset = getNextWaveOffset(wavyness, offset, i - y, y2 - y);
            for (int j = 0; j != lineThickness; j++)
            {
                int thickOff = (j + 1) / 2;
                if ((j + 1) % 2 == 0)
                {
                    thickOff = -thickOff;
                }
                setPixel(x + thickOff + offset, i + (holes ? thickOff : 0), pixel);
            }
        }
    }

    public void setWaviness(int waviness)
    {
        this.waviness = waviness;
    }

    public int getWaviness()
    {
        return waviness;
    }

    private int getNextWaveOffset(int wavyness, int curOffset, int linePos, int lineLength)
    {
        linePos = Math.abs(linePos);
        lineLength = Math.abs(lineLength);
        if (linePos == 0 || linePos == lineLength)
        {
            return 0;
        }

        if (linePos >= lineLength - wavyness)
        {
            // time to get back toward endpoint
            if (curOffset == 0)
            {
                return 0;
            } else if (curOffset > 0)
            {
                return curOffset - 1;
            } else if (curOffset < 0)
            {
                return curOffset + 1;
            }
        }
        // will return -1,0 or 1
        int newOf = (int) randomizer.nextLong() % 2;

        if (Math.abs(curOffset + newOf) > wavyness)
        {
            newOf = 0;
        }

        return curOffset + newOf;
    }
}
