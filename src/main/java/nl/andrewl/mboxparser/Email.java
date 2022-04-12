package nl.andrewl.mboxparser;

import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.ZonedDateTime;

public class Email {
	public String messageId;
	public String inReplyTo;
	public String sentFrom;
	public String subject;
	public ZonedDateTime date;

	public String mimeType;
	public String charset;
	public String transferEncoding;
	public byte[] body;

	public String readBodyAsText() {
		try {
			InputStream in = MimeUtility.decode(new ByteArrayInputStream(body), transferEncoding);
			return new String(in.readAllBytes(), Charset.forName(charset));
		} catch (IOException | MessagingException e) {
			e.printStackTrace();
			return null;
		}
	}
}
