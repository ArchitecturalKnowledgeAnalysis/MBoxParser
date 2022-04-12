package nl.andrewl.mboxparser;

import org.apache.james.mime4j.parser.AbstractContentHandler;
import org.apache.james.mime4j.stream.BodyDescriptor;
import org.apache.james.mime4j.stream.Field;

import javax.mail.internet.MimeUtility;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A content handler that can be provided to the Mbox parser, to handle any
 * incoming field values.
 */
public class EmailContentHandler extends AbstractContentHandler {
	private final Email email = new Email();

	@Override
	public void body(BodyDescriptor bd, InputStream is) throws IOException {
		email.mimeType = bd.getMimeType();
		email.charset = bd.getCharset();
		email.transferEncoding = bd.getTransferEncoding();
		email.body = is.readAllBytes();
	}

	@Override
	public void field(Field field) {
		String name = field.getName().toUpperCase();
		String body = field.getBody();
		try {
			body = MimeUtility.decodeText(body);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		switch (name) {
			case "FROM" -> email.sentFrom = body;
			case "SUBJECT" -> email.subject = body;
			case "DATE" -> email.date = parseDate(body);
			case "MESSAGE-ID" -> email.messageId = sanitizeId(body);
			case "IN-REPLY-TO" -> email.inReplyTo = body;
		}
	}

	private ZonedDateTime parseDate(String s) {
		int timezoneParenthesis = s.lastIndexOf('(');
		if (timezoneParenthesis != -1) {
			s = s.substring(0, timezoneParenthesis).trim();
		}
		if (s.startsWith("RANDOM_")) {
			s = s.substring(7);
		}
		s = s.replaceAll("\\s+", " ");
		// RFC-1123 doesn't allow this type of GMT label.
		if (s.endsWith("+0000")) s = s.replace("+0000", "GMT");

		// Some dates are formatted like so: "Wed Mar 19 02:22:16 2014"
		// We need to convert this to: "Wed, 19 Mar 2014 02:22:16
		final String regex = "(\\w{3}) (\\w{3}) (\\d{1,2}) (\\d{2}:\\d{2}:\\d{2}) (\\d{4})";
		Pattern p1 = Pattern.compile(regex);
		Matcher m1 = p1.matcher(s);
		if (m1.find()) {
			String weekday = m1.group(1);
			String month = m1.group(2);
			String dayOfMonth = m1.group(3);
			String time = m1.group(4);
			String year = m1.group(5);
			s = s.replaceFirst(regex, "%s, %s %s %s %s".formatted(weekday, dayOfMonth, month, year, time));
		}
		try {
			return ZonedDateTime.parse(s, DateTimeFormatter.RFC_1123_DATE_TIME);
		} catch (DateTimeParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	private String sanitizeId(String rawId) {
		String s = rawId.trim();
		int i = s.indexOf('<');
		if (i != -1) s = s.substring(i + 1);
		i = s.lastIndexOf('>');
		if (i != -1) s = s.substring(0, i);
		return s;
	}

	/**
	 * Gets the email that this handler has assembled after parsing is complete.
	 * @return The completed email.
	 */
	public Email getEmail() {
		return email;
	}
}
