package mapReducePovray;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Writable;

import com.google.common.io.Files;

/**
 * One or more frames of a rendered video which can be used as a value for map-reduce.
 *  
 * @author Sebastian Sams
 *
 */
public class FrameWriteable implements Writable, Comparable<FrameWriteable> {

	private int mFrameNumber;
	private BytesWritable mImageData;
	private String mImageFormat;
	
	/**
	 * Create a new frame with existing data.
	 * @param frameNumber the frame number, or the number of the first frames if multiple are stored in the image data
	 * @param imageFormat the format of the image, e.g. "png"
	 * @param image the image data
	 */
	public FrameWriteable(int frameNumber, String imageFormat, byte[] image) {
		mFrameNumber = frameNumber;
		mImageFormat = imageFormat;
		mImageData = new BytesWritable(image);
	}
	
	/**
	 * Create a new frame from a file.
	 * @param frameNumber the frame number, or the number of the first frames if multiple are stored in the image data
	 * @param image the file containing the image data
	 * @throws IOException if an I/O error occurs
	 */
	public FrameWriteable(int frameNumber, File image) throws IOException {
		this(frameNumber, Files.getFileExtension(image.getName()), Files.toByteArray(image));
	}
	
	/**
	 * Create an empty writeable without any data.
	 */
	protected FrameWriteable() {
		
	}
	
	/**
	 * Save the image data to a file.
	 * @param directory the directory where the image should be saved.
	 * @return the name of the created output file, relative to the specified directory
	 * @throws IOException if an I/O error occurs
	 */
	public String saveImage(File directory) throws IOException {
		if (!directory.isDirectory()) {
			throw new IOException("not a valid directory");
		}
		
		final String fileName = getOutputFilename();
		Files.write(mImageData.getBytes(), new File(directory, fileName));
		return fileName;
	}
	
	/**
	 * Get the frame number.
	 * @return the frame number
	 */
	public int getFrameNumber() {
		return mFrameNumber;
	}
	
	/**
	 * Get the name of the output file when writing the image data.
	 * @return the file name
	 */
	protected String getOutputFilename() {
		return "frame" + mFrameNumber + "." + mImageFormat;
	}
	
	@Override
	public int compareTo(FrameWriteable o) {
		return Integer.compare(mFrameNumber, o.mFrameNumber);
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeInt(mFrameNumber);
		out.writeUTF(mImageFormat);
		mImageData.write(out);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		mFrameNumber = in.readInt();
		mImageFormat = in.readUTF();
		mImageData = new BytesWritable();
		mImageData.readFields(in);
	}

	/**
	 * Create a new writable by reading the data from the stream. 
	 * @param in the input stream
	 * @return the read writable
	 * @throws IOException if an I/O error occurs
	 */
	public static FrameWriteable read(DataInput in) throws IOException {
		final FrameWriteable w = new FrameWriteable();
		w.readFields(in);
		return w;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + mFrameNumber;
		result = prime * result
				+ ((mImageFormat == null) ? 0 : mImageFormat.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FrameWriteable other = (FrameWriteable) obj;
		if (mFrameNumber != other.mFrameNumber)
			return false;
		if (mImageFormat == null) {
			if (other.mImageFormat != null)
				return false;
		} else if (!mImageFormat.equals(other.mImageFormat))
			return false;
		return true;
	}
	
}
