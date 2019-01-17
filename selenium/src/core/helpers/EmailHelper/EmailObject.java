package core.helpers.emailHelper;

public class EmailObject {

	public String toEmail;
	public String password;
	public String fromEmail;
	public String recipientEmail;
	public String attachmentFile;
	public String attachmentPath;
	public String subject;
	public String body;
	public String smtpHost;
	public String smtpPort;
	public Boolean smtpStarttlsEnabled;
	public Boolean smtpAuth;

	public EmailObject withToEmail(String toEmail) {
		this.toEmail = toEmail;
		return this;
	}

	public EmailObject withPassword(String password) {
		this.password = password;
		return this;
	}

	public EmailObject withFromEmail(String fromEmail) {
		this.fromEmail = fromEmail;
		return this;
	}

	public EmailObject withRecipientEmail(String recipientEmail) {
		this.recipientEmail = recipientEmail;
		return this;
	}

	public EmailObject withAttachmentFile(String attachmentFile) {
		this.attachmentFile = attachmentFile;
		return this;
	}

	public EmailObject withAttachmentPath(String attachmentPath) {
		this.attachmentPath = attachmentPath;
		return this;
	}

	public EmailObject withSubject(String subject) {
		this.subject = subject;
		return this;
	}

	public EmailObject withBody(String body) {
		this.body = body;
		return this;
	}
	
	public EmailObject withSmtpHost(String smtpHost) {
		this.smtpHost = smtpHost;
		return this;
	}
	
	public EmailObject withSmtpPort(String smtpPort) {
		this.smtpPort = smtpPort;
		return this;
	}
	
	public EmailObject withSmtpStarttlsEnabled(Boolean smtpStarttlsEnabled) {
		this.smtpStarttlsEnabled = smtpStarttlsEnabled;
		return this;
	}
	
	public EmailObject withSmtpAuth(Boolean smtpAuth) {
		this.smtpAuth = smtpAuth;
		return this;
	}
}