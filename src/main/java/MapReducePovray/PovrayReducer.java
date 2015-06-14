package MapReducePovray;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.net.URL;
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

	@Override
	public void reduce(IntWritable key, Iterable<FrameWriteable> values, Context context) throws IOException, InterruptedException {
		// prepare temporary directory and process arguments
		final File workingDir = Files.createTempDir();
		extractGraphicsMagick(workingDir);
		final List<String> commandArray = new ArrayList<>(Arrays.asList("./gm", "convert", "-loop", "0", "-delay", "0"));
		final int firstFrameNumber = values.iterator().next().getFrameNumber();
		
		// write individual frames to disk and collect filenames
		for (final FrameWriteable frame : values) {
			commandArray.add(frame.saveImage(workingDir));
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
	
	/**
	 * Extract the  Graphics Magick binary from the JAR archive. The binary is executable after extraction.
	 * @param directory the directory where to store the extracted binary
	 * @throws IOException if an I/O error occurs
	 */
	private void extractGraphicsMagick(File directory) throws IOException {
		final URL gmURL = this.getClass().getResource("resources/gm");
		final File outputFile = new File(directory, "gm");
		FileUtils.copyURLToFile(gmURL, outputFile);
		outputFile.setExecutable(true);
	}
}