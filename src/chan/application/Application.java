package chan.application;
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

import chan.imageIO.ImageDataOut;
import chan.imageIO.ImageFileReader;
import chan.imageIO.ImageFileWriter;

//Sorry for the lack of comments. I might get around to that eventually.

/**
 * Main class for the program, controls frontend and allows the user to interface with the program
 * @author Christopher J. Chauvin
 * @version 1.0
 * @since   1.0
 *
 */
public class Application {

	//Prevents the use of photo types that may not be recognized by the program
	/**List if image types known to work with this program*/
	private String[] supportedTypes = {"png", "jpg", "bmp", "jpeg"};
	
	/**The application window*/
	private JFrame frame;
	
	/**FileChooser used in selecting files and images, only needs one instance for both cases*/
	private JFileChooser fc = new JFileChooser();
	
	//Various labels, buttons, and bars that make up the UI of the application
	/**Text label showing available space in human-readable format*/
	private JLabel lblAvailableSpace = null;
	/**Displays a preview of the selected image, not corrected for aspect ratio, may fix later*/
	private JLabel lblImage;
	/**Button used to select an Image*/
	private JButton btnSetImage;
	/**Button used to select a File*/
	private JButton btnSetFile;
	/**Displays the file path of the selected Image*/
	private JTextArea txtrImage;
	/**Displays the file path of the selected File*/
	private JTextArea txtrFile;
	/**Progress bar to represent used space*/
	private JProgressBar pbUsed;
	/**Button that, when clicked, writes the selected file to the selected image*/
	private JButton btnWrite;
	/**Button that, when clicked, extracts all files from the selected image*/
	private JButton btnExtract;
	/**Button that, when clicked, writes the selected file to the selected image*/
	private JButton btnWipe;
	
	//Variables related to the selected Image
	/**The Image that was selected*/
	private File imageFile = null;
	private BufferedImage image = null;
	
	//Variables related to the selected File
	/**The File that was selected*/
	private File selectedFile = null;
	
	//For the Progress Bar
	/**Measure of the total amount of space available in the selected image*/
	private long totalSpace;
	/**Measure of the amount of used space in the selected image*/
	private long usedSpace;
	
	/**
	 * Creates the window, calls the Application constructor
	 * @param args
	 */
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
	
	/**
	 * Calls the Initialize method, not sure why this is here but the program works so I'm not touching it.
	 */
	private Application() {
		initialize();
	}

    
	/**
	 * Initializes the various labels, buttons, and whatnot
	 * <p>
	 * The button functions defined later in this method handle 
	 * the way the frontend talks to the main program, not all
	 * of them are simple method calls so don't skim over the
	 * button initialization when debugging.
	 */
	private void initialize() {
		//Set default directory for the file chooser
		fc.setCurrentDirectory(Paths.get("").toAbsolutePath().toFile());
		
		//Initialize the window
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setResizable(false);
		
		//Init available space label
		lblAvailableSpace = new JLabel("Available Space: ");
		lblAvailableSpace.setBounds(226, 214, 198, 36);
		frame.getContentPane().add(lblAvailableSpace);
		
		//Init image preview
		lblImage = new JLabel("Image");
		lblImage.setBorder(BorderFactory.createLineBorder(Color.black));
		lblImage.setBackground(Color.WHITE);
		lblImage.setHorizontalAlignment(SwingConstants.CENTER);
		lblImage.setIcon(null);
		lblImage.setBounds(226, 11, 198, 198);
		frame.getContentPane().add(lblImage);
		
		//Init "Set Image" button, function implemented later in this method
		btnSetImage = new JButton("Set Image");
		btnSetImage.setBounds(109, 11, 107, 23);
		frame.getContentPane().add(btnSetImage);
		
		//Init "Set File" button, function implemented later in this method
		btnSetFile = new JButton("Set File");
		btnSetFile.setBounds(10, 11, 89, 23);
		frame.getContentPane().add(btnSetFile);
		
		//Init label that displays the selected image path
		txtrImage = new JTextArea();
		txtrImage.setBackground(SystemColor.control);
		txtrImage.setFont(new Font("Tahoma", Font.PLAIN, 11));
		txtrImage.setWrapStyleWord(true);
		txtrImage.setLineWrap(true);
		txtrImage.setText("Image: ");
		txtrImage.setBounds(10, 45, 206, 45);
		txtrImage.setEditable(false);
		frame.getContentPane().add(txtrImage);

		//Init label that displays the selected file path
		txtrFile = new JTextArea();
		txtrFile.setWrapStyleWord(true);
		txtrFile.setText("File: ");
		txtrFile.setLineWrap(true);
		txtrFile.setFont(new Font("Tahoma", Font.PLAIN, 11));
		txtrFile.setBackground(SystemColor.menu);
		txtrFile.setBounds(10, 101, 206, 45);
		txtrFile.setEditable(false);
		frame.getContentPane().add(txtrFile);
		
		//Init progress bar
		pbUsed = new JProgressBar();
		pbUsed.setBounds(10, 236, 200, 14);
		frame.getContentPane().add(pbUsed);
		
		//Init Write Button, function implemented later in this method
		btnWrite = new JButton("Write");
		btnWrite.setBounds(10, 157, 89, 23);
		frame.getContentPane().add(btnWrite);
		
		//Init Extract Button, function implemented later in this method
		btnExtract = new JButton("Extract");
		btnExtract.setBounds(109, 157, 107, 23);
		frame.getContentPane().add(btnExtract);
		
		//Init Wipe button and implement functionality
		btnWipe = new JButton("Wipe");
		btnWipe.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//Ask user to confirm deletion of stored data
				int choice = JOptionPane.showConfirmDialog(frame, "Are you sure you want to remove all data from this image?", "Confirm wipe", JOptionPane.YES_NO_CANCEL_OPTION);
				
				//If anything but "Yes" is chosen, cancel the operation
				if (choice != JOptionPane.YES_OPTION)
					return;
				
				//Wipe all stored data from the image
				wipe();
			}
		});
		btnWipe.setBounds(10, 191, 89, 23);
		frame.getContentPane().add(btnWipe);
		
		
		//Implement functionality of the "Set Image" button
		btnSetImage.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				selectStorage();
			}
		});
		
		//Implement functionality of the "Set File" button
		btnSetFile.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				selectFile();
			}
		});
		
		//Implement functionality of the "Extract" button
		btnExtract.addActionListener(new ActionListener() {			
			@Override public void actionPerformed(ActionEvent e) {
				//Cancel operation and inform user if no image  is selected
				if (image == null) {
					JOptionPane.showMessageDialog(frame, "No image selected!", "ERROR!", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				//Initialize the ImageFileReader
				ImageFileReader ifr = new ImageFileReader(image, imageFile.getParentFile());
				
				//Extract all files from the image, keeping a list of them to display to the user
				ArrayList<File> extractedFiles = new ArrayList<File>();
				while (ifr.isEof() == false) {
					try {
						//The .extractFile() method handles the I/O and returns a File object
						extractedFiles.add(ifr.extractFile());
					} catch (IOException e1) {
						e1.printStackTrace();
						System.exit(0);
					}
				}
				
				//Build a string listing the extracted files
				String fileList = "Extracted files:";
				
				for (int i = 0; i < extractedFiles.size() - 1; i++)
					fileList += "\n" + extractedFiles.get(i).toPath();
				
				//Show the list of extracted files to the user
				JOptionPane.showMessageDialog(frame, fileList, "Extraction Complete", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		
		//Implement functionality of the "Write" button
		btnWrite.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				//Check that an image and file are selected
				if (image == null || selectedFile == null) {
					//Inform the user of which (Image, File, or both) is not selected
					JOptionPane.showMessageDialog(frame, "No "+ (image == null ? (selectedFile == null ? "image or file" : "image") : "file") +" selected!", "ERROR!", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				//Create an instance of the ImageFileWriter
				ImageFileWriter ifw = new ImageFileWriter(image, true);
				
				//Attempt to write the selected file to the selected image
				try {
					//Tries to write file to image, 
					boolean success = ifw.writeFile(selectedFile);
					
					//False is only returned if there isn't enough space, other errors are handled by throwing an exception.
					if (success == false)
						//Inform the user that there was not enough space to store the selected file
						JOptionPane.showMessageDialog(frame, "Not enough space", "ERROR!", JOptionPane.ERROR_MESSAGE);
					
					
				} catch (IOException e1) {
					//Catch and display any exception that occurred during the above operation
					e1.printStackTrace();
					JOptionPane.showMessageDialog(frame, "An error occurred when writing the file:\n" + e1.toString(), "ERROR!", JOptionPane.ERROR_MESSAGE);
				}
				
				try {
					//Replace the image on the disk with the updated image
					ImageIO.write(image, "png", imageFile);
				} catch (IOException e1) {
					//Catch and display any exception that occurred during the above operation
					JOptionPane.showMessageDialog(frame, "An error occurred when writing the Image to the disk:\n" + e1.toString(), "ERROR!", JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				}
				
				//Update the usedSpace variable, progress bar, etc.
				analyzeSpace(image);
			}
		});
	}
	
	/**The functionality of the "Set Image" button*/
	private void selectStorage() {
		//Some setup before displaying the FileChooser to the user
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setAcceptAllFileFilterUsed(false);
		//Some setup before displaying the FileChooser to the user
		fc.setFileFilter(new FileFilter() {
			
			//Create description using the supportedTypes list 
			@Override public String getDescription() {
				String description = "";
				
				//Iterate through the supported types, adding them to the description
				for (String type : supportedTypes) {
					description += ", *." + type;
				}
				
				//Returns the description
				return description.substring(2);
			}
			
			//Controls which files are displayed by the FileChooser
			@Override public boolean accept(File f) {
				//Show directories
				if (f.isDirectory())
					return true;
				
				//Reference to the supportedTypes array
				final String[] extensions = supportedTypes;
				
				//Get name of file to be processed
				//Bigfix: Set letters to lower case to prevent a file being declined based on capitalization
				String name = f.getName().toLowerCase();
				
				//Check if the file's extension matches any of our supported types
				for (int i = 0 ; i < extensions.length; i++) {
					if (name.endsWith(extensions[i]))
						return true;
				}
				
				return false;
			}
		});
		
		//Show the FileChooser to the user
		fc.showOpenDialog(frame);
		
		//Cancel operation if no file is selected
		if (fc.getSelectedFile() == null)
			return;
		
		//Keep a copy of the previously selected image in case the newly selected image has an error
		File prevImageFile = imageFile;
		
		//Get the image chosen by the user
		imageFile = fc.getSelectedFile();
		
		//Keep a copy of the previously selected image path in case the newly selected image has an error
		String prevImagePath = txtrImage.getText();
		
		//Update the Image Path text area to reflect the new image	
		txtrImage.setText("Image: " + imageFile.toPath());
		
		//Try to read the new image
		try {
			image = ImageIO.read(imageFile);
		} catch (IOException e) {
			//Reverse any performed operations if the newly selected image has an error being read
			imageFile = prevImageFile;
			txtrImage.setText(prevImagePath);
			//Inform the user of the error
			JOptionPane.showMessageDialog(frame, "Could not open " + imageFile.getName(), "Error!", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		//Update the usedSpace variable and progress bar
		analyzeSpace(image);
		
		//Scale image before use, scaling is not proportional, may fix later
		BufferedImage icon = new BufferedImage(lblImage.getWidth(), lblImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
		icon.createGraphics().drawImage(image, 0, 0, lblImage.getWidth(), lblImage.getHeight(), null);
		lblImage.setIcon(new ImageIcon(icon));
	}
	
	/**Method used to analyze the space available in the provided image and automatically updates
	 * the totalSpace, usedSpace, usedSpace ProgressBar, and number of files already stored 
	 * @param image
	 */
	private void analyzeSpace(BufferedImage image) {
		//Calculate the amount of space available in the image
		long pixels = (long) image.getWidth() * (long) image.getHeight(); //Pixel count
		totalSpace = pixels; //Starting point for space calculation
		totalSpace *= 3; //3 bits per pixel
		totalSpace /= 8; //8 bits per byte
		
		//Begin usedSpace calculation
		ImageFileReader ifr = new ImageFileReader(image, imageFile.getParentFile());
		//Check if file needs to be formatted
		if (ifr.isEof() == true) {
			int result = JOptionPane.showConfirmDialog(frame, "This image must be formatted to store files, format now?", "Warning", JOptionPane.WARNING_MESSAGE);
			
			if (result == JOptionPane.YES_OPTION) {
				wipe();
			}
				
		}
		usedSpace = 0L;
		
		//Setup for next operation
		long skipLength = ifr.skipFile();
		int skips = 0;
		
		//Count the number of files skipped and how long each one was for use in the usedSpace calculation
		try {
			while (skipLength != 0L) {
				usedSpace += skipLength;
				skips++;
				skipLength = ifr.skipFile();
			}
		} catch (Exception e) {
			//If the file is corrupt, prompt the user and ask if they want to format the image
			int response = JOptionPane.showConfirmDialog(frame, "Error opening file, wipe clean?", "ERROR!", JOptionPane.YES_NO_CANCEL_OPTION);
			
			//If anything that isn't "Yes" is selected, cancel the operation
			if (response != JOptionPane.YES_OPTION)
				return;
			
			//Set the usedSpace progress bar as indeterminate while Image is wiped
			pbUsed.setMinimum(0);
			pbUsed.setMaximum(100);
			pbUsed.setValue(100);
			pbUsed.setIndeterminate(true);
			
			//Remove all file contents from the image
			wipe();
					
			//Update the progress bar
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
			
			//Runs the selectStorage method to "naturally" select the file again which restarts the space calculation
			//The above thread cancels the fileChooser automatically
			selectStorage();
		}
		
		//Update the Extract button to show how many files can be extracted
		btnExtract.setText("Extract (" + skips + ")");
		
		//Update the label displaying the amount of free spece available in the image
		lblAvailableSpace.setText("Free Space: " + humanReadable(totalSpace - usedSpace) + " / " + humanReadable(totalSpace));
		
		//TODO If totalSpace is greater than Integer.MAX_VALUE the rounding will cause a non-fatal error
		//Update the Used Space Progress Bar
		pbUsed.setMinimum(0);
		pbUsed.setMaximum((int) totalSpace);
		pbUsed.setValue((int) usedSpace);
	}
	
	/**The functionality of the "Set File" button*/
	private void selectFile() {
		//Some setup before displaying the FileChooser to the user
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setAcceptAllFileFilterUsed(true);
		fc.resetChoosableFileFilters();
		//Show the file chooser to the user
		fc.showOpenDialog(frame);
		
		//Cancel operation if no file is selected
		if (fc.getSelectedFile() == null)
			return;
		
		//Keep a copy of the previously selected file in case the new file has an error being read
		File previousFile = selectedFile;
		
		//Update the variable selectedFile to the newly selected file 
		selectedFile = fc.getSelectedFile();
		
		//Attempt to check the file's size to verify read privileges
		try {
			Files.size(selectedFile.toPath());
		} catch (IOException e) {
			//Reverse any changes made in the event that the file could not be read
			selectedFile = previousFile;
			//Inform the user of the error
			JOptionPane.showMessageDialog(frame, "Could not open the file " + selectedFile.toString(), "ERROR!", JOptionPane.ERROR_MESSAGE); 
		}
		
		//Update the File Path text area to reflect the new File
		txtrFile.setText("File: " + selectedFile.toPath());
	}
	
	/**
	 * Wipe all contents from the selected Image
	 * <p>
	 * This operation cannot be undone
	 */
	private void wipe() {
		try {
			//Create an image writer
			ImageDataOut ido = new ImageDataOut(image);
			//How much space needs to be formatted
			long space = ido.freeSpace();
			
			//Write the database tag to the image
			ido.write(0xDB);
			space--;
			
			//Create a progress bar to show wiping progress in a new window
			JProgressBar pb = new JProgressBar();
			pb.setMinimum(0);
			pb.setMaximum((int)space);
			
			Long spaceRef = Long.valueOf(space);
			
			//Create a thread that wipes the image and updates the progress bar
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
			
			//Start the wiping thread
			thr.start();

			//Show the progress bar for the wiping in a popup window
			JOptionPane.showMessageDialog(frame, pb);
			
			//Wait for the wiping to be completed
			thr.join();
			
			//Debug
			System.out.println("done");
			
			//Close the image data writer
			ido.close();

			//Write the newly wiped file to the disk
			ImageIO.write(image, "png", imageFile);
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(frame, "Error wiping file!", "ERROR!", JOptionPane.ERROR_MESSAGE);
		}
		
		//Recalculate available/used space and update relevant UI components
		analyzeSpace(image);
	}
	
	/**
	 * Class for converting amounts of bytes into a human readable format
	 * @param bytes The number of bytes you would like to represent
	 * @return Human readable String
	 */
	private String humanReadable(long bytes) {
		//Units for use in the string
		//Note this is not metric, we use units like kiB and MiB to represent conversion
		//by a factor 1024, not 1000
		String units = "kMGT";
		
		//Store as Double to allow for decimal points 
		double amount = (double) bytes;
		
		//If it can be represented as just bytes then do so and return that
		if (amount < 1024) {
			return String.format("%.2f bytes", amount);
		}
		
		//If it requires a higher unit then divide by 1024 until a suitable amount has been reached
		for (int i = 0; i < units.length(); i++) {
			amount /= 1024;
			if (amount < 1024) {
				return String.format("%.2f", amount) + " " + units.charAt(i) + "iB";
			}
		}
		
		//Return the value in Human readable format
		return String.format("%.2f", amount) + " " + units.charAt(units.length() - 1);
	}
}
