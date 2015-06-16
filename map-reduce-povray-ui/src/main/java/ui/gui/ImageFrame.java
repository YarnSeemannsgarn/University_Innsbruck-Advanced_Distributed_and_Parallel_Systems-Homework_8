package ui.gui;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * A simple frame to display an image. Supports animated GIFs.
 * @author Sebastian Sams
 *
 */
public class ImageFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private JLabel mImagelabel;

	/**
	 * Create the frame.
	 */
	public ImageFrame() {
		setResizable(false);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				// discard the image to free some resources when the window is closed
				mImagelabel.setIcon(null);
			}
		});
		
		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		mImagelabel = new JLabel();
		contentPane.add(mImagelabel, BorderLayout.CENTER);
	}
	
	/**
	 * Display an image and resize the frame accordingly.
	 * @param image the image to display
	 */
	public void setImage(Icon image) {
		mImagelabel.setIcon(image);
		setSize(image.getIconWidth(), image.getIconHeight());
	}

}
