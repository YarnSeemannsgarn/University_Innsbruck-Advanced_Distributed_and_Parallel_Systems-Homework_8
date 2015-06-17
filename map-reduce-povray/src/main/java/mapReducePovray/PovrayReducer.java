package mapReducePovray;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.log4j.Logger;

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

	private static File sGmBinary;
	private static Logger sLog = Logger.getLogger(PovrayReducer.class);
	
	// static constructor to extract the binary before the class is used
	static {
		final File workingDir = Files.createTempDir();
		workingDir.deleteOnExit();
		
		try {
			sGmBinary = Utils.extractFileFromUpperDirectory("gm", workingDir, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void reduce(IntWritable key, Iterable<FrameWriteable> values, Context context) throws IOException, InterruptedException {
		// prepare temporary directory and process arguments
		final File workingDir = Files.createTempDir();
		workingDir.deleteOnExit();
		final List<String> commandArray = new ArrayList<>(Arrays.asList(sGmBinary.getAbsolutePath(), "convert", "-loop", "0", "-delay", "4"));
		
		// write individual frames to disk and collect filenames
		// insert filenames into a sorted map so we can retrieve them sorted by frame number later
		final SortedMap<Integer, String> frameFilenameMap = new TreeMap<>();
		for (final FrameWriteable frame : values) {
			frameFilenameMap.put(frame.getFrameNumber(), frame.saveImage(workingDir));
		}
		if (frameFilenameMap.isEmpty()) {
			sLog.info("nothing to do (no values) for key " + key);
			return;
		}
		
		// add filenames as arguments ordered by their frame number
		commandArray.addAll(frameFilenameMap.values());
		final int firstFrameNumber = frameFilenameMap.keySet().iterator().next();
		
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
