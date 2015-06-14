package mapReducePovray;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.google.common.io.Files;


/**
 * Mapper implementation to render pictures using Povray.
 * 
 * Input format is <?, Text>, where 
 * Text lines contain the number of frames, the subset start and the subset end frame
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
	
	// static constructor to extract the binary and .pov file before the class is used
	static {
		final File workingDir = Files.createTempDir();
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
		Process p = Runtime.getRuntime().exec(POVRAY_BINARY.getAbsolutePath() + " +I" + POVRAY_INPUT.getAbsolutePath() + 
				" Output_File_Name=- +FN +W1024 +H768 +KFI" + 1 + " +KFF1" + " +SF1" + " +EF1" + " -A0.1 +R2 +KI0 +KF1 +KC -P");

		// Wait and get output as byte array
		byte[] povrayResultBytes = IOUtils.toByteArray(p.getInputStream());

		// TODO set proper frame number in constructor
		context.write(one, new FrameWriteable(1, "png", povrayResultBytes));
	}
}