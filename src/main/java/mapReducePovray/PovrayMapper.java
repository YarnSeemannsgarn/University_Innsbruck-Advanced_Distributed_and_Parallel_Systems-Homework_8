package mapReducePovray;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Logger;

import com.google.common.io.Files;


/**
 * Mapper implementation to render pictures using Povray.
 * 
 * Input format is <?, Text>, where 
 * Text lines contain the start frame, the end frame, the subset start frame and the subset end frame
 * 
 * Output format is <Intwritable, FrameWritable>, where 
 * IntWritable is always 1 to combine all frames in the Reducer and 
 * FrameWritable contains the generated picture
 * 
 * @author Jan Schlenker
 *
 */

public class PovrayMapper extends Mapper<Object, Text, IntWritable, FrameWriteable>{

	private final static IntWritable one = new IntWritable(1);
	private static File POVRAY_BINARY;
	private static File POVRAY_INPUT;
	private static Logger log = Logger.getLogger(PovrayMapper.class);

	// static constructor to extract the binary and .pov file before the class is used
	static {
		File workingDir = Files.createTempDir();
		workingDir.deleteOnExit();

		try {
			POVRAY_BINARY = Utils.extractFileFromUpperDirectory("povray", workingDir, true);
			POVRAY_INPUT = Utils.extractFileFromUpperDirectory("scherk.pov", workingDir, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void map(Object key, Text value, Context context
			) throws IOException, InterruptedException {
		File workingDir = Files.createTempDir();
		workingDir.deleteOnExit();

		// Get frame numbers
		StringTokenizer inputTokenizer = new StringTokenizer(value.toString());
		int startFrame = Integer.parseInt(inputTokenizer.nextToken());
		int endFrame = Integer.parseInt(inputTokenizer.nextToken());
		int subsetStartFrame = Integer.parseInt(inputTokenizer.nextToken());
		int subsetEndFrame = Integer.parseInt(inputTokenizer.nextToken());

		List<String> commandArray = new ArrayList<>(Arrays.asList(
				POVRAY_BINARY.getAbsolutePath(), "+I" + POVRAY_INPUT.getAbsolutePath(), "+O" + workingDir.getAbsolutePath() + "/frame",
				"+KFI" + startFrame, "+KFF" + endFrame, "+SF" + subsetStartFrame, "+EF" + subsetEndFrame,
				"+FN", "+W1024", "+H768", "-A0.1", "+R2", "+KI0", "+KF1", "+KC", "-P")
				);

		// Redirect outputs of process to log files, otherwise hadoop get stucked for multiple images somehow
		ProcessBuilder processBuilder = new ProcessBuilder(commandArray).directory(workingDir)
				.redirectError(new File(workingDir.getAbsolutePath() + "/povray.log"))
				.redirectOutput(new File(workingDir.getAbsolutePath() + "/povray_error.log"));

		log.info("Render frames " + subsetStartFrame + "-" + subsetEndFrame + " on " + InetAddress.getLocalHost().getHostName());
		Process process = processBuilder.start();
		int returnCode = process.waitFor();
		if (returnCode != 0) {
			throw new IOException("povray process terminated with exit code " + returnCode);
		}

		// Get frames and return them as mapper output
		for (int i = subsetStartFrame; i <= subsetEndFrame; i++) {
			Path framPath = Paths.get(workingDir.getAbsolutePath() + "/frame" + i + ".png");
			byte[] frameBytes = java.nio.file.Files.readAllBytes(framPath);
			context.write(one, new FrameWriteable(i, "png", frameBytes));
		}
	}
}