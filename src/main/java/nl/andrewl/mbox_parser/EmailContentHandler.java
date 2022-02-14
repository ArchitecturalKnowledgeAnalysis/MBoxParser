package nl.andrewl.mbox_parser;

import org.apache.james.mime4j.MimeException;
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
			case "MESSAGE-ID" -> email.messageId = body;
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
		try {
			return ZonedDateTime.parse(s, DateTimeFormatter.RFC_1123_DATE_TIME);
		} catch (DateTimeParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Email getEmail() {
		return email;
	}
}
