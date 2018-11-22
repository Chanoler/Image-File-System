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
		//Store the image as a global variable
		this.image = image;
		////Update bounds to reflect the given image
		bounds = new Rectangle(0, 0, image.getWidth(), image.getHeight());
		
		//Initialize sizePow2
		sizePow2 = 0x1;
		
		/*Shift the bit in sizePow2 left by one (effectively multiplies by two) until it is greater than or equal to
		 * both of the image's dimensions
		 */
		while (sizePow2 < image.getWidth()) {
			sizePow2 = sizePow2 << 1;
		}
		
		while (sizePow2 < image.getHeight()) {
			sizePow2 = sizePow2 << 1;
		}
		
		/* Initialize the Point object, the value does not matter as this is effectively a return type for calls to the
		 * Hilbert class*/
		p = new Point(0, 0);
		
		//Current index (bit) in the image
		index = 0;
	}

	/**Implementation of a method from OutputStream, functions as specified by OutputStream
	 * @param b Byte to be written
	 * @see java.io.OutputStream#write(int)
	 */
	@Override public void write(int b) throws IOException {
		
		//Iterate for the length of a byte (in bits)
		for (int i = 0; i < Byte.SIZE; i++) {
			//Extracts the bit from the byte that needs to be written
			/* Example:
			 * Let's say we're on the 4th iteration (meaning index 3), the operation would look like:
			 * 
			 * 01101000		Starting byte
			 * 
			 * ----0110		Shift to the right by (Byte.SIZE minus Index, minus one), which is ((8-3)-1) = 4
			 * 				that brings the 4th digit from the left to be the rightmost bit
			 * 
			 * -------0		Apply the firstBitMask to get only the first bit
			 * 				CurrentBit should only ever come out to be 0x00 or 0x01
			 */
			int currentBit = firstBitMask & (b >> ((Byte.SIZE - i) - 1));
			
			//Find x/y using the Hilbert class
			Hilbert.d2xy(sizePow2, (int) (index / 3), p);
			
			//If the location given by the Hilbert class is outside of the image bounds...
			while (!bounds.contains(p)) {
				//...then keep calculating new ones until one is within the image bounds
				index++;
				Hilbert.d2xy(sizePow2, (int)(index / 3), p);
			}
			
			//Get the color of the current pixel
			int rgb = image.getRGB(p.x, p.y);
			Color c = new Color(rgb);
			
			//Select the current color and extract the first bit using a mask
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
			
			//Set the pixel color to the new color (with the new bit written)
			image.setRGB(p.x, p.y, rgb);
			
			//Increment the index
			index++;
		}
	}
	
	/**
	 * Fast forward the stream by the specified number of bytes
	 * @param length The number of bytes to skip
	 */
	public void skip(long length) {
		index += length * Byte.SIZE;
	}
	
	/**Implementation of a method from OutputStream, functions as specified by OutputStream
	 * @see java.io.OutputStream#write(byte[])
	 */
	@Override public void write(byte[] b) throws IOException {
		for (byte bt : b) {
			write(bt);
		}
	}
	
	/**Method used for modifying the last bit in the color values
	 * 
	 * @param color The R, G, or B color value to change
	 * @param bit Bit to set (1 or 0)
	 * @return Modified byte
	 */
	private int mask(int color, int bit) {
		if (bit == 0x1) {
			color = color | 0x1; //Set the last bit as 1
		} else {
			color = color & ~0x1; //Set the last bit as 0
		}
		
		return color;
	}
	
	/**Gets the number of bytes that have not been written
	 * <p>
	 * Note: this does not mean the remaining bytes will not have data
	 * @return number of bytes that have not been written
	 */
	public long freeSpace() {
		long pixels = (long) image.getWidth() * (long) image.getHeight(); //Pixel count
		
		pixels *= 3; //3 bits per pixel
		
		pixels -= index; //Subtract used space
		
		pixels /= 8; //8 bits per byte
		
		return pixels;
	}
	
	/**Gets the maximum possible amount of bytes that can be stored in the image
	 * @return maximum possible amount of bytes that can be stored
	 * 
	 * TODO return long
	 */
	public int totalSpace() {
		long pixels = (long) image.getWidth() * (long) image.getHeight(); //Pixel count
		
		pixels *= 3; //3 bits per pixel
		
		pixels /= 8; //8 bits per byte
		
		return (int) pixels;
	}
	
	/**Implementation of a method from OutputStream, functions as specified by OutputStream
	 * @see java.io.OutputStream#close()
	 */
	@Override public void close() throws IOException {
		if (freeSpace() >= Character.BYTES)
			for (int i = 0; i < Character.BYTES; i++)
				write(0);
		
		super.close();
	}

}
