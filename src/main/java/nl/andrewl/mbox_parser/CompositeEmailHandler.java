package nl.andrewl.mbox_parser;

import java.util.ArrayList;
import java.util.List;

/**
 * An email handler that passes received emails onto a number of other handlers.
 */
public class CompositeEmailHandler implements EmailHandler {
	private final List<EmailHandler> handlers;

	/**
	 * Constructs the composite handler, possibly with a list of handlers to
	 * pass received emails to.
	 * @param handlers A list of email handlers.
	 */
	public CompositeEmailHandler(EmailHandler... handlers) {
		this.handlers = new ArrayList<>(List.of(handlers));
	}

	/**
	 * Adds a handler to this handler.
	 * @param handler The email handler to add.
	 * @return The composite email handler, for method chaining.
	 */
	public CompositeEmailHandler withHandler(EmailHandler handler) {
		this.handlers.add(handler);
		return this;
	}

	@Override
	public void emailReceived(Email email) {
		for (var handler : handlers) {
			handler.emailReceived(email);
		}
	}
}
