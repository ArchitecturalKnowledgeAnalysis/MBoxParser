package nl.andrewl.mboxparser;

/**
 * Handler that's invoked whenever a new email is parsed from an MBox file.
 */
public interface EmailHandler {
	void emailReceived(Email email);
}
