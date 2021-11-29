import com.jcraft.jsch.JSch;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.JSchException;
import java.io.File;
import java.util.Properties;

public class SSHClient {

    private JSch jsch;
    private ChannelSftp channelSftp;

    SSHClient(String username,String host, String password, int port) throws JSchException
    {
        jsch = new JSch();
        Session jschSession = jsch.getSession(username, host,port);
        jschSession.setPassword(password);

        Properties config = new Properties();
        config.put("kex", "diffie-hellman-group1-sha1,diffie-hellman-group14-sha1,diffie-hellman-group-exchange-sha1,diffie-hellman-group-exchange-sha256,ssh-dss");
        config.put("StrictHostKeyChecking", "no");
        jschSession.setConfig(config);
        jschSession.connect();
        channelSftp = (ChannelSftp) jschSession.openChannel("sftp");
        channelSftp.connect();
    }

    public File get(String path, String destino) throws SftpException {
        channelSftp.get(path, destino);
        return new File(destino);
    }
}
