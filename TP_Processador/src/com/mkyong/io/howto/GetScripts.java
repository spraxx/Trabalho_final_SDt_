package com.mkyong.io.howto;
import com.jcraft.jsch.*;

import java.io.IOException;

public class GetScripts {
    private static final String REMOTE_HOST = "192.168.1.119";
    private static final String USERNAME = "Root123";
    private static final String PASSWORD = "Root123";
    private static final int REMOTE_PORT = 22;
    private static final int SESSION_TIMEOUT = 10000;
    private static final int CHANNEL_TIMEOUT = 5000;

    //classe que faz conexão SFTP

    public void ProcessScript(String script) throws IOException {
        Session jschSession = null;

        String remoteFile = "\\";
        remoteFile= remoteFile.replace("\\", "/");

        script = script.replaceAll("\\s", "");
        script = script + ".bat";

        // ex: string "script 1" para "script1.bat"

        try{

            JSch jsch = new JSch();
            jsch.setKnownHosts("C:\\Users\\Ricardo\\.ssh\\known_hosts");
            remoteFile= remoteFile.replace("\\", "/");
            jschSession = jsch.getSession(USERNAME, REMOTE_HOST, REMOTE_PORT);

            jschSession.setPassword(PASSWORD);
            jschSession.connect(SESSION_TIMEOUT);
            Channel sftp = jschSession.openChannel("sftp");
            sftp.connect(CHANNEL_TIMEOUT);
            ChannelSftp channelSftp = (ChannelSftp) sftp;

            channelSftp.get(script, script);

        }
        catch (JSchException | SftpException ignored) {}
        finally { if (jschSession != null) { jschSession.disconnect(); } }
        String path = "cmd.exe /c start " + script;
        Process p =  Runtime.getRuntime().exec(path);
        System.out.println("Script Processado: " + script);
    }
}
