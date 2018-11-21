package chan.imageIO;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import chan.util.Hilbert;

public class ImageDataOut extends OutputStream {
	
	BufferedImage image;
	long index;
	int currentBit;
	int x, y;
	int rgb;
	Color c;
	int sizePow2;
	Point p;
	Rectangle bounds;
	
	private static int firstBitMask = 0x1;
	
	public ImageDataOut(BufferedImage image) {
		
		this.image = image;
		
		bounds = new Rectangle(0, 0, image.getWidth(), image.getHeight());
		
		sizePow2 = 0x1;
		
		while (sizePow2 < image.getWidth()) {
			sizePow2 = sizePow2 << 1;
		}
		
		while (sizePow2 < image.getHeight()) {
			sizePow2 = sizePow2 << 1;
		}
		
		p = new Point(0, 0);
		
		index = 0;
	}

	@Override public void write(int b) throws IOException {
		
		for (int i = 0; i < Byte.SIZE; i++) {
			//Get bit from left to right
			currentBit = firstBitMask & (b >> ((Byte.SIZE - i) - 1));
			
			//Find x/y
			Hilbert.d2xy(sizePow2, (int) (index / 3), p);
			
			while (!bounds.contains(p)) {
				//System.err.println("write " + p.toString() + "skip");
				index++;
				Hilbert.d2xy(sizePow2, (int)(index / 3), p);
			}
			
			//System.out.println("write " + p.toString() + "\t" + currentBit);
			
			/*try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				System.err.println("Sleeping interrupted");
			}*/
			
			x = p.x;
			y = p.y;
			
			//System.out.println(x + "\t" + y);
			
			rgb = image.getRGB(x, y);
			
			//rgb = (int)index;
			
			
			
			c = new Color(rgb);
			
			switch ((int) (index % 3)) {
				case 0:
					rgb = new Color(mask(c.getRed(), currentBit), c.getGreen(), c.getBlue()).getRGB();
					break;
				case 1:
					rgb = new Color(c.getRed(), mask(c.getGreen(), currentBit), c.getBlue()).getRGB();
					break;
				case 2:
					rgb = new Color(c.getRed(), c.getGreen(), mask(c.getBlue(), currentBit)).getRGB();
					break;
			}
			
			image.setRGB(x, y, rgb);
			
			index++;
			
			//System.out.print(currentBit + " ");
		}
	}
	
	public void skip(long length) {
		index += length * Byte.SIZE;
	}
	
	@Override public void write(byte[] b) throws IOException {
		for (byte bt : b) {
			write(bt);
		}
	}
	
	private int mask(int color, int bit) {
		if (bit == 0x1) {
			color = color | 0x1; //Add last bit
		} else {
			color = color & ~0x1; //Keep all but last bit
		}
		
		return color;
	}
	
	public long freeSpace() {
		long pixels = (long) image.getWidth() * (long) image.getHeight(); //Pixel count
		
		pixels *= 3; //3 bits per pixel
		
		pixels -= index; //Subtract used space
		
		pixels /= 8; //8 bits per byte
		
		return pixels;
	}
	
	public int totalSpace() {
		long pixels = (long) image.getWidth() * (long) image.getHeight(); //Pixel count
		
		pixels *= 3; //3 bits per pixel
		
		pixels /= 8; //8 bits per byte
		
		return (int) pixels;
	}
	
	@Override public void close() throws IOException {
		if (freeSpace() >= Character.BYTES)
			for (int i = 0; i < Character.BYTES; i++)
				write(0);
		
		super.close();
	}

}
