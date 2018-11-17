import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Tester {
	
	public static final File in = new File("towrite.mp4");
	
	public static void main(String[] args) {
		//write();
		//System.out.println();
		//read();
		
		
		//writeFile();
		readFile();
	}
	
	@SuppressWarnings("unused")
	private static void write() {
		BufferedImage image;
		try {
			image = ImageIO.read(new File("test.png"));
		} catch (IOException e) {
			image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);
		}
		
		
		ImageDataOut ido = new ImageDataOut(image);
		DataOutputStream dos = new DataOutputStream(ido);
		
		try {
			dos.writeInt(10000);
			dos.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		
		
		try {
			ImageIO.write(image, "png", new File("testwritten.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unused")
	private static void read() {
		BufferedImage image;
		try {
			image = ImageIO.read(new File("testwritten.png"));
		} catch (IOException e) {
			image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);
		}
		
		
		ImageDataIn idi = new ImageDataIn(image);
		DataInputStream dis = new DataInputStream(idi);
		
		try {
			System.out.println(dis.readInt());
			dis.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}
	
	@SuppressWarnings("unused")
	private static void writeFile() {
		BufferedImage image;
		try {
			image = ImageIO.read(new File("test.png"));
		} catch (IOException e) {
			image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);
		}
		
		
		ImageFileWriter ifw = new ImageFileWriter(image, false);
		
		try {
			ifw.writeFile(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			ImageIO.write(image, "png", new File("testwritten.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("done");
	}
	
	private static void readFile() {
		BufferedImage image;
		try {
			image = ImageIO.read(new File("testwritten.png"));
		} catch (IOException e) {
			image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);
		}
		
		
		ImageFileReader ifw = new ImageFileReader(image, null);
		
		try {
			ifw.extractFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("done");
	}
}
