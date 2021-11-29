import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.shell.InteractiveProcessShellFactory;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;

public class SSHServer {

    private SshServer sshd;

    SSHServer(int port, String user, String pass, String host) {
        sshd = SshServer.setUpDefaultServer();
        sshd.setPort(24000);
        sshd.setHost("localhost");
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(Paths.get("hostkey.ser")));
        sshd.setShellFactory(new InteractiveProcessShellFactory());
        sshd.setPasswordAuthenticator((username, password, serverSession) -> username.equals("sftpLoader") && password.equals("password"));
        sshd.setSubsystemFactories(Collections.singletonList(new SftpSubsystemFactory()));
        VirtualFileSystemFactory fileSystemFactory = new VirtualFileSystemFactory();
        fileSystemFactory.setDefaultHomeDir(Paths.get("C:\\Users\\Ricardo\\Documents\\GitHub\\Trabalho_final_SDt_\\Carregador"));
        sshd.setFileSystemFactory(fileSystemFactory);
    }

    public void start() throws IOException, InterruptedException {
        sshd.start();
        Thread.sleep(Long.MAX_VALUE);
    }

    public int getPort(){
        return sshd.getPort();
    }
}
