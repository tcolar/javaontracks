/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.captcha.generators;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Random;
import javax.servlet.http.HttpServletResponse;
import net.jot.image.JOTAbstractImage;
import net.jot.image.JOTAbstractPixel;
import net.jot.image.JOTAbstractImageWriterInterface;
import net.jot.captcha.JOTCaptchaGeneratorInterface;
import net.jot.captcha.JOTCaptchaImage;

/**
 * My own captcha impl, called "drunk" because of the way the letters look (wavy)
 * Generate 200x60 captcha containing 5 characters
 * characters used are listed in CHARS (others can be confusing) 
 * Background of image is white
 * Characters use Random colors
 * We add some random lines of the same color as the characters to the background(1 pixel thick)
 * Characters "size" varies a bit between (scale: 3 to 5)
 * Characters lines thickness vary from 3 to 5
 * Characters are drown using "wavy" lines (wavyness: 1 to 4)
 * Wavy lines will also have holes in them (sometimes)
 * @author thibautc
 */
public class JOTSTDCaptchaGenerator implements JOTCaptchaGeneratorInterface
{

    //public final static AbstractImagePixel WHITE_PIX = new AbstractImagePixel(255, 255, 255, 0);
    public final static int WIDTH = 220;
    public final static int HEIGHT = 60;
    public final static int CODE_LENGTH_MIN = 4;
    public final static int CODE_LENGTH_MAX = 6;
    public final static int MIN_CHAR_SCALE = 3;
    public final static int MAX_CHAR_SCALE = 5;
    public final static int MIN_CHAR_COLORS = 1;
    public final static int MAX_CHAR_COLORS = CODE_LENGTH_MAX;
    public final static int BG_LINES = 40;
    public final static int BG_LINES_LENGTH = 150;
    public final static int FG_LINES = 10;
    public final static int FG_LINES_LENGTH = 180;
    public final static int CHAR_THICKNESS_MIN = 3;
    public final static int CHAR_THICKNESS_MAX = 5;
    public final static int WAVYNESS_MIN = 1;
    public final static int WAVYNESS_MAX = 4;
    private Random randomizer = new Random();
    // all the allowed chard need to be defined in CHAR_POINTS
    private final static String CHARS = "ACDEFGHJKLNPRTXY346";
    private static Hashtable CHARS_POINTS = new Hashtable();
    private String code="";
    // define the characters look in 9x9(0..8) scale
    static
    {
        Line[] a = {new Line(4, 0, 0, 8), new Line(4, 0, 8, 8), new Line(2, 4, 6, 4)};
        CHARS_POINTS.put("A", a);
        Line[] c = {new Line(0, 0, 8, 0), new Line(0, 0, 0, 8), new Line(0, 8, 8, 8)};
        CHARS_POINTS.put("C", c);
        Line[] d = {new Line(0, 0, 8, 0), new Line(3, 0, 3, 8), new Line(0, 8, 8, 8), new Line(8, 0, 8, 8)};
        CHARS_POINTS.put("D", d);
        Line[] e = {new Line(0, 0, 6, 0), new Line(0, 4, 6, 4), new Line(0, 8, 6, 8), new Line(0, 0, 0, 8)};
        CHARS_POINTS.put("E", e);
        Line[] f = {new Line(0, 0, 6, 0), new Line(0, 3, 6, 3), new Line(0, 0, 0, 8)};
        CHARS_POINTS.put("F", f);
        Line[] g = {new Line(0, 0, 6, 0), new Line(6,0,6,2), new Line(0, 0, 0, 8),new Line(0, 8, 6, 8),new Line(6, 8, 6, 5),new Line(4,5,8,5)};
        CHARS_POINTS.put("G", g);
        Line[] h = {new Line(8, 0, 8, 8), new Line(0, 4, 8, 4), new Line(0, 0, 0, 8)};
        CHARS_POINTS.put("H", h);
        Line[] j = {new Line(1, 0, 7, 0), new Line(4, 0, 4, 8), new Line(1, 8, 4, 8), new Line(1, 8, 1, 6)};
        CHARS_POINTS.put("J", j);
        Line[] k = {new Line(0, 4, 8, 0), new Line(0, 4, 8, 8), new Line(0, 0, 0, 8)};
        CHARS_POINTS.put("K", k);
        Line[] l = {new Line(2, 0, 2, 8), new Line(2, 8, 6, 8)};
        CHARS_POINTS.put("L", l);
        Line[] n = {new Line(0, 0, 6, 8), new Line(6, 0, 6, 8), new Line(0, 0, 0, 8)};
        CHARS_POINTS.put("N", n);
        Line[] p = {new Line(0,0,0,8), new Line(0, 0, 6, 0), new Line(6, 0, 6, 3), new Line(0, 3, 6, 3)};
        CHARS_POINTS.put("P", p);
        Line[] r = {new Line(0,0,0,8), new Line(0, 0, 6, 0), new Line(6, 0, 6, 3), new Line(0, 3, 6, 3), new Line(0, 3, 6, 8)};
        CHARS_POINTS.put("R", r);
        Line[] t = {new Line(0, 0, 8, 0), new Line(4, 0, 4, 8)};
        CHARS_POINTS.put("T", t);
        Line[] x = {new Line(0, 0, 8, 8), new Line(0, 8, 8, 0)};
        CHARS_POINTS.put("X", x);
        Line[] y = {new Line(0, 0, 4, 4), new Line(4, 4, 8, 0), new Line(4, 4, 4, 8)};
        CHARS_POINTS.put("Y", y);
        Line[] _3 = {new Line(0, 8, 8, 8),new Line(0, 4, 8, 4), new Line(0, 0, 8, 0), new Line(8, 0, 8, 8)};
        CHARS_POINTS.put("3", _3);
        Line[] _4 = {new Line(1, 0, 0, 4), new Line(0, 4, 8, 4), new Line(5, 2, 5, 8)};
        CHARS_POINTS.put("4", _4);
        Line[] _6 = {new Line(0, 0, 0, 8), new Line(0, 0, 6, 0), new Line(0, 4, 6, 4),new Line(0, 8, 6, 8),new Line(6, 4, 6, 8)};
        CHARS_POINTS.put("6", _6);

    }

    /*public String writeImage(JOTAbstractImageWriterInterface writer, File imageFile) throws Exception
    {
        generateImage().writeToFile(writer, imageFile);
        return code;
    }**/

    private JOTAbstractImage generateImage()
    {
        JOTAbstractPixel bgColor=getRandomColor();
        JOTCaptchaImage image = new JOTCaptchaImage(WIDTH, HEIGHT, JOTAbstractImage.ALPHA_SUPPORT_SIMPLE_BLEND);
        image.setWaviness(2);
        code = generateCode();
        int nbColors=MIN_CHAR_COLORS+Math.abs(randomizer.nextInt(MAX_CHAR_COLORS+1-MIN_CHAR_COLORS));
        // fill BG with bgcolor and some similar color lines
        image.fillImage(bgColor);
        int red=bgColor.getRed()+randomizer.nextInt()%10;
        int green=bgColor.getGreen()+randomizer.nextInt()%10;
        int blue=bgColor.getBlue()+randomizer.nextInt()%10;
        if(red>255) red=255;
        if(green>255) green=255;
        if(blue>255) blue=255;
        JOTAbstractPixel bgColor2=new JOTAbstractPixel(red,green,blue,0);
        for(int i=3;i<HEIGHT-1;i+=8)
        {
            image.drawWavyLine(5, 1,i,WIDTH-1,i, bgColor2, true);
        }
        // code chars
        JOTAbstractPixel[] pixels = new JOTAbstractPixel[nbColors];
        for (int i = 0; i != nbColors; i++)
        {
            pixels[i] = getSafeColor(bgColor);
        }
        int nbLineCharColors=Math.abs(randomizer.nextInt(BG_LINES-1));
        for (int j = 0; j !=nbLineCharColors; j++)
        {
                JOTAbstractPixel pixel=pixels[(j+1)%nbColors];
                drawRandomLine(image, 5, BG_LINES_LENGTH, pixel);
        }
        
        int nbOtherColors=2+Math.abs(randomizer.nextInt(3));
        JOTAbstractPixel[] otherPixels = new JOTAbstractPixel[nbOtherColors];
        for(int i=0;i!=otherPixels.length;i++)
        {
            otherPixels[i]=getRandomColor();
        }
        for (int j = 0; j !=BG_LINES-nbLineCharColors; j++)
        {
                JOTAbstractPixel pixel=otherPixels[(j+1)%otherPixels.length];
                drawRandomLine(image, 5, BG_LINES_LENGTH, pixel);
        }
        
        int xOffset = 2+Math.abs(randomizer.nextInt(10));
        for (int i = 0; i != code.length(); i++)
        {
            drawCharacter(image, code.charAt(i), xOffset, pixels[(i+1)%nbColors]);
            xOffset += ((WIDTH-25) / code.length()) + randomizer.nextInt(5);
        }
        for (int i = 0; i != FG_LINES; i++)
        {
            //TODO: fglines
            drawRandomLine(image, 5, FG_LINES_LENGTH, bgColor);
        }
        return image;
    }
    
    private JOTAbstractPixel getSafeColor(JOTAbstractPixel bgColor)
    {
        JOTAbstractPixel pixel=null;
        boolean done=false;
        do
        {
            pixel=getRandomColor();
            int diff=Math.abs(bgColor.getBlue()-pixel.getBlue())+
                    Math.abs(bgColor.getRed()-pixel.getRed())+
                    Math.abs(bgColor.getGreen()-pixel.getGreen());
            if(diff>300)
            {
                done=true;
            }
        }
        while(!done);
        return pixel;
    }
    
    private void drawCharacter(JOTCaptchaImage image, char c, int xOffset, JOTAbstractPixel pixel)
    {
 
        int x = 0;
        int y = 0;
        // use random waviness, random char size, random thickness, random holes or not
        // TODO: implement alpha blend in image
        Line[] lines = (Line[]) CHARS_POINTS.get(""+c);

        int scale = MIN_CHAR_SCALE + Math.abs(randomizer.nextInt(MAX_CHAR_SCALE+1 - MIN_CHAR_SCALE));
        if(xOffset > WIDTH-9*scale -2)
            xOffset=WIDTH-9*scale -2;
        boolean holes = randomizer.nextLong() < 0 && scale>=4;
        int charHeigth=9*scale;
        int yOffset = 2+randomizer.nextInt(HEIGHT -3 - charHeigth);
        int thickness = CHAR_THICKNESS_MIN + Math.abs(randomizer.nextInt(CHAR_THICKNESS_MAX+1 - CHAR_THICKNESS_MIN));

        for (int i = 0; i != lines.length; i++)
        {
            Line line = lines[i];
            image.drawWavyLine(thickness, xOffset + line.getX1() * scale, yOffset + line.getY1() * scale, xOffset + line.getX2() * scale, yOffset + line.getY2() * scale, pixel, holes);
        }
    }

    /**
     * Draw a line in a random location of line_length max length
     * @param image
     * @param WHITE_LINES_LENGTH
     * @param WHITE_PIX
     */
    private void drawRandomLine(JOTCaptchaImage image, int minLength, int maxLength, JOTAbstractPixel pixel)
    {
        int length=minLength+Math.abs(randomizer.nextInt(maxLength+1-minLength));
        int x = 1+Math.abs(randomizer.nextInt(WIDTH));
        int y = 1+Math.abs(randomizer.nextInt(HEIGHT));

        int x2 = x + Math.abs(randomizer.nextInt(length));
        // calculate y2.
        int y2= x==x2?y:y+(int) Math.sqrt(Math.pow(length, 2)/Math.pow(Math.abs(x2-x),2));
        image.drawWavyLine(1, x, y, x2, y2, pixel,true);
        
    }

    /**
     * Genearte arandom code.
     * @return
     */
    private String generateCode()
    {
        String code = "";
        int length=CODE_LENGTH_MIN+Math.abs(randomizer.nextInt(CODE_LENGTH_MAX+1-CODE_LENGTH_MIN));
        for (int i = 0; i != length; i++)
        {
            int index = Math.abs(randomizer.nextInt(CHARS.length()));
            code += CHARS.charAt(index);
        }
        return code;
    }

    /**
     * generate a random color (though we check it's not too white, which would not be easy to see on white background)
     * @return
     */
    private JOTAbstractPixel getRandomColor()
    {
        // limit each to 200, to stay away from white (255)
        int red = Math.abs(randomizer.nextInt(201));
        int green = Math.abs(randomizer.nextInt(201));
        int blue = Math.abs(randomizer.nextInt(201));
        return new JOTAbstractPixel(red, green, blue, 0);
    }

    // stores a line coordinates
    static class Line
    {

        int x1 = 0;
        int x2 = 0;
        int y1 = 0;
        int y2 = 0;

        public Line(int x1, int y1, int x2, int y2)
        {
            this.x1 = x1;
            this.x2 = x2;
            this.y1 = y1;
            this.y2 = y2;
        }

        public int getX1()
        {
            return x1;
        }

        public int getX2()
        {
            return x2;
        }

        public int getY1()
        {
            return y1;
        }

        public int getY2()
        {
            return y2;
        }
    }
    
    public String getChars()
    {
        return CHARS;
    }

    public String writeToFile(JOTAbstractImageWriterInterface writer, File imageFile) throws Exception
    {
        JOTAbstractImage image=generateImage();
        OutputStream os=new FileOutputStream(imageFile);
        try
        {
           writer.writeToStream(image, os);
        }
        catch(Exception e){throw(e);}
        finally
        {
            os.flush();
            os.close();
        }
        return code;
    }

    public String writeToBrowser(JOTAbstractImageWriterInterface writer, HttpServletResponse response) throws Exception
    {
        JOTAbstractImage image=generateImage();
        OutputStream out=response.getOutputStream();
        response.setContentType(writer.getContentType());
        int dataLength=image.writeToStream(writer, out);
        response.setContentLength(dataLength);
        response.flushBuffer();
        out.close();
        return code;
    }

}
