package core.helpers.emailHelper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.SearchTerm;

import core.support.logger.TestLog;

public class EmailHelper {

	/**
	 * 
	 * @param message
	 * @param value
	 * @throws Exception
	 */
	public static void searchEmail(final EmailObject email) throws Exception {
		Properties props = System.getProperties();
		props.setProperty("mail.store.protocol", "imaps");

		Session session = Session.getDefaultInstance(props, null);
		Store store = session.getStore("imaps");
		store.connect("imap.gmail.com", email.toEmail, email.password);

		Folder folder = store.getFolder("INBOX");
		folder.open(Folder.READ_WRITE);

		TestLog.ConsoleLog("Total Message:" + folder.getMessageCount());
		TestLog.ConsoleLog("Unread Message:" + folder.getUnreadMessageCount());

		boolean isMailFound = false;
		Message mailFrom = null;

		// Search for mail
		// creates a search criterion
		@SuppressWarnings("serial")
		SearchTerm searchCondition = new SearchTerm() {

			@Override
			public boolean match(Message message) {
				try {
					if (message.getSubject().contains(email.subject)) {
						TestLog.ConsoleLog("message.getSubject()" + message.getSubject());
						return true;
					}
				} catch (MessagingException ex) {
					ex.printStackTrace();
				}
				return false;
			}
		};

		// performs search through the folder
		Message[] foundMessages = folder.search(searchCondition);

		// Search for unread mail from God
		// This is to avoid using the mail for which
		// Registration is already done
		for (Message mail : foundMessages) {
			mailFrom = mail;
			TestLog.ConsoleLog("Message Count is: " + mailFrom.getMessageNumber());
			isMailFound = true;
			break;
		}

		// Test fails if no unread mail was found from God
		if (!isMailFound) {
			throw new Exception("Could not find new mail");

			// Read the content of mail and launch registration URL
		} else {
			String line;
			StringBuffer buffer = new StringBuffer();
			BufferedReader reader = new BufferedReader(new InputStreamReader(mailFrom.getInputStream()));
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
			}

			// Your logic to split the message and get the Registration URL goes
			// here
			String registrationURL = buffer.toString().split("&amp;gt;http://www.god.de/members/?")[0]
					.split("href=")[1];
			TestLog.ConsoleLog(registrationURL);
		}
	}
}