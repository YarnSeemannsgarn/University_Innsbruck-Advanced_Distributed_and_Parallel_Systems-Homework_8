package mapReducePovray;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.DefaultCodec;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.ReflectionUtils;

public class ImageOutputFormat extends FileOutputFormat<Writable, FrameWriteable> {
	
	protected static class ImageRecordWriter extends RecordWriter<Writable, FrameWriteable> {

		private final OutputStream mOut;
		
		public ImageRecordWriter(OutputStream out) {
			mOut = out;
		}
		
		@Override
		public void write(Writable key, FrameWriteable value) throws IOException {
			// key is ignored
			mOut.write(value.getImageData());
		}

		@Override
		public void close(TaskAttemptContext context) throws IOException, InterruptedException {
			mOut.close();
		}

	}

	@Override
	public RecordWriter<Writable, FrameWriteable> getRecordWriter(TaskAttemptContext job) throws IOException, InterruptedException {
	    final Path path = FileOutputFormat.getOutputPath(job);
	    final FileSystem fs = path.getFileSystem(job.getConfiguration());
	    
		if (!getCompressOutput(job)) {
			final Path fullPath = new Path(path, "result");
			final FSDataOutputStream fileOut = fs.create(fullPath, job);
			return new ImageRecordWriter(fileOut);
		} else {
			final Class<? extends CompressionCodec>  codecClass = getOutputCompressorClass(job, DefaultCodec.class);
			final CompressionCodec codec = (CompressionCodec) ReflectionUtils.newInstance(codecClass, job.getConfiguration());
			final Path fullPath = new Path(path, "result" + codec.getDefaultExtension());
			final FSDataOutputStream fileOut = fs.create(fullPath, job);
			return new ImageRecordWriter(codec.createOutputStream(fileOut));
		}
			
	}

}
