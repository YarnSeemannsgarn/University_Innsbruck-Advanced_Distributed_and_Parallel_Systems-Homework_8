package ui;

/**
 * Receives reports about the progress of an operation.
 * @author Sebastian Sams
 *
 */
public interface ProgressListener {

	/**
	 * Invoked whenever a new message describing the current progress is available.
	 * @param message the updated description of the progress
	 */
	void progressMessageChanged(String message);
}
