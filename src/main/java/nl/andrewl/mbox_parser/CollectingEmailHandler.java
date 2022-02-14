package nl.andrewl.mbox_parser;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * A simple email handler that collects all received emails into a list. You may
 * add filter functions to avoid collecting emails which match certain criteria.
 */
public class CollectingEmailHandler implements EmailHandler {
	private final List<Email> emails = new ArrayList<>();
	private final List<Function<Email, Boolean>> filters = new ArrayList<>();

	/**
	 * Adds a filter to this handler. The filter is a function that returns true
	 * if a given email should be collected, or false if not.
	 * @param filter The filter to add.
	 */
	public void addFilter(Function<Email, Boolean> filter) {
		this.filters.add(filter);
	}

	@Override
	public void emailReceived(Email email) {
		for (var f : filters) {
			if (!f.apply(email)) return;
		}
		emails.add(email);
	}

	public List<Email> getEmails() {
		return this.emails;
	}
}
