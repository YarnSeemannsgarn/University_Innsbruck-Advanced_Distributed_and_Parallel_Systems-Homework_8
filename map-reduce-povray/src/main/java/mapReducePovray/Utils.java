package mapReducePovray;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;

public class Utils {
	public static final String povFileHDFSName = "povFile";
	
	/**
	 * Extract a file (e.g. povray and gm) from the JAR archive.
	 * @param fileName the name of the file which should be extracted
	 * @param directory the directory where to store the extracted file
	 * @param setExecutable defines whether the output file should be executable or not
	 * @throws IOException if an I/O error occurs
	 */
	public static File extractFileFromUpperDirectory(String fileName, File directory, boolean setExecutable) throws IOException {
		final URL fileUrl = Utils.class.getResource("/" + fileName);
		if (fileUrl == null) {
			throw new IOException("could not determine source location of " + fileName);
		}
		
		final File outputFile = new File(directory, fileName);
		FileUtils.copyURLToFile(fileUrl, outputFile);
		outputFile.setExecutable(setExecutable);
		return outputFile;
	}
}
