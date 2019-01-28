package core.helpers.emailHelper;

import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class EmailSendHelper {

	/**
	 * sends email to recipient using email object
	 * 
	 * @throws Exception
	 */
	public static void sendMail(EmailObject email) {

		final String username = email.fromEmail;
		final String password = email.password;

		Properties props = new Properties();
		props.put("mail.smtp.auth", email.smtpAuth);
		props.put("mail.smtp.starttls.enable", email.smtpStarttlsEnabled);
		props.put("mail.smtp.host", email.smtpHost);
		props.put("mail.smtp.port", email.smtpPort);

		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(email.fromEmail));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email.toEmail));
			message.setSubject(email.subject);
			// message.setText(email.body);

			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText(email.body);

			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);

			// attachment
			messageBodyPart = new MimeBodyPart();
			String file = email.attachmentPath;
			String fileName = email.attachmentFile;
			DataSource source = new FileDataSource(file);
			messageBodyPart.setDataHandler(new DataHandler(source));
			messageBodyPart.setFileName(fileName);

			multipart.addBodyPart(messageBodyPart);
			message.setContent(multipart);
			Transport.send(message);

		} catch (MessagingException e) {
			e.printStackTrace();
		}

	}
}