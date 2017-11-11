package core.helpers.EmailHelper;

public class EmailObject {

	public String email;
	public String password;
	public String subject;

	public EmailObject withEmail(String email) {
		this.email = email;
		return this;
	}

	public EmailObject withPassword(String password) {
		this.password = password;
		return this;
	}

	public EmailObject withSubject(String subject) {
		this.subject = subject;
		return this;
	}
}