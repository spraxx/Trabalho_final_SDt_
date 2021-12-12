import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

import com.jcraft.jsch.*;

public class processManager extends UnicastRemoteObject implements ProcessManagerInterface {

    ArrayList<ProcessRequest> allReq = new ArrayList<>();
    ArrayList<ProcessRequest> waitL = new ArrayList<>();
    private HashMap<String, ProcessorInfo> ProcInfo = new HashMap<>();

    String id_processador = UUID.randomUUID().toString();  //gera ID para o processador
    int state = 0;

    public processManager(int hb_port) throws RemoteException {
        Thread t1 = new Thread(new Runnable()
        {
            public void run()
            {
                try { CheckQueue(); }
                catch (Exception ignored) {}
            }
        });

        Thread t2 = new Thread(new Runnable()
        {
            public void run() {
                try { sendHeartbeat(hb_port); }
                catch (IOException | InterruptedException ignored) { }
            }
        });

        t1.start();
        t2.start();
    }

    public ArrayList<ProcessRequest> allRequests() throws RemoteException {
        return allReq;
    }

    public ArrayList<ProcessRequest> waitList() throws RemoteException {
        return waitL;
    }


    //-----------------------------------escuta de processadores-----------------------------------//

    private void ProcessorListener(int port) throws IOException {
        MulticastSocket socket1 = null;
        byte[] buf = new byte[256];

        socket1 = new MulticastSocket(port);
        InetAddress group = InetAddress.getByName("230.0.0.0");
        socket1.joinGroup(group);
        while (true) {
            DatagramPacket packet1 = new DatagramPacket(buf, buf.length);
            socket1.receive(packet1);
            String mensagem = new String(packet1.getData(), 0, packet1.getLength());
            System.out.println(mensagem);
            String[] partes = mensagem.split(",");
            ProcessorInfo pInfo = new ProcessorInfo();
            pInfo.setId(partes[1]);
            pInfo.setCpu(Float.parseFloat(partes[2]));
            pInfo.setMemory(Float.parseFloat(partes[3]));
            pInfo.setState(Integer.parseInt(partes[4]));
            pInfo.setQueue(Integer.parseInt(partes[5]));
            pInfo.setAddress(partes[6]);
            ProcInfo.put(partes[1], pInfo);
        }
    }


    //-----------------------------------processamento de pedidos-----------------------------------//

    public String ProcRequest (ProcessRequest pRequest) throws Exception {
        thread1 x;
        x = new thread1();
        int usageCPU = x.sumOfList();
        if(usageCPU < 60) {
            while(!waitL.isEmpty()){
                execRequest(pRequest);
                ProcRequestFunc(pRequest);
                waitL.remove(0);
            }
            execRequest(pRequest);
            ProcRequestFunc(pRequest);
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


    //-----------------------------------execução de pedidos-----------------------------------//

    void execRequest (ProcessRequest pRequest) throws Exception {
        Process p1 = null;

        getFileRebex(pRequest.getFile());

        try{
            p1 = Runtime.getRuntime().exec(pRequest.getpScript());
        }
        catch (IOException e) {
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


    //-----------------------------------processamento de pedidos cérebro-----------------------------------//

    private void ProcRequestFunc (ProcessRequest pRequest) throws RemoteException, MalformedURLException, NotBoundException, InterruptedException {

        state = 1;

        int iScript = Integer.parseInt(pRequest.getpScript().substring(pRequest.getpScript().length() - 1));
        Thread.sleep(1000);
        String model = ScriptText(iScript);

        BrainManagerInterface brainManInt = (BrainManagerInterface) Naming.lookup("rmi://localhost:2024/brain");
        try {
            brainManInt.NewModel(model, id_processador, pRequest.getpId());
        }
        catch (Exception ignored) {
            System.out.println("Modelo gerado: " + model);
            File f1 = new File(iScript + ".txt");
            f1.delete();

            state = 0;
        }
    }


    //-----------------------------------verifica fila-----------------------------------//

    public void CheckQueue() throws RemoteException, InterruptedException {
        thread1 x;
        x = new thread1();
        int usageCPU = x.sumOfList();

        while(true)
        {
            Thread.sleep(5000);
            System.out.println("A verificar fila de espera...");
            if(usageCPU < 60 && !waitL.isEmpty())
            {
                System.out.println("A processar script na fila: " + waitL.get(0).getpScript());
                ProcessRequest pRequest = waitL.get(0);
                waitL.remove(0);
                try {ProcRequestFunc(pRequest);}
                catch (MalformedURLException | NotBoundException ignored) {}
            }
        }
    }


    //-----------------------------------conteúdo da script-----------------------------------//

    public String ScriptText(int file)
    {
        String file1 = file + ".txt";
        String content = null;

        try
        {
            content = new String ( Files.readAllBytes( Paths.get(file1) ) );
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        System.out.println("conteúdo da script: " + content);
        return content;
    }


    //-----------------------------------heartbeat-----------------------------------//

    private void sendHeartbeat(int port) throws IOException, InterruptedException {

        int i = 0;
        boolean setup = true;
        String type = "hb";
        while (true) {
            if (setup) {
                type = "setup";
                setup = false;
            }

            String mensagem = i + ", TIPO: " + type + ", ESTADO: " + state + ", WAITLIST: " + waitL.size();
            System.out.println("MSG Nº: " + mensagem);

            DatagramSocket socket = new DatagramSocket();
            InetAddress group = InetAddress.getByName("230.0.0.0");
            byte[] buffer = mensagem.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
            socket.send(packet);
            socket.close();
            Thread.sleep(10000); //Envia Heartbeat a cada 10 segundos
            i++;
        }
    }

    //-----------------------------------download script Rebex-----------------------------------//

    public void getFileRebex(String file) throws Exception{
        String USERNAME = "user1";
        String PASSWORD = "user1";
        String HOST = "192.168.56.1";
        int PORT = 23;
        int SESSION_TIMEOUT = 10000;
        int CHANNEL_TIMEOUT = 5000;

        JSch JSch = new JSch();

        try{
            Session session = JSch.getSession(USERNAME, HOST, PORT);
            session.setPassword(PASSWORD);
            System.out.println("Sessão criada com sucesso!");

            java.util.Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect(SESSION_TIMEOUT);
            Channel channel = session.openChannel("sftp");
            channel.connect(CHANNEL_TIMEOUT);
            ChannelSftp channelSftp = (ChannelSftp)channel;

            Vector<ChannelSftp.LsEntry> vFiles = channelSftp.ls("/");

            for(ChannelSftp.LsEntry entry : vFiles){
                if(entry.getFilename().equals(file)){
                    System.out.println(entry.getFilename());
                    channelSftp.get("/" + entry.getFilename());
                    //faz download se o nome do script(2º parâmetro)
                    //for igual a um existente na pasta data do Rebex
                }
            }

            //file download
            channelSftp.get("/" + file,"C:\\Users\\Ricardo\\Documents\\ESCOLA\\_ESCOLA\\3ano\\SDt\\files");
                    System.out.println("Download feito com sucesso.");

            session.disconnect();

        }
        catch (JSchException | SftpException e){
            e.printStackTrace();
        }
    }


    //-----------------------------------delete script Rebex-----------------------------------//

    public static void deleteScriptRebex(String script) throws Exception{
        String USERNAME = "user1";
        String PASSWORD = "user1";
        String HOST = "192.168.56.1";
        int PORT = 25;
        int SESSION_TIMEOUT = 10000;
        int CHANNEL_TIMEOUT = 5000;

        JSch JSch = new JSch();

        try{
            Session session = JSch.getSession(USERNAME, HOST, PORT);
            session.setPassword(PASSWORD);
            System.out.println("Sessão criada com sucesso!");

            java.util.Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect(SESSION_TIMEOUT);
            Channel channel = session.openChannel("sftp");
            channel.connect(CHANNEL_TIMEOUT);
            ChannelSftp channelSftp = (ChannelSftp)channel;

            channelSftp.rm(script);
            System.out.println("Script apagado com sucesso");

            session.disconnect();

        }
        catch (JSchException e){
            e.printStackTrace();
        }
    }
}
