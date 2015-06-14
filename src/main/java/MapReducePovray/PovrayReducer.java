package MapReducePovray;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Reducer;

// TODO: Sebastian has to implement povray reducer it
public class PovrayReducer
extends Reducer<IntWritable, BytesWritable, IntWritable, BytesWritable> {
	private BytesWritable gif = new BytesWritable();

	public void reduce(IntWritable key, Iterable<BytesWritable> values,
			Context context
			) throws IOException, InterruptedException {
		
		// TODO: Sebastian has to implement gif creation
		gif = values.iterator().next();		
		FileUtils.writeByteArrayToFile(new File("./test.png"), gif.getBytes());
		
		// context.write?
	}
}