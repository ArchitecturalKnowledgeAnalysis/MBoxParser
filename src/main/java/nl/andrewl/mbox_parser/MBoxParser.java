package nl.andrewl.mbox_parser;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.mboxiterator.CharBufferWrapper;
import org.apache.james.mime4j.mboxiterator.FromLinePatterns;
import org.apache.james.mime4j.mboxiterator.MboxIterator;
import org.apache.james.mime4j.parser.MimeStreamParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * This parser offers various methods for parsing MBox files. Each parsed email
 * will be passed to a given {@link EmailHandler} for processing.
 */
public class MBoxParser {
	private final EmailHandler emailHandler;

	/**
	 * Convenience method to parse a series of mbox files (or directories
	 * containing them) and collect the emails in a list.
	 * @param files The files or directories to parse.
	 * @return The list of parsed emails.
	 * @throws IOException If an error occurs.
	 */
	public static List<Email> parseEmails(Path... files) throws IOException {
		var handler = new CollectingEmailHandler();
		var parser = new MBoxParser(handler);
		parser.parse(files);
		return handler.getEmails();
	}

	/**
	 * Constructs the parser with the given email handler.
	 * @param emailHandler The email handler to apply to all read emails.
	 */
	public MBoxParser(EmailHandler emailHandler) {
		this.emailHandler = emailHandler;
	}

	/**
	 * Parses a series of files (or directories).
	 * @param files The list of files to parse.
	 * @throws IOException If an error occurs during parsing.
	 */
	public void parse(Path... files) throws IOException {
		for (var f : files) {
			parse(f);
		}
	}

	/**
	 * Parses a file or directory. If a directory, then the contents of that
	 * directory will be parsed recursively. Non-mbox files will be silently
	 * ignored.
	 * @param file The file or directory to parse.
	 * @throws IOException If an error occurs while reading.
	 */
	private void parse(Path file) throws IOException {
		if (Files.isDirectory(file)) {
			try (var s = Files.list(file)) {
				for (var path : s.toList()) {
					parse(path);
				}
			}
			return;
		}
		// Skip any non-mbox files encountered.
		if (!file.getFileName().toString().toLowerCase().endsWith(".mbox")) {
			return;
		}
		MboxIterator mboxIterator = MboxIterator.fromFile(file)
				.fromLine(FromLinePatterns.DEFAULT2)
				.charset(StandardCharsets.ISO_8859_1)
				.build();
		MimeStreamParser parser = new MimeStreamParser();
		for (CharBufferWrapper w : mboxIterator) {
			var handler = new EmailContentHandler();
			parser.setContentHandler(handler);
			try {
				parser.parse(w.asInputStream(StandardCharsets.UTF_8));
				emailHandler.emailReceived(handler.getEmail());
			} catch (MimeException | IOException e) {
				e.printStackTrace();
			}
		}
	}
}
