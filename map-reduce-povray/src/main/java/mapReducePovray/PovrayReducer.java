package mapReducePovray;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Reducer;

import com.google.common.io.Files;

/**
 * Reduce implementation to combine multiple images to a GIF-animation using Graphics Magick.
 * 
 * Runs only on Linux as this implementation uses a precompiled binary to do the image conversion.
 * 
 * @author Sebastian Sams
 *
 */
public class PovrayReducer extends Reducer<IntWritable, FrameWriteable, IntWritable, FrameWriteable> {

	private static File GM_BINARY;
	
	// static constructor to extract the binary before the class is used
	static {
		final File workingDir = Files.createTempDir();
		workingDir.deleteOnExit();
		
		try {
			GM_BINARY = Utils.extractFileFromUpperDirectory("gm", workingDir, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void reduce(IntWritable key, Iterable<FrameWriteable> values, Context context) throws IOException, InterruptedException {
		// prepare temporary directory and process arguments
		final File workingDir = Files.createTempDir();
		workingDir.deleteOnExit();
		final List<String> commandArray = new ArrayList<>(Arrays.asList(GM_BINARY.getAbsolutePath(), "convert", "-loop", "0", "-delay", "0"));
		
		// write individual frames to disk and collect filenames
		int frameCount = 0;
		int firstFrameNumber = -1;
		for (final FrameWriteable frame : values) {
			if (firstFrameNumber < 0) {
				firstFrameNumber = frame.getFrameNumber();
			}
			commandArray.add(frame.saveImage(workingDir));
			frameCount++;
		}
		if (frameCount == 0) {
			System.out.println("reducer: nothing to do (no values) for key " + key);
			return;
		}
		
		final String outputFileName = "output.gif";
		commandArray.add(outputFileName);
		
		// run gm
		final ProcessBuilder processBuilder = new ProcessBuilder(commandArray)
				.directory(workingDir)
				// redirect input/output of process to stdout/stderr of current java process
				// allows viewing messages from gm directly in the console
				.redirectError(Redirect.INHERIT)
				.redirectOutput(Redirect.INHERIT);
		final Process process = processBuilder.start();
		final int returnCode = process.waitFor();
		if (returnCode != 0) {
			throw new IOException("gm process terminated with exit code " + returnCode);
		}
		
		// read the generated output and pass it to Hadoop
		context.write(key, new FrameWriteable(firstFrameNumber, new File(workingDir, outputFileName)));
		
		// don't check for errors as it's a temporary directory, so it's deleted by the OS at some point anyway
		FileUtils.deleteQuietly(workingDir);
	}
}
