package chan.imageIO;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageFileReader {
	/* 
	 * File system is as follows:
	 * Bytes		Data
	 * 1			Flags
	 * 2 (short)	File Name Length
	 * #			File Name
	 * 4 (int)		File Data Length
	 * #			File Data
	 */
	
	private File dir;
	
	private ImageDataIn idi;
	private DataInputStream dis;
	private boolean eof = false;
	
	public ImageFileReader(BufferedImage image, File outputDirectory) {
		dir = outputDirectory;
		
		idi = new ImageDataIn(image);
		dis = new DataInputStream(idi);
		
		try {
			byte b = dis.readByte();
			eof = b != (byte) 0xDB;
		} catch (IOException e) {
			eof = true;
			e.printStackTrace();
		}
	}
	
	/**
	 * @return number of bytes skipped
	 * @throws IOException
	 */
	public long skipFile() {
		if (eof)
			return 0L;
		
		try {
			short nameLength = (short) (dis.readShort() / (short) Character.BYTES);
		
			if (nameLength == 0) {
				eof = true;
				return 0L;
			}
		
		
			idi.skip(nameLength * Character.BYTES);
		
			int dataLength = dis.readInt();
		
			idi.skip(dataLength);
		
			return (nameLength * Character.BYTES) + dataLength + Short.BYTES + Integer.BYTES;
		} catch (IOException e) {
			eof = true;
			return 0L;
		}
	}
	
	/**
	 * Extracts the next available file in the image, if no file is available or the end of the
	 * stream has been reached, returns null
	 * <p>
	 * TODO Bug causing files that already exist to be overwritten  
	 * @return Path of the extracted file
	 * @throws IOException
	 */
	public File extractFile() throws IOException {
		if (eof)
			return null;
		
		short nameLength = (short) (dis.readShort() / (short) Character.BYTES);
		
		if (nameLength == 0) {
			eof = true;
			return null;
		}
		
		char[] chars = new char[nameLength];
		
		for (int i = 0; i < nameLength; i++) {
			chars[i] = dis.readChar();
		}
		String name = new String(chars);
		
		File file = new File(dir, name);
		
		byte[] data = new byte[dis.readInt()];
		
		for (int i = 0; i < data.length; i++) {
			data[i] = dis.readByte();
		}
		
		FileOutputStream fos = new FileOutputStream(file);
		
		fos.write(data);
		
		fos.close();
		
		return file;
	}
	
	public boolean isEof() {
		return eof;
	}
	
	public void close() {
		try {
			dis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
