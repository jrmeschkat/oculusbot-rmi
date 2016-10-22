package oculusbot.network.server;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SSHExecuter {
	private Session session;
	
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

	public void disconnect() {
		session.disconnect();
	}

	public String sendCommand(String command) {
		StringBuffer buffer = new StringBuffer();
		try {
			ChannelExec channel = (ChannelExec) session.openChannel("exec");
			channel.setCommand(command);
			channel.connect();
//			InputStream in = channel.getInputStream();
//			int read = in.read();
//			while (read != -1) {
//				buffer.append((char) read);
//				read = in.read();
//			}
			channel.disconnect();
		} catch (JSchException e) {
			e.printStackTrace();
		}
//		catch (IOException e) {
//			e.printStackTrace();
//		}

		return buffer.toString();
	}
}
