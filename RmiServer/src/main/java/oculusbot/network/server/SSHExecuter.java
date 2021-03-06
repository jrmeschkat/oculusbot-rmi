package oculusbot.network.server;

import java.io.IOException;
import java.io.InputStream;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * Class that allows to execute commands on a remote machine using SSH.
 * 
 * @author Robert Meschkat
 *
 */
public class SSHExecuter {
	private Session session;

	/**
	 * Starts a SSH-session so commands can be send.
	 * 
	 * @param user
	 *            User that will execute the commands on the host
	 * @param password
	 *            Password that will complete the credentials
	 * @param hostname
	 *            Host that will execute the commands
	 */
	public SSHExecuter(String user, String password, String hostname) {
		JSch jsch = new JSch();
		try {
			session = jsch.getSession(user, hostname);
			session.setPassword(password);
			session.setConfig("StrictHostKeyChecking", "no");
			session.connect();
		} catch (JSchException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Closes the SSH-session.
	 */
	public void disconnect() {
		session.disconnect();
	}

	/**
	 * Executes a command.
	 * 
	 * @param command
	 *            Command that will be executed
	 * @return Output of the command
	 */
	public String sendCommand(String command) {
		StringBuffer buffer = new StringBuffer();
		try {
			//execute the command
			ChannelExec channel = (ChannelExec) session.openChannel("exec");
			channel.setCommand(command);
			channel.connect();
			channel.disconnect();
		} catch (JSchException e) {
			e.printStackTrace();
		} 

		return buffer.toString();
	}
}
