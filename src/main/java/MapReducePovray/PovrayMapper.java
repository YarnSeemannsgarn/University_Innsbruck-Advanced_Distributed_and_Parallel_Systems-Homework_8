package MapReducePovray;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;


/**
 * Mapper implementation to render pictures using Povray.
 * 
 * Input format is <?, Text>, where 
 * Text lines contain the number of frames, the subset start and the subset end frame
 * 
 * Output format is <Intwritable, FramesWritable>, where 
 * IntWritable is always 1 to combine all frames in the Reducer and 
 * BytesWritable contains the generated picture
 * 
 * @author Jan Schlenker
 *
 */

public class PovrayMapper extends Mapper<Object, Text, IntWritable, FrameWriteable>{

	private final static IntWritable one = new IntWritable(1);

	@Override
	public void map(Object key, Text value, Context context
			) throws IOException, InterruptedException {
		Process p = Runtime.getRuntime().exec("./povray +I./scherk.pov Output_File_Name=- +FN +W1024 +H768 +"
						+ " +KFI" + 1 + " +KFF1" + " +SF1" + " +EF1" + " -A0.1 +R2 +KI0 +KF1 +KC -P");
		byte[] povrayResultBytes = IOUtils.toByteArray(p.getInputStream());

		// TODO set proper frame number in constructor
		context.write(one, new FrameWriteable(1, "png", povrayResultBytes));
	}
}