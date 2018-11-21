package chan.imageIO;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import chan.util.Hilbert;

public class ImageDataOut extends OutputStream {
	

	/**The image this stream is bound to*/
	private BufferedImage image;
	/**Current position in the image*/
	private long index;
	
	/**For use in the call to the Hilbert Curve class
	 * <p>
	 * The Hilbert curve requires a dimension that is some power of two, this variable stores
	 * the power of 2 that is equal to or greater than the largest dimension of the selected image
	 */
	private int sizePow2;
	
	/**This variable acts as the return for the Hilbert class since it is passed as a reference
	 * and updated by the Hilbert class
	 */
	private Point p;
	
	/**The hilbert class can only make perfect squares whose dimensions are equal and are some power
	 * of two that is greater than one, these bounds are used to skip any positions calculated by
	 * the Hilbert class that may be outside of the image's bounds 
	 */
	private Rectangle bounds;
	
	/**Mask used in combination with the "binary AND" operator to get only the first bit from a given
	 * byte
	 */
	private static int firstBitMask = 0x1;
	
	/**
	 * Constructor for the ImageDataOut stream
	 * @param image An image containing data
	 */
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
			int currentBit = firstBitMask & (b >> ((Byte.SIZE - i) - 1));
			
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
			
			//System.out.println(x + "\t" + y);
			
			int rgb = image.getRGB(p.x, p.y);
			
			//rgb = (int)index;
			
			
			
			Color c = new Color(rgb);
			
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
			
			image.setRGB(p.x, p.y, rgb);
			
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
