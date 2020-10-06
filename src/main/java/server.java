import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class server {
    public static void main(String[] args) throws Exception{
        ServerSocket s = new ServerSocket(9999);
        Socket ss = s.accept();
        System.out.println("connected");

        DataInputStream din = new DataInputStream(ss.getInputStream());

        int len = din.readInt();
        byte[] data = new byte[len];
        if (len > 0) {
            din.readFully(data);
        }

        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        BufferedImage bImage2 = ImageIO.read(bis);
        ImageIO.write(bImage2, "png", new File("output.png") );
        System.out.println("image created");
    }
}
