package chan.imageIO;
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;

public class ImageFileWriter {
	/* 
	 * File system is as follows:
	 * Bytes		Data
	 * 1			Flags
	 * 2 (short)	File Name Length
	 * #			File Name
	 * 4 (int)		File Data Length
	 * #			File Data
	 */
	
	private ImageDataOut ido;
	private DataOutputStream dos;
	
	public ImageFileWriter(BufferedImage image, boolean append) {
		ido = new ImageDataOut(image);
		dos = new DataOutputStream(ido);
		
		try {
			ImageDataIn imageDataIn = new ImageDataIn(image);
			
			if (imageDataIn.read() != 0xDB)
				append = false;
			
			imageDataIn.close();
			
			dos.writeByte(0xDB);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (append) {
			ImageFileReader ifr = new ImageFileReader(image, null);
			long totalLength = 0L;
			long skipLength = ifr.skipFile();
			
			while (skipLength != 0L) {
				totalLength += skipLength;
				skipLength = ifr.skipFile();
			}
			
			ido.skip(totalLength);
			
			/*try {
				for (int i = 0; i < totalLength; i++)
					dos.write(ifr.idi.read());
				
			} catch (IOException e) {
				e.printStackTrace();
			}*/
		}
	}
	
	public boolean writeFile(File file) throws IOException {
		BasicFileAttributes bfa = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
		//If no error is thrown, it is safe
		
		long requiredSpace = Short.BYTES + (file.getName().length() * Character.BYTES) + Integer.BYTES + (bfa.size() * Byte.BYTES);
		
		if (ido.freeSpace() < requiredSpace)
			return false;
		
		byte[] buffer = new byte[(int) bfa.size()];
		
		FileInputStream fis = new FileInputStream(file);
		
		for (int i = 0; i < buffer.length; i++) {
			buffer[i] = (byte) fis.read();
		}
		
		if (fis.read() != -1) {
			fis.close();
			throw new IOException();
		}
		
		fis.close();
		
		//Get file name
		String filename = file.getName();
			
		//Write name length
		dos.writeShort((filename.length() * Character.BYTES));
		
		dos.writeChars(filename);
			
		//Write data length
		dos.writeInt(buffer.length);
			
		//Write file data
		dos.write(buffer);
		
		return true;
	}
	
	public void close() {
		try {
			dos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
