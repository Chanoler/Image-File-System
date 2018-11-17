import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

//Sorry for the lack of comments. I might get around to that eventually.

public class Application {

	
	private String[] supportedTypes = {"png", "jpg", "bmp", "jpeg"};
	
	
	private JFrame frame;
	
	private JFileChooser fc = new JFileChooser();
	private JLabel lblAvailableSpace = null;
	private JLabel lblImage;
	private JButton btnSetImage;
	private JButton btnSetFile;
	private JTextArea txtrImage;
	private JTextArea txtrFile;
	private JProgressBar pbUsed;
	private JButton btnWrite; 
	private JButton btnExtract;
	private JButton btnWipe;
	
	protected File imageFile = null;
	protected BufferedImage image = null;
	
	protected File selectedFile = null;
	
	protected long totalSpace;
	protected long usedSpace;
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Application window = new Application();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public Application() {
		initialize();
	}

	private void initialize() {
		fc.setCurrentDirectory(Paths.get("").toAbsolutePath().toFile());
		
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setResizable(false);
		
		lblAvailableSpace = new JLabel("Available Space: ");
		lblAvailableSpace.setBounds(226, 214, 198, 36);
		
		frame.getContentPane().add(lblAvailableSpace);
		
		lblImage = new JLabel("Image");
		lblImage.setBorder(BorderFactory.createLineBorder(Color.black));
		lblImage.setBackground(Color.WHITE);
		lblImage.setHorizontalAlignment(SwingConstants.CENTER);
		lblImage.setIcon(null);
		lblImage.setBounds(226, 11, 198, 198);
		frame.getContentPane().add(lblImage);
		
		btnSetImage = new JButton("Set Image");
		btnSetImage.setBounds(109, 11, 107, 23);
		frame.getContentPane().add(btnSetImage);
		
		btnSetFile = new JButton("Set File");
		btnSetFile.setBounds(10, 11, 89, 23);
		frame.getContentPane().add(btnSetFile);
		
		txtrImage = new JTextArea();
		txtrImage.setBackground(SystemColor.control);
		txtrImage.setFont(new Font("Tahoma", Font.PLAIN, 11));
		txtrImage.setWrapStyleWord(true);
		txtrImage.setLineWrap(true);
		txtrImage.setText("Image: ");
		txtrImage.setBounds(10, 45, 206, 45);
		frame.getContentPane().add(txtrImage);
		
		txtrFile = new JTextArea();
		txtrFile.setWrapStyleWord(true);
		txtrFile.setText("File: ");
		txtrFile.setLineWrap(true);
		txtrFile.setFont(new Font("Tahoma", Font.PLAIN, 11));
		txtrFile.setBackground(SystemColor.menu);
		txtrFile.setBounds(10, 101, 206, 45);
		frame.getContentPane().add(txtrFile);
		
		pbUsed = new JProgressBar();
		pbUsed.setBounds(10, 236, 200, 14);
		frame.getContentPane().add(pbUsed);
		
		btnWrite = new JButton("Write");
		btnWrite.setBounds(10, 157, 89, 23);
		frame.getContentPane().add(btnWrite);
		
		btnExtract = new JButton("Extract");
		btnExtract.setBounds(109, 157, 107, 23);
		frame.getContentPane().add(btnExtract);
		
		btnWipe = new JButton("Wipe");
		btnWipe.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int choice = JOptionPane.showConfirmDialog(frame, "Are you sure you want to remove all data from this image?", "Confirm wipe", JOptionPane.YES_NO_CANCEL_OPTION);
				
				if (choice != JOptionPane.YES_OPTION)
					return;
				
				//Wipe it clean
				wipe();
			}
		});
		btnWipe.setBounds(10, 191, 89, 23);
		frame.getContentPane().add(btnWipe);
		
		
		
		btnSetImage.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				selectStorage();
			}
		});
		
		btnSetFile.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				selectFile();
			}
		});
		
		btnExtract.addActionListener(new ActionListener() {			
			@Override public void actionPerformed(ActionEvent e) {
				if (image == null) {
					JOptionPane.showMessageDialog(frame, "No image selected!", "ERROR!", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				ImageFileReader ifr = new ImageFileReader(image, imageFile.getParentFile());
				
				ArrayList<File> extractedFiles = new ArrayList<File>();
				while (ifr.eof == false) {
					try {
						extractedFiles.add(ifr.extractFile());
					} catch (IOException e1) {
						e1.printStackTrace();
						System.exit(0);
					}
				}
				
				String fileList = "Extracted files:";
				
				for (int i = 0; i < extractedFiles.size() - 1; i++)
					fileList += "\n" + extractedFiles.get(i).toPath();
				
				JOptionPane.showMessageDialog(frame, fileList, "Extraction Complete", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		
		btnWrite.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				if (image == null || selectedFile == null) {
					//Alert the user that no image / file is selected
					JOptionPane.showMessageDialog(frame, "No "+ (image == null ? (selectedFile == null ? "image or file" : "image") : "file") +" selected!", "ERROR!", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				ImageFileWriter ifw = new ImageFileWriter(image, true);
				
				try {
					boolean success = ifw.writeFile(selectedFile);
					
					if (success == false)
						JOptionPane.showMessageDialog(frame, "Not enough space", "ERROR!", JOptionPane.ERROR_MESSAGE);
				} catch (IOException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(frame, "An error occurred when writing the file:\n" + e1.toString(), "ERROR!", JOptionPane.ERROR_MESSAGE);
				}
				
				try {
					ImageIO.write(image, "png", imageFile);
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(frame, "An error occurred when saving the Image:\n" + e1.toString(), "ERROR!", JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				}
				
				analyzeSpace(image);
			}
		});
	}
	
	private void selectStorage() {
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setAcceptAllFileFilterUsed(false);
		fc.setFileFilter(new FileFilter() {
			
			@Override public String getDescription() {
				return "*.png, *.jpg, *.bmp";
			}
			
			@Override public boolean accept(File f) {
				if (f.isDirectory())
					return true;
				
				final String[] extensions = supportedTypes;
				
				String name = f.getName();
				
				for (int i = 0 ; i < extensions.length; i++) {
					if (name.endsWith(extensions[i]))
						return true;;
				}
				
				return false;
			}
		});
		fc.showOpenDialog(frame);
		
		if (fc.getSelectedFile() == null)
			return;
		
		File prevImageFile = imageFile;
		
		imageFile = fc.getSelectedFile();
		
		txtrImage.setText("Image: " + imageFile.toPath());
		
		try {
			image = ImageIO.read(imageFile);
		} catch (IOException e) {
			imageFile = prevImageFile;
			JOptionPane.showMessageDialog(frame, "Could not open " + imageFile.getName(), "Error!", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		analyzeSpace(image);
		
		//Scale image before use
		BufferedImage icon = new BufferedImage(lblImage.getWidth(), lblImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
		icon.createGraphics().drawImage(image, 0, 0, lblImage.getWidth(), lblImage.getHeight(), null);
		lblImage.setIcon(new ImageIcon(icon));
	}
	
	private void analyzeSpace(BufferedImage image) {
		long pixels = (long) image.getWidth() * (long) image.getHeight(); //Pixel count
		pixels *= 3; //3 bits per pixel
		pixels /= 8; //8 bits per byte
		totalSpace = pixels;
		
		ImageFileReader ifr = new ImageFileReader(image, imageFile.getParentFile());
		if (ifr.eof == true) {
			int result = JOptionPane.showConfirmDialog(frame, "This image must be formatted to store files, format now?", "Warning", JOptionPane.WARNING_MESSAGE);
			
			if (result == JOptionPane.YES_OPTION) {
				wipe();
			}
				
		}
		usedSpace = 0L;
		long skipLength = ifr.skipFile();
		int skips = 0;
		
		try {
			while (skipLength != 0L) {
				usedSpace += skipLength;
				skips++;
				skipLength = ifr.skipFile();
			}
		} catch (Exception e) {
			int response = JOptionPane.showConfirmDialog(frame, "Error opening file, wipe clean?", "ERROR!", JOptionPane.YES_NO_CANCEL_OPTION);
			
			if (response != JOptionPane.YES_OPTION)
				return;
			
			pbUsed.setMinimum(0);
			pbUsed.setMaximum(100);
			pbUsed.setValue(100);
			pbUsed.setIndeterminate(true);
			
			
			wipe();
					
			pbUsed.setValue(0);
			pbUsed.setIndeterminate(false);
			
			//Skip user confirming when selecting storage
			new Thread(new Runnable() {
				@Override public void run() {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					fc.approveSelection();
				}
			}).start();;
			
			selectStorage();
		}
		
		btnExtract.setText("Extract (" + skips + ")");
		
		lblAvailableSpace.setText("Free Space: " + humanReadable(totalSpace - usedSpace) + " / " + humanReadable(totalSpace));
		
		pbUsed.setMinimum(0);
		pbUsed.setMaximum((int) totalSpace);
		pbUsed.setValue((int) usedSpace);
	}
	
	private void selectFile() {
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setAcceptAllFileFilterUsed(true);
		fc.resetChoosableFileFilters();
		fc.showOpenDialog(frame);
		
		if (fc.getSelectedFile() == null)
			return;
		
		selectedFile = fc.getSelectedFile();
		
		//long fileSize;
		
		try {
			//fileSize = Files.size(selectedFile.toPath());
			Files.size(selectedFile.toPath());
		} catch (IOException e) {
			JOptionPane.showMessageDialog(frame, "Could not open the file " + selectedFile.toString(), "ERROR!", JOptionPane.ERROR_MESSAGE); 
		}
		
		txtrFile.setText("File: " + selectedFile.toPath());
	}
	
	private void wipe() {
		try {
			ImageDataOut ido = new ImageDataOut(image);
			long space = ido.freeSpace();

			ido.write(0xDB);
			space--;
			
			JProgressBar pb = new JProgressBar();
			pb.setMinimum(0);
			pb.setMaximum((int)space);
			
			Long spaceRef = Long.valueOf(space);
			
			Thread thr = new Thread(new Runnable(){public void run() {
				try {
					long space = spaceRef.longValue();
					while (space > 0) {
						ido.write(0);
						space--;
						pb.setValue(pb.getMaximum() - (int)space);
					}
					
					SwingUtilities.getWindowAncestor(pb).setVisible(false);
				} catch (Exception e) {
					
				}
			}});
			
			thr.start();

			JOptionPane.showMessageDialog(frame, pb);
			
			thr.join();
			
			System.out.println("done");
			
			ido.close();

			ImageIO.write(image, "png", imageFile);
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(frame, "Error wiping file!", "ERROR!", JOptionPane.ERROR_MESSAGE);
		}
		
		analyzeSpace(image);
	}
	
	private String humanReadable(long space) {
		String units = "kMGT";
		
		double size = (double) space;
		
		if (size < 1024) {
			return String.format("%.2f bytes", size);
		}
		
		for (int i = 0; i < units.length(); i++) {
			size /= 1024;
			if (size < 1024) {
				return String.format("%.2f", size) + " " + units.charAt(i) + "iB";
			}
		}
		
		return String.format("%.2f", size) + " " + units.charAt(units.length() - 1);
	}
}
