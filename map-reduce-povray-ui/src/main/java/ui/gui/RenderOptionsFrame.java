package ui.gui;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.amazonaws.regions.Regions;

public class RenderOptionsFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private JComboBox<Regions> mComboBoxRegion;
	private JTextField mTxtClusterId;
	private JTextField mTxtBucket;
	private JSpinner mSpinnerFrameCount;
	private JTextField mTxtOutputFile;
	private JButton mBtnSelectOutput;
	private JProgressBar mProgressBar;
	private JButton mBtnRender;
	private JLabel mLblProgressMessage;
	
	private final JFileChooser mOutputFileChooser;
	private final JFileChooser mPovFileChooser;
	private File mPovFile;
	private File mOutputFile;
	private JTextField mTxtSceneFile;
	private JButton mBtnSelectScene;

	/**
	 * Create the frame.
	 * @param defaultRegion the default AWS region
	 * @param defaultCluster the default cluster ID
	 * @param defaultBucket the default S3 bucket name
	 */
	public RenderOptionsFrame(Regions defaultRegion, String defaultCluster, String defaultBucket) {
		createGui();
		mOutputFileChooser = new JFileChooser();
		mOutputFileChooser.setFileFilter(new FileNameExtensionFilter("GIF animation", "gif"));
		mOutputFileChooser.setAcceptAllFileFilterUsed(false);
		mPovFileChooser = new JFileChooser();
		mPovFileChooser.setFileFilter(new FileNameExtensionFilter("Povray scene", "pov"));
		mPovFileChooser.setAcceptAllFileFilterUsed(false);
		
		mComboBoxRegion.setSelectedItem(defaultRegion);
		mTxtClusterId.setText(defaultCluster);
		mTxtBucket.setText(defaultBucket);
		
		verifyInputs();
		
		final DocumentListener verifyInputDocumentListener = new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				verifyInputs();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				verifyInputs();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
			}
		};
		
		mTxtClusterId.getDocument().addDocumentListener(verifyInputDocumentListener);
		mTxtBucket.getDocument().addDocumentListener(verifyInputDocumentListener);
		
		mBtnSelectOutput.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mOutputFileChooser.showSaveDialog(RenderOptionsFrame.this) == JFileChooser.APPROVE_OPTION) {
					mOutputFile = mOutputFileChooser.getSelectedFile();
					if (!mOutputFile.getName().endsWith(".gif")) {
						mOutputFile = new File(mOutputFile.getPath() + ".gif");
					}
					
					mTxtOutputFile.setText(mOutputFile.getAbsolutePath());
					verifyInputs();
				}
			}
		});
		
		mBtnSelectScene.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mPovFileChooser.showOpenDialog(RenderOptionsFrame.this) == JFileChooser.APPROVE_OPTION) {
					mPovFile = mPovFileChooser.getSelectedFile();
					mTxtSceneFile.setText(mPovFile.getAbsolutePath());
					verifyInputs();
				}
			}
		});
	}

	/**
	 * Create the GUI elements.
	 */
	private void createGui() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 459, 456);
		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		SpringLayout sl_contentPane = new SpringLayout();
		contentPane.setLayout(sl_contentPane);
		
		JPanel awsPanel = new JPanel();
		sl_contentPane.putConstraint(SpringLayout.NORTH, awsPanel, 6, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, awsPanel, 5, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, awsPanel, 180, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, awsPanel, -5, SpringLayout.EAST, contentPane);
		awsPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "AWS settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		SpringLayout sl_awsPanel = new SpringLayout();
		awsPanel.setLayout(sl_awsPanel);
		
		JLabel lblRegion = new JLabel("Region");
		lblRegion.setHorizontalAlignment(SwingConstants.TRAILING);
		sl_awsPanel.putConstraint(SpringLayout.NORTH, lblRegion, 10, SpringLayout.NORTH, awsPanel);
		sl_awsPanel.putConstraint(SpringLayout.WEST, lblRegion, 10, SpringLayout.WEST, awsPanel);
		awsPanel.add(lblRegion);
		
		mComboBoxRegion = new JComboBox<Regions>();
		sl_awsPanel.putConstraint(SpringLayout.NORTH, mComboBoxRegion, -3, SpringLayout.NORTH, lblRegion);
		sl_awsPanel.putConstraint(SpringLayout.WEST, mComboBoxRegion, 6, SpringLayout.EAST, lblRegion);
		sl_awsPanel.putConstraint(SpringLayout.EAST, mComboBoxRegion, -10, SpringLayout.EAST, awsPanel);
		mComboBoxRegion.setModel(new DefaultComboBoxModel<Regions>(Regions.values()));
		awsPanel.add(mComboBoxRegion);
		
		JLabel lblBucket = new JLabel("S3 bucket");
		sl_awsPanel.putConstraint(SpringLayout.WEST, lblBucket, 0, SpringLayout.WEST, lblRegion);
		lblBucket.setHorizontalAlignment(SwingConstants.TRAILING);
		awsPanel.add(lblBucket);
		
		JLabel lblClusterId = new JLabel("EMR cluster ID");
		lblClusterId.setHorizontalAlignment(SwingConstants.TRAILING);
		sl_awsPanel.putConstraint(SpringLayout.EAST, lblRegion, 0, SpringLayout.EAST, lblClusterId);
		sl_awsPanel.putConstraint(SpringLayout.EAST, lblBucket, 0, SpringLayout.EAST, lblClusterId);
		sl_awsPanel.putConstraint(SpringLayout.WEST, lblClusterId, 0, SpringLayout.WEST, lblRegion);
		awsPanel.add(lblClusterId);
		
		mTxtBucket = new JTextField();
		sl_awsPanel.putConstraint(SpringLayout.NORTH, lblBucket, 3, SpringLayout.NORTH, mTxtBucket);
		awsPanel.add(mTxtBucket);
		mTxtBucket.setColumns(25);
		
		mTxtClusterId = new JTextField();
		sl_awsPanel.putConstraint(SpringLayout.NORTH, mTxtBucket, 3, SpringLayout.SOUTH, mTxtClusterId);
		sl_awsPanel.putConstraint(SpringLayout.NORTH, lblClusterId, 3, SpringLayout.NORTH, mTxtClusterId);
		sl_awsPanel.putConstraint(SpringLayout.NORTH, mTxtClusterId, 3, SpringLayout.SOUTH, mComboBoxRegion);
		sl_awsPanel.putConstraint(SpringLayout.WEST, mTxtBucket, 0, SpringLayout.WEST, mTxtClusterId);
		sl_awsPanel.putConstraint(SpringLayout.EAST, mTxtBucket, 0, SpringLayout.EAST, mTxtClusterId);
		sl_awsPanel.putConstraint(SpringLayout.WEST, mTxtClusterId, 0, SpringLayout.WEST, mComboBoxRegion);
		sl_awsPanel.putConstraint(SpringLayout.EAST, mTxtClusterId, 0, SpringLayout.EAST, mComboBoxRegion);
		awsPanel.add(mTxtClusterId);
		contentPane.add(awsPanel);
		
		JPanel povrayPanel = new JPanel();
		sl_contentPane.putConstraint(SpringLayout.NORTH, povrayPanel, 6, SpringLayout.SOUTH, awsPanel);
		sl_contentPane.putConstraint(SpringLayout.WEST, povrayPanel, 5, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, povrayPanel, -5, SpringLayout.EAST, contentPane);
		povrayPanel.setBorder(new TitledBorder(null, "Povray settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		SpringLayout sl_povrayPanel = new SpringLayout();
		povrayPanel.setLayout(sl_povrayPanel);
		
		JLabel lblFrameCount = new JLabel("Frame count");
		lblFrameCount.setHorizontalAlignment(SwingConstants.TRAILING);
		sl_povrayPanel.putConstraint(SpringLayout.NORTH, lblFrameCount, 10, SpringLayout.NORTH, povrayPanel);
		sl_povrayPanel.putConstraint(SpringLayout.WEST, lblFrameCount, 10, SpringLayout.WEST, povrayPanel);
		povrayPanel.add(lblFrameCount);
		
		JLabel lblOutput = new JLabel("Output");
		lblOutput.setHorizontalAlignment(SwingConstants.TRAILING);
		sl_povrayPanel.putConstraint(SpringLayout.WEST, lblOutput, 0, SpringLayout.WEST, lblFrameCount);
		sl_povrayPanel.putConstraint(SpringLayout.EAST, lblOutput, 0, SpringLayout.EAST, lblFrameCount);
		povrayPanel.add(lblOutput);
		
		mSpinnerFrameCount = new JSpinner();
		mSpinnerFrameCount.setModel(new SpinnerNumberModel(new Integer(10), new Integer(1), null, new Integer(1)));
		sl_povrayPanel.putConstraint(SpringLayout.NORTH, mSpinnerFrameCount, -3, SpringLayout.NORTH, lblFrameCount);
		sl_povrayPanel.putConstraint(SpringLayout.WEST, mSpinnerFrameCount, 6, SpringLayout.EAST, lblFrameCount);
		sl_povrayPanel.putConstraint(SpringLayout.EAST, mSpinnerFrameCount, -10, SpringLayout.EAST, povrayPanel);
		povrayPanel.add(mSpinnerFrameCount);
		
		mTxtOutputFile = new JTextField();
		sl_povrayPanel.putConstraint(SpringLayout.NORTH, lblOutput, 3, SpringLayout.NORTH, mTxtOutputFile);
		mTxtOutputFile.setEditable(false);
		sl_povrayPanel.putConstraint(SpringLayout.WEST, mTxtOutputFile, 0, SpringLayout.WEST, mSpinnerFrameCount);
		povrayPanel.add(mTxtOutputFile);
		mTxtOutputFile.setColumns(10);
		
		mBtnSelectOutput = new JButton("Select...");
		sl_povrayPanel.putConstraint(SpringLayout.NORTH, mBtnSelectOutput, 0, SpringLayout.NORTH, mTxtOutputFile);
		sl_povrayPanel.putConstraint(SpringLayout.SOUTH, mBtnSelectOutput, 0, SpringLayout.SOUTH, mTxtOutputFile);
		sl_povrayPanel.putConstraint(SpringLayout.EAST, mTxtOutputFile, -6, SpringLayout.WEST, mBtnSelectOutput);
		sl_povrayPanel.putConstraint(SpringLayout.EAST, mBtnSelectOutput, 0, SpringLayout.EAST, mSpinnerFrameCount);
		povrayPanel.add(mBtnSelectOutput);
		contentPane.add(povrayPanel);
		
		mLblProgressMessage = new JLabel("ready");
		mLblProgressMessage.setFont(mLblProgressMessage.getFont().deriveFont(mLblProgressMessage.getFont().getStyle() | Font.ITALIC));
		sl_contentPane.putConstraint(SpringLayout.WEST, mLblProgressMessage, 7, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, mLblProgressMessage, -5, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, mLblProgressMessage, -7, SpringLayout.EAST, contentPane);
		contentPane.add(mLblProgressMessage);
		
		mProgressBar = new JProgressBar();
		sl_contentPane.putConstraint(SpringLayout.SOUTH, povrayPanel, -6, SpringLayout.NORTH, mProgressBar);
		
		JLabel lblScene = new JLabel("Scene");
		sl_povrayPanel.putConstraint(SpringLayout.WEST, lblScene, 0, SpringLayout.WEST, lblFrameCount);
		lblScene.setHorizontalAlignment(SwingConstants.TRAILING);
		sl_povrayPanel.putConstraint(SpringLayout.EAST, lblScene, 0, SpringLayout.EAST, lblFrameCount);
		povrayPanel.add(lblScene);
		
		mTxtSceneFile = new JTextField();
		sl_povrayPanel.putConstraint(SpringLayout.NORTH, mTxtOutputFile, 3, SpringLayout.SOUTH, mTxtSceneFile);
		sl_povrayPanel.putConstraint(SpringLayout.NORTH, lblScene, 3, SpringLayout.NORTH, mTxtSceneFile);
		sl_povrayPanel.putConstraint(SpringLayout.NORTH, mTxtSceneFile, 3, SpringLayout.SOUTH, mSpinnerFrameCount);
		sl_povrayPanel.putConstraint(SpringLayout.WEST, mTxtSceneFile, 0, SpringLayout.WEST, mSpinnerFrameCount);
		mTxtSceneFile.setEditable(false);
		povrayPanel.add(mTxtSceneFile);
		mTxtSceneFile.setColumns(10);
		
		mBtnSelectScene = new JButton("Select...");
		sl_povrayPanel.putConstraint(SpringLayout.NORTH, mBtnSelectScene, 0, SpringLayout.NORTH, mTxtSceneFile);
		sl_povrayPanel.putConstraint(SpringLayout.SOUTH, mBtnSelectScene, 0, SpringLayout.SOUTH, mTxtSceneFile);
		sl_povrayPanel.putConstraint(SpringLayout.EAST, mBtnSelectScene, 0, SpringLayout.EAST, mSpinnerFrameCount);
		sl_povrayPanel.putConstraint(SpringLayout.EAST, mTxtSceneFile, -6, SpringLayout.WEST, mBtnSelectScene);
		povrayPanel.add(mBtnSelectScene);
		sl_contentPane.putConstraint(SpringLayout.WEST, mProgressBar, 0, SpringLayout.WEST, mLblProgressMessage);
		contentPane.add(mProgressBar);
		
		mBtnRender = new JButton("Render");
		sl_contentPane.putConstraint(SpringLayout.NORTH, mProgressBar, 0, SpringLayout.NORTH, mBtnRender);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, mProgressBar, 0, SpringLayout.SOUTH, mBtnRender);
		sl_contentPane.putConstraint(SpringLayout.EAST, mProgressBar, -6, SpringLayout.WEST, mBtnRender);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, mBtnRender, -6, SpringLayout.NORTH, mLblProgressMessage);
		sl_contentPane.putConstraint(SpringLayout.EAST, mBtnRender, 0, SpringLayout.EAST, mLblProgressMessage);
		contentPane.add(mBtnRender);
	}
	
	protected void verifyInputs() {
		final boolean invalid = mTxtClusterId.getText().isEmpty()
				|| mTxtBucket.getText().isEmpty() || mOutputFile == null
				|| mPovFile == null;
		
		mBtnRender.setEnabled(!invalid);
	}
	
	/**
	 * Changes the state of the frame to display progress.
	 * @param progress whether to display progress or not
	 */
	public void setProgress(boolean progress) {
		setInputsEnabled(!progress);
		mBtnRender.setEnabled(!progress);
		mProgressBar.setIndeterminate(progress);
	}
	
	public void setProgressMessage(String message) {
		mLblProgressMessage.setText(message);
	}
	
	/**
	 * Enable (or disable) the input fields.
	 * @param enabled
	 */
	protected void setInputsEnabled(boolean enabled) {
		mComboBoxRegion.setEnabled(enabled);
		mTxtClusterId.setEnabled(enabled);
		mTxtBucket.setEnabled(enabled);
		mSpinnerFrameCount.setEnabled(enabled);
		mBtnSelectOutput.setEnabled(enabled);
		mBtnSelectScene.setEnabled(enabled);
	}
	
	/**
	 * Add an {@link ActionListener} to the button used to start the rendering job.
	 * @param listener the listener to add
	 */
	public void addRenderActionListener(ActionListener listener) {
		mBtnRender.addActionListener(listener);
	}
	
	/**
	 * Get the cluster ID.
	 * @return the cluster ID
	 */
	public String getClusterId() {
		return mTxtClusterId.getText();
	}
	
	/**
	 * Get the storage bucket.
	 * @return the storage bucket
	 */
	public String getStorageBucket() {
		return mTxtBucket.getText();
	}
	
	/**
	 * Get the AWS region.
	 * @return the region
	 */
	public Regions getRegion() {
		return (Regions) mComboBoxRegion.getSelectedItem();
	}
	
	/**
	 * Get the number of frames to render.
	 * @return the frame count
	 */
	public int getFrameCount() {
		return (Integer) mSpinnerFrameCount.getValue();
	}
	
	/**
	 * Get the output file for storing the rendered animation.
	 * @return the output file
	 */
	public File getOutputFile() {
		return mOutputFile;
	}
	
	/**
	 * Get the Povray scene description which should be rendered.
	 * @return the input file
	 */
	public File getPovFile() {
		return mPovFile;
	}
}
