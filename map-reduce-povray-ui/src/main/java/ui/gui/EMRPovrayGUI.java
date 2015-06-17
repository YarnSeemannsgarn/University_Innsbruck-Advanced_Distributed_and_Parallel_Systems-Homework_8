package ui.gui;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import ui.PovrayRunner;
import ui.ProgressListener;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;

/**
 * Small application with a GUI to render a Povray animation via Amazon EMR.
 * @author Sebastian Sams
 *
 */
public class EMRPovrayGUI {

	public static final Regions DEFAULT_REGION = Regions.US_EAST_1;
	
	public static final String DEFAULT_CLUSTER_ID = "";
	
	public static final String DEFAULT_STORAGE_BUCKET = "povray-emr";
	
	private final AWSCredentials mCredentials;
	private final RenderOptionsFrame mOptionsFrame;
	private final ImageFrame mImageFrame;
	private final ExecutorService mExecutorService;
	
	/**
	 * Create a new renderer GUI.
	 * @param credentials the credentials to use for interacting with AWS
	 */
	public EMRPovrayGUI(AWSCredentials credentials) {
		mCredentials = credentials;
		
		// use daemon threads so program can end automatically without needing to terminate the executor 
		mExecutorService = Executors.newSingleThreadExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				final Thread thread = new Thread(r, "render thread");
				thread.setDaemon(true);
				return thread;
			}
		});
		
		mImageFrame = new ImageFrame();
		
		mOptionsFrame = new RenderOptionsFrame(DEFAULT_REGION, DEFAULT_CLUSTER_ID, DEFAULT_STORAGE_BUCKET);
		mOptionsFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mOptionsFrame.addRenderActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				render();
			}
		});
		
		mOptionsFrame.setVisible(true);
	}
	
	/**
	 * Render the animation using the currently entered options.
	 */
	private void render() {
		mOptionsFrame.setProgress(true);
		mImageFrame.setVisible(false);
		
		// render on a different thread to avoid blocking the GUI
		mExecutorService.submit(new Runnable() {
			@Override
			public void run() {
				final PovrayRunner renderer = new PovrayRunner(
						mCredentials,
						Region.getRegion(mOptionsFrame.getRegion()),
						mOptionsFrame.getClusterId(),
						mOptionsFrame.getStorageBucket());
				renderer.addProgressListener(new ProgressListener() {
					@Override
					public void progressMessageChanged(final String message) {
						EventQueue.invokeLater(new Runnable() {
							@Override
							public void run() {
								mOptionsFrame.setProgressMessage(message);
							}
						});
					}
				});
				
				try {
					final File outputFile = mOptionsFrame.getOutputFile();
					renderer.render(mOptionsFrame.getPovFile(), mOptionsFrame.getFrameCount(), outputFile);
					
					final Icon image = new ImageIcon(outputFile.getAbsolutePath());
					EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() {
							mImageFrame.setTitle(outputFile.getName());
							mImageFrame.setImage(image);
							mImageFrame.setVisible(true);
						}
					});
				} catch (final IOException e) {
					e.printStackTrace();
					
					EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() {
							mOptionsFrame.setProgressMessage("error");
							JOptionPane.showMessageDialog(mOptionsFrame, e.getMessage(), "Rendering error", JOptionPane.ERROR_MESSAGE);
						}
					});
				} finally {
					EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() {
							mOptionsFrame.setProgress(false);
						}
					});
				}
			}
		});
	}
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		
		try {
		    final AWSCredentials credentials = new PropertiesCredentials(new File("AwsCredentials.properties"));
		    
		    EventQueue.invokeLater(new Runnable() {
				public void run() {
					try {
						new EMRPovrayGUI(credentials);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		} catch (IllegalArgumentException | IOException e) {
			System.out.println(e.getMessage());
			JOptionPane.showMessageDialog(null, "Credentials were not properly entered into AwsCredentials.properties.", "Could not load credentials", JOptionPane.ERROR_MESSAGE);
		    System.exit(2);
		}
	}
	
	

}
