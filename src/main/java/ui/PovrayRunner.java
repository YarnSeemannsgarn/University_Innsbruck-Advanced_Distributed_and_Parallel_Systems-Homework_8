package ui;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduce;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClient;
import com.amazonaws.services.elasticmapreduce.model.AddJobFlowStepsRequest;
import com.amazonaws.services.elasticmapreduce.model.HadoopJarStepConfig;
import com.amazonaws.services.elasticmapreduce.model.StepConfig;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.common.io.Files;

/**
 * Renders a Povray animation using a running Amazon EMR cluster.
 * @author Sebastian Sams
 *
 */
public class PovrayRunner {
	
	/**
	 * Name of the jar file containing the Map-Reduce implementation for Povray.
	 * Must be present in the S3 bucket used for input/output.
	 */
	private static final String JAR_NAME = "map-reduce-povray-0.0.1-SNAPSHOT.jar";
	
	/**
	 * Name of the main class in the jar-file.
	 */
	private static final String JAR_MAIN_CLASS = "mapReducePovray.Povray";

	/**
	 * Number of frames which should be rendered by each mapper.
	 */
	private static final int FRAMES_PER_MAPPER = 10;
	
	private final AmazonS3 mS3Client;
	private final AmazonElasticMapReduce mMapReduceClient;
	
	private final String mClusterId;
	private final String mStorageBucket;
	
	/**
	 * Create a new remote renderer.
	 * @param credentials Amazon AWS credentials used for accessing the cluster
	 * @param clusterId the ID of the cluster (job flow) which should be used for rendering
	 * @param storageBucket the S3 bucket to use for storing input/output files
	 */
	public PovrayRunner(AWSCredentials credentials, String clusterId, String storageBucket) {
		mClusterId = clusterId;
		mStorageBucket = storageBucket;
		
		mS3Client = new AmazonS3Client(credentials);
		mS3Client.setRegion(Region.getRegion(Regions.US_EAST_1));
		
		mMapReduceClient = new AmazonElasticMapReduceClient(credentials);
		mMapReduceClient.setRegion(Region.getRegion(Regions.US_EAST_1));
	}
	
	/**
	 * Render the animation.
	 * @param frames number of frames to render
	 * @param output local file for storing the generated animation
	 * @throws IOException if an I/O error occurs
	 */
	public void render(int frames, File output) throws IOException {
		// cleanup old files
		deleteS3directory("input");
		deleteS3directory("output");
		
		// prepare input files for the job
		prepareInput(frames);
		
		// create and run the job
		final String s3BaseUrl = "s3://" + mStorageBucket + "/";
		HadoopJarStepConfig jarStepConfig = new HadoopJarStepConfig()
	        .withJar(s3BaseUrl + JAR_NAME)
	        .withMainClass(JAR_MAIN_CLASS)
	        .withArgs(s3BaseUrl + "input/", s3BaseUrl + "output/");
		mMapReduceClient.addJobFlowSteps(new AddJobFlowStepsRequest(mClusterId).withSteps(new StepConfig("render", jarStepConfig)));
		
		// download the generated gif
		downloadResult(output);
	}
	
	/**
	 * Delete a "directory" in S3. A directory is equivalent to all files starting with a common prefix and are delimited by a slash.
	 * @param directory the directory to delete (without the slash at the end)
	 */
	private void deleteS3directory(String directory) {
		final List<S3ObjectSummary> objects = mS3Client.listObjects(mStorageBucket, directory + "/").getObjectSummaries();
		
		if (objects.size() > 0) {
			final List<String> keys = new ArrayList<String>(objects.size());
			for (final S3ObjectSummary object : objects) {
				keys.add(object.getKey());
			}
			mS3Client.deleteObjects(new DeleteObjectsRequest(mStorageBucket).withKeys(keys.toArray(new String[0])));
		}
	}

	/**
	 * Prepare the input files required by the Map-Reduce implementation.
	 * Creates the required files and uploads them to the S3 bucket.
	 * @param frames the number of frames to render
	 * @throws IOException if an I/O error occurs
	 */
	private void prepareInput(int frames) throws IOException {
		final File tempDir = Files.createTempDir();
		tempDir.deleteOnExit();
		
		try {
			int start;
			int end = 0;
			int inputNumber = 0;
			do {
				start = end + 1;
				end = Math.min(start + FRAMES_PER_MAPPER - 1, frames);
				
				final File file = new File(tempDir, "file" + inputNumber);
				Files.write("1 " + frames + " " + start + " " + end, file, Charset.forName("UTF-8"));
				mS3Client.putObject(mStorageBucket, "input/file" + inputNumber, file);
				inputNumber++;
			} while (end != frames);
		} catch (IOException e) {
			throw new IOException("could not create input files for job", e);
		}
	}
	
	/**
	 * Download the generated animation.
	 * @param output the local file for storing the result
	 */
	private void downloadResult(File output) {
		mS3Client.getObject(new GetObjectRequest(mStorageBucket, "output/result"), output);
	}

}
