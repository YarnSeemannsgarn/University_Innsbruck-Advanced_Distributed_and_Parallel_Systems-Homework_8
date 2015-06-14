package MapReducePovray;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;


// Input format <?, Text>, where Text only contains the number of the frame
// Output format <Intwritable, BytesWritable>, where IntWritable is always 1 and BytesWritable contains the generated picture
public class PovrayMapper extends Mapper<Object, Text, IntWritable, BytesWritable>{

	private final static IntWritable one = new IntWritable(1);

	public void map(Object key, Text value, Context context
			) throws IOException, InterruptedException {
		Process p = Runtime.getRuntime().exec("./povray +I./scherk.pov Output_File_Name=- +FN +W1024 +H768 +"
						+ " +KFI" + 1 + " +KFF1" + " +SF1" + " +EF1" + " -A0.1 +R2 +KI0 +KF1 +KC -P");
		byte[] povrayResultBytes = IOUtils.toByteArray(p.getInputStream());

		context.write(one, new BytesWritable(povrayResultBytes));
	}
}