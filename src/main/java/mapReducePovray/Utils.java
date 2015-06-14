package mapReducePovray;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;

public class Utils {
	/**
	 * Extract a file (e.g. povray and gm) from the JAR archive. The binary is executable after extraction.
	 * @param directory the directory where to store the extracted file
	 * @throws IOException if an I/O error occurs
	 */
	public static File extractFileFromUpperDirectory(String fileName, File directory, boolean setExecutable) throws IOException {
		final URL fileUrl = Utils.class.getResource("../" + fileName);
		if (fileUrl == null) {
			throw new IOException("could not determine source location of " + fileName);
		}
		
		final File outputFile = new File(directory, fileName);
		FileUtils.copyURLToFile(fileUrl, outputFile);
		outputFile.setExecutable(setExecutable);
		return outputFile;
	}
}
