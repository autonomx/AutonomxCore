package core.apiCore.helpers;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import core.support.configReader.Config;
import core.support.logger.TestLog;

public class ConnectionHelper {

	private static final String SSH_USER = "ssh.user";
	private static final String SSH_PASSWORD = "ssh.password";
	private static final String SSH_HOST = "ssh.host";
	private static final String SSH_PORT = "ssh.port";
	private static final String SSH_LPORT = "ssh.lport";
	private static final String SSH_RHOST = "ssh.rHost";
	private static final String SSH_RPORT = "ssh.rport";

	/**
	 * ssh connection supports port forwarding as well if host is empty, Then return
	 * if rhost is empty, return for port forwarding
	 */
	public static void sshConnect() {
		String user = Config.getValue(SSH_USER);
		String password = Config.getValue(SSH_PASSWORD);
		String host = Config.getValue(SSH_HOST);
		int port = Config.getIntValue(SSH_PORT);

		if (host.isEmpty())
			return;

		Session session = null;
		try {
			JSch jsch = new JSch();
			if (port != -1)
				session = jsch.getSession(user, host, port);
			else
				session = jsch.getSession(user, host);

			session.setPassword(password);
			session.setConfig("StrictHostKeyChecking", "no");
			TestLog.ConsoleLog("Establishing Connection...");
			session.connect();
			setPortForwarding(session);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void setPortForwarding(Session session) throws JSchException {
		int lport = Config.getIntValue(SSH_LPORT);
		String rhost = Config.getValue(SSH_RHOST);
		int rport = Config.getIntValue(SSH_RPORT);

		if (!rhost.isEmpty()) {
			int assinged_port = -1;
			assinged_port = session.setPortForwardingL(lport, rhost, rport);

			TestLog.ConsoleLog("Port forwarding: assigned port: " + assinged_port + " -> " + rhost + ":" + rport);
		}
	}
}
