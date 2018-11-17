import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class ImageDataIn extends InputStream {
	
	BufferedImage image;
	long index;
	int currentBit;
	int x, y;
	int rgb;
	Color c;
	long resetIndex;
	int sizePow2;
	Point p;
	Rectangle bounds;
	
	private static int firstBitMask = 0x1;
	
	public ImageDataIn(BufferedImage image) {
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
	
	@Override public int read() throws IOException {
		
		int out = 0x0;
		
		for (int i = 0; i < Byte.SIZE; i++) {
			
			/*
			//Find x/y
			Hilbert.d2xy(sizePow2, (int) (index / 3), p);
			
			while (!bounds.contains(p)) {
				index++;
				Hilbert.d2xy(sizePow2, (int) (index / 3), p);
			}
			
			x = p.x;
			y = p.y;*/
			
			//Find x/y
			Hilbert.d2xy(sizePow2, (int) (index / 3), p);
			
			while (!bounds.contains(p)) {
				//System.err.println("read  " + p.toString() + " skip");
				index++;
				Hilbert.d2xy(sizePow2, (int)(index / 3), p);
			}
			
			//System.out.println("read  " + p.toString() + "\t" + currentBit);
			
			/*try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				System.err.println("Sleeping interrupted");
			}*/
			
			x = p.x;
			y = p.y;
			
			
			//Get color
			rgb = image.getRGB(x, y);
			c = new Color(rgb);
			
			//Get correct color bit
			switch ((int) (index % 3)) {
				case 0:
					currentBit = c.getRed() & firstBitMask;
					break;
				case 1:
					currentBit = c.getGreen() & firstBitMask;
					break;
				case 2:
					currentBit = c.getBlue() & firstBitMask;
					break;
			}
			
			//Shift return
			out = out << 1;
			
			//Add current bit
			out = out | currentBit;
			
			//Increment index
			index++;
			
			//System.out.print(currentBit + " ");
		}
		
		return out;
	}
	
	public long skip(long length) {
		index += Byte.SIZE * length;
		return 0L;
	}
	
	@Override public int read(byte[] b) throws IOException {
		int i;
		
		for (i = 0; i < b.length; i++) {
			b[i] = (byte) read();
		}
		
		return i;
	}
	
	public int available() {
		long pixels = (long) image.getWidth() * (long) image.getHeight(); //Pixel count
		
		pixels *= 3; //3 bits per pixel
		
		pixels -= index; //Subtract used space
		
		pixels /= 8; //8 bits per byte
		
		return (int) pixels;
	}
	
	public int totalSpace() {
		long pixels = (long) image.getWidth() * (long) image.getHeight(); //Pixel count
		
		pixels *= 3; //3 bits per pixel
		
		pixels /= 8; //8 bits per byte
		
		return (int) pixels;
	}
	
	@Override public boolean markSupported() {
		return true;
	}
	
	@Override public synchronized void reset() throws IOException {
		index = resetIndex;
	}
	
	@Override public synchronized void mark(int readlimit) {
		resetIndex = index;
	}

}
