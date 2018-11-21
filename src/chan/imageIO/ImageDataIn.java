package chan.imageIO;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import chan.util.Hilbert;

public class ImageDataIn extends InputStream {
	
	/**The image this stream is bound to*/
	BufferedImage image;
	/**Current position in the image*/
	long index;
	
	/**Index used for the mark/reset functionality of InputStream*/
	long markIndex;
	
	/**For use in the call to the Hilbert Curve class
	 * <p>
	 * The Hilbert curve requires a dimension that is some power of two, this variable stores
	 * the power of 2 that is equal to or greater than the largest dimension of the selected image
	 */
	int sizePow2;
	
	/**This variable acts as the return for the Hilbert class since it is passed as a reference
	 * and updated by the Hilbert class
	 */
	Point p;
	
	/**The hilbert class can only make perfect squares whose dimensions are equal and are some power
	 * of two that is greater than one, these bounds are used to skip any positions calculated by
	 * the Hilbert class that may be outside of the image's bounds 
	 */
	Rectangle bounds;
	
	/**Mask used in combination with the "binary AND" operator to get only the first bit from a given
	 * byte
	 */
	private static int firstBitMask = 0x1;
	
	/**
	 * Constructor for the ImageDataIn stream
	 * @param image An image containing data
	 */
	public ImageDataIn(BufferedImage image) {
		//Store the image as a global variable
		this.image = image;
		//Update bounds to reflect the given image
		bounds = new Rectangle(0, 0, image.getWidth(), image.getHeight());
		
		//Set size as 1
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
	
	@Override public int read() throws IOException {
		//Byte to be returned later
		int out = 0x0;
		
		//Iterate for the length of a byte (in bits)
		for (int i = 0; i < Byte.SIZE; i++) {
			
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
			int currentBit = 0;
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
			
			//Push the previously extracted bits left one to make room for the new one
			out = out << 1;
			
			//Add the new bit to the previously extracted bits
			out = out | currentBit;
			
			//Increment index
			index++;
		}
		//Continues until one byte is extracted
		
		//Return the extracted byte
		return out;
	}
	
	/**
	 * Fast forward the stream by the specified number of bytes
	 * @param length The number of bytes to skip
	 */
	public long skip(long length) {
		index += Byte.SIZE * length;
		return 0L;
	}
	
	//Implementation of a method from InputStream, functions as specified by InputStream
	@Override public int read(byte[] b) throws IOException {
		int i;
		
		for (i = 0; i < b.length; i++) {
			b[i] = (byte) read();
		}
		
		return i;
	}
	
	//Gets the number of available bytes minus the amount of free space
	public int available() {
		long pixels = (long) image.getWidth() * (long) image.getHeight(); //Pixel count
		
		pixels *= 3; //3 bits per pixel
		
		pixels -= index; //Subtract used space
		
		pixels /= 8; //8 bits per byte
		
		return (int) pixels;
	}
	
	//Gets the maximum possible amount of bytes that can be stored in the image
	public int totalSpace() {
		long pixels = (long) image.getWidth() * (long) image.getHeight(); //Pixel count
		
		pixels *= 3; //3 bits per pixel
		
		pixels /= 8; //8 bits per byte
		
		return (int) pixels;
	}
	
	//Implementation of a method from InputStream, functions as specified by InputStream
	@Override public boolean markSupported() {
		return true;
	}
	
	//Implementation of a method from InputStream, functions as specified by InputStream
	@Override public synchronized void reset() throws IOException {
		index = markIndex;
	}
	
	//Implementation of a method from InputStream, functions as specified by InputStream
	@Override public synchronized void mark(int readlimit) {
		markIndex = index;
	}

}
