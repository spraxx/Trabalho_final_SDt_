import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class DataManager extends UnicastRemoteObject implements DataManagerInterface {

    SSHServer sshServer;

    protected DataManager() throws IOException {
        sshServer = new SSHServer(24000, "sftploader","password", "localhost");
        new Thread(()->{
            try {
                sshServer.start();
            }
            catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private String getRequest(String url) throws IOException {
        StringBuilder res = new StringBuilder();
        URL u = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) u.openConnection();
        connection.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream())))
        {
            for(String line; (line = reader.readLine())!=null;)
            {
                res.append(line);
            }
        }
        return res.toString();
    }

    private File writeResFile(String s1, ProcessRequest p){
        try {
            String nome = p.getpScript().split("http://")[1] + "_" + java.time.LocalDateTime.now();
            nome = nome.replace(".","_");
            nome = nome.replace(":","-");

            File f = new File(nome + ".processorrequest" );
            FileWriter fW = new FileWriter(f);
            fW.write(s1);
            fW.close();
            return f;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public LoaderAnswer execRequest(ProcessRequest pRequest) throws IOException {
        String s2 = null;
        try {
            s2 = getRequest(pRequest.getpScript());
        }
        catch (IOException e)
        {
            e.printStackTrace();
            s2 = "IOException";
        }

        File f = writeResFile(s2, pRequest);

        if (f != null)
        {
            return new LoaderAnswer(f);
            //enviar por sftp
        }
        else
        {
            return new LoaderAnswer(new File("error"));
            //enviar erro
        }
    }
}



















