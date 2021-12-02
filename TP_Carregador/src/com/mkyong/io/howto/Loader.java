package com.mkyong.io.howto;

import com.jcraft.jsch.*;

public class Loader {
    private static final String REMOTE_HOST = "192.168.56.1";
    private static final String USERNAME = "user1";
    private static final String PASSWORD = "user1";
    private static final int REMOTE_PORT = 22;
    private static final int SESSION_TIMEOUT = 15000;
    private static final int CHANNEL_TIMEOUT = 5000;

    public static void main(String[] args) {

        //upload dos scripts para o server SFTP

        String s1= "\\scripts\\_script1.bat";
        s1 = s1.replace("\\", "/");

        String s2= "\\scripts\\_script2.bat";
        s2 = s2.replace("\\", "/");

        String s3= "\\scripts\\_script3.bat";
        s3 = s3.replace("\\", "/");

        String remoteFile = "\\";
        remoteFile= remoteFile.replace("\\", "/");

        Session jschSession = null;

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
            channelSftp.put(s1, remoteFile);
            System.out.println("Carregado script 1!");

            channelSftp.put(s2, remoteFile);
            System.out.println("Carregado script 2!");

            channelSftp.put(s3, remoteFile);
            System.out.println("Carregado script 3!");

            System.out.println("Ficheiros carregados com sucesso!");
        }
        catch (JSchException | SftpException ignored) {}
        finally
        {
            if (jschSession != null) {
                jschSession.disconnect();
            }
        }
    }
}
