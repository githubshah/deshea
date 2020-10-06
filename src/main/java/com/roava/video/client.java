package com.roava.video;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;

public class client {
    public static void main(String[] args) throws Exception{
        Socket s = new Socket("localhost", 9999);
        DataOutputStream dout = new DataOutputStream(s.getOutputStream());

        BufferedImage bImage = ImageIO.read(new File("/Users/daffolapmac-156/Downloads/image/caller.png"));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(bImage, "png", bos );
        byte [] data = bos.toByteArray();

        dout.writeInt(data.length);
        if (data.length > 0) {
            dout.write(data, 0, data.length);
        }

        s.close();

    }

}
