package ui;

import java.io.File;
import java.io.IOException;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;

/**
 * Utility to render a Povray animation via Amazon EMR from the command line.
 * @author Sebastian Sams
 *
 */
public final class EMRPovrayCLI {

	public static void main(String[] args) {
		// parse arguments
		if (args.length != 3) {
			System.out.println("usage: EMRPovrayCLI awsRegion clusterId storageBucket frameCount");
			System.exit(1);
		}
		final Region region = Region.getRegion(Regions.fromName(args[0]));
		final String clusterId = args[1];
		final String storageBucket = args[2];
		final int frames = Integer.parseInt(args[3]);
		
		// read credentials
		AWSCredentials credentials = null;
		try {
		    credentials = new PropertiesCredentials(new File("AwsCredentials.properties"));
		} catch (IOException e1) {
		    System.out.println("Credentials were not properly entered into AwsCredentials.properties.");
		    System.out.println(e1.getMessage());
		    System.exit(2);
		}
		
		// render the animation
		final PovrayRunner renderer = new PovrayRunner(credentials, region, clusterId, storageBucket);
		renderer.addProgressListener(new ProgressListener() {
			@Override
			public void progressMessageChanged(String message) {
				System.out.println("status: " + message);
			}
		});
		try {
			renderer.render(frames, new File("out.gif"));
		} catch (IOException e) {
			System.out.println("rendering error");
			e.printStackTrace();
		}
	}

}
