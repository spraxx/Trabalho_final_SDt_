import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

import com.jcraft.jsch.*;

public class processManager extends UnicastRemoteObject implements ProcessManagerInterface {

    ArrayList<ProcessRequest> allReq = new ArrayList<>();
    ArrayList<ProcessRequest> waitL = new ArrayList<>();

    public processManager() throws RemoteException {}

    public ArrayList<ProcessRequest> allRequests() throws RemoteException {
        return allReq;
    }

    public ArrayList<ProcessRequest> waitList() throws RemoteException {
        return waitL;
    }

    public String ProcRequest (ProcessRequest pRequest) throws RemoteException {
        thread1 x;
        x = new thread1();
        int usageCPU = x.sumOfList();
        if(usageCPU < 60) {
            while(!waitL.isEmpty()){
                showCommand(pRequest);
                waitL.remove(0);
            }
            showCommand(pRequest);
        }

        else {
            System.out.println("O pedido: " + pRequest.getpScript() + " ficará em lista de espera");
            waitL.add(pRequest);
            System.out.println("A lista de espera tem : ["+waitL.size()+"] scripts por executar");
            // devido à falta de recursos, o pedido fica em lista de espera
        }

        String pid = UUID.randomUUID().toString();
        pRequest.setpId(pid);
        System.out.println("PID: " + pid);
        return pid;
        // geração de id e respetiva devolução
    }

    void showCommand (ProcessRequest pRequest){
        Process p1 = null;

        try{
            p1 = Runtime.getRuntime().exec(pRequest.getpScript());
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert p1 != null;
        InputStream inpStream = p1.getInputStream();
        Scanner s1 = new Scanner(inpStream).useDelimiter("\\A");
        String value = "";
        if(s1.hasNext()) {
            value = s1.next();
        }
        else {
            value = "";
        }

        System.out.println(value);
    }


    //Funcao para processar os pedidos

    private boolean ProcRequestFunc (ProcessRequest pRequest) {
        boolean sftpError = false;
        ChannelSftp channelSftp = null;

        try {
            channelSftp = setupJsch();
        }
        catch (JSchException e1) {
            System.out.println("1: " + e1.getMessage());
            sftpError = true;
        }

        try {
            channelSftp.connect();
        }
        catch (JSchException e2) {
            System.out.println("2: " + e2.getMessage());
            sftpError = true;
        }

        try{
            channelSftp.get(pRequest.getInFile(), "file/inFile/" + pRequest.getOutFile());
            System.out.println("Download Complete!");
        }
        catch (SftpException e3) {
            System.out.println("3: " + e3.getMessage());
            sftpError = true;
        }
        channelSftp.exit();

        if (sftpError) {
            System.out.println("getInFile falhou!");
            return false;
        }

        try {
            // Execucao da script
            String procRequestString = String.format("\"%s\" %s %s", pRequest.getpScript(),
                    ("file/inFile/" + pRequest.getInFile()),
                    ("file/outFile/" + pRequest.getOutFile())
            );
            Runtime.getRuntime().exec(procRequestString);

            File f1 = new File("file/outFile/" + pRequest.getOutFile());
            if (f1.exists()) {
                byte[] file = Files.readAllBytes(f1.toPath());
                //Converte f1 para byte array

                BrainModel model = new BrainModel(pRequest.getpId(), pRequest.getOutFile(), file);

                BrainManagerInterface bManInt;

                try {
                    bManInt = (BrainManagerInterface) Naming.lookup("rmi://localhost:2024/brain");
                    bManInt.saveModel(model);
                    //guarda o modelo no cerebro

                }
                catch (Exception e1) {
                    System.out.println("Processor saveModel() error: " + e1.getMessage());
                }
            }
        }
        catch (IOException e2) {
            System.out.println("Processor exec() error: " + e2.getMessage());
            return false;
        }
        return true;
    }


    private static ChannelSftp setupJsch() throws JSchException {
        String USERNAME = "user1";
        String PASSWORD = "user1";
        String HOST = "192.168.56.1";
        int PORT = 22;

        JSch jsch = new JSch();
        Session jschSession = jsch.getSession(USERNAME, HOST, PORT);

        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        jschSession.setConfig(config);
        jschSession.setPassword(PASSWORD);

        jschSession.connect();

        return (ChannelSftp) jschSession.openChannel("sftp");
    }

}
