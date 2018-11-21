package chan.debug;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.File;

import javax.imageio.ImageIO;

import chan.imageIO.ImageDataIn;
import chan.imageIO.ImageFileReader;

/**
 * Class used for writing code to test various parts of the application
 * intended only for use by the original developer
 * @author Christopher J. Chauvin
 *
 */
public class Debug {
	public static void main (String[] args) {
		try {
			new Debug().run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private Long data = 0L;
	
	public void run() throws Exception {
		//Hard-coded test file
		BufferedImage image = ImageIO.read(new File("C:/Users/Christopher/Desktop/TEST.jpg"));
		
		//----------Read Test----------
		//Reads the files in the image and displays them as plain text
		DataInputStream dis = new DataInputStream(new ImageDataIn(image));

		data = (long) dis.readByte();
		print();
		data = (long) dis.readShort();
		print();
		while (data > 0) {
			System.out.print(dis.readChar());
			data--;
		}
		System.out.println();
		
		data = (long) dis.readInt();
		print();
		if (data > 1024L)
			data = 1024L;
		
		while (data > 0) {
			System.out.printf("%#010x\n", dis.readInt());
			data -= Integer.BYTES;
		}
		
		System.out.println("done");
		
		dis.close();
		
		//Attempts to extract a file from the image and returns its path
		ImageFileReader ifr = new ImageFileReader(image, new File("C:/Users/Christopher/Desktop/TEST.jpg").getParentFile());
		System.out.println(ifr.isEof());
		System.out.println(ifr.extractFile().toPath());
	}
	
	private void print() {
		System.out.printf("%#010x (%d)\n", data, data);
	}
}
