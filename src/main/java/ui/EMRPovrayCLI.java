package ui;

import java.io.File;
import java.io.IOException;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;

/**
 * Utility to render a Povray animation via Amazon EMR from the command line.
 * @author Sebastian Sams
 *
 */
public final class EMRPovrayCLI {

	public static void main(String[] args) {
		AWSCredentials credentials = null;
		try {
		    credentials = new PropertiesCredentials(new File("AwsCredentials.properties"));
		} catch (IOException e1) {
		    System.out.println("Credentials were not properly entered into AwsCredentials.properties.");
		    System.out.println(e1.getMessage());
		    System.exit(1);
		}
		
		try {
			new PovrayRunner(credentials, "", "povray-emr").render(12, new File("out.gif"));
		} catch (IOException e) {
			System.out.println("rendering error");
			e.printStackTrace();
		}
	}

}
