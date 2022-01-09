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
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.jcraft.jsch.*;

public class processManager extends UnicastRemoteObject implements ProcessManagerInterface {

    ArrayList<ProcessRequest> allReq = new ArrayList<>();
    Map<String, ProcessRequest> waitL = new ConcurrentHashMap<>();
    AbstractMap.SimpleEntry<UUID, ProcessRequest> currProcess = null;
    public synchronized AbstractMap.SimpleEntry<UUID, ProcessRequest> getCurrProcess(){ return currProcess; }
    String delimiter = " ## ";
    String reqDelimit = " - ";
    Integer id_processador;
    int state = 0;
    int port;


    public processManager(String idProc, int hb_port) throws RemoteException {
        this.id_processador = Integer.parseInt(idProc);
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
                try {
                    SendHeartbeat(hb_port);
                    SendDataRestore();  // ativar para fazer restore
                }
                catch (IOException | InterruptedException ignored) { }
            }
        });

        t1.start();
        t2.start();
    }

    public ArrayList<ProcessRequest> allRequests() throws RemoteException {
        return allReq;
    }



    //-----------------------------------processamento de pedidos-----------------------------------//

    public String ProcRequest (ProcessRequest pRequest) throws Exception {
        thread1 x;
        x = new thread1();
        int usageCPU = x.sumOfList();

        String pid = UUID.randomUUID().toString();
        pRequest.setpId(pid);

        if(usageCPU < 40) {
            while(!waitL.isEmpty()){
                ExecRequest(pRequest);
                ProcRequestFunc(pRequest);
                waitL.remove(0);
            }
            ExecRequest(pRequest);
            ProcRequestFunc(pRequest);
        }

        else {
            System.out.println("O pedido: " + pRequest.getpScript() + " ficará em lista de espera");
            waitL.put(pid, pRequest);
            System.out.println("A lista de espera tem : ["+waitL.size()+"] scripts por executar");
            // devido à falta de recursos, o pedido fica em lista de espera
        }

        System.out.println("PID: " + pid);
        return pid;
        // geração de id e respetiva devolução
    }


    //-----------------------------------execução de pedidos-----------------------------------//

    void ExecRequest (ProcessRequest pRequest) throws Exception {
        Process p1 = null;

        getFileRebex(pRequest.getFile());
        //download file

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
        int iScript;
        try {
            iScript = Integer.parseInt(pRequest.getpScript().substring(pRequest.getpScript().length() - 1).trim());
            Thread.sleep(1000);
            String model = ScriptText(iScript);

            BrainManagerInterface brainManInt = (BrainManagerInterface) Naming.lookup("rmi://localhost:2024/brain");
            try {
                brainManInt.NewModel(model, String.valueOf(id_processador), pRequest.getpId());
                brainManInt.ModelRequest(pRequest.getpId());

            } catch (Exception ignored) {
                state = 0;
            }
        }
        catch (NumberFormatException e)
        { iScript = 0; }
    }


    //-----------------------------------verifica fila-----------------------------------//

    public void CheckQueue() throws RemoteException, InterruptedException {
        thread1 x;
        x = new thread1();
        int usageCPU = x.Media();

        while(true)
        {
            Thread.sleep(5000);
            //A cada 5 segundos verifica a fila de espera

            if(usageCPU <= 40 && !waitL.isEmpty())
            {
                System.out.println("A processar script na fila com ID: " + waitL.get(0).getpScript());
                ProcessRequest pRequest = waitL.get(0);
                waitL.remove(0);
                System.out.println("\nScripts da fila de espera processados com sucesso!");
                try
                {
                    ProcRequest(pRequest);
                }
                catch (MalformedURLException | NotBoundException ignored) {} catch (Exception e) {
                    e.printStackTrace();
                }
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


    //-----------------------------------novo processador continua-----------------------------------//

    public void ResumeProc(String reqList) throws RemoteException {

        String[] arrayPedidos = reqList.split(delimiter);
        for (String request: arrayPedidos) {
            if(!request.isEmpty()) {

                String St1 = request.split(reqDelimit)[0],
                        query = request.split(reqDelimit)[1],
                        ficheiro = request.split(reqDelimit)[2];

                if (!St1.contains("current:")) //waitlist do processador anterior
                {
                    waitL.put(St1, new ProcessRequest(query, ficheiro));
                }
                else {
                    waitL.put(St1.split(":")[1], new ProcessRequest(query, ficheiro));
                    //setIfImportantProcess(true);
                }
            }
        }
    }


    //-------------------------------lista processos existentes no processador-------------------------------//

    private String DataProc(String entryDelimit, String id, String query, String file, String reqDelimit)
    {
        return entryDelimit + id + reqDelimit + query + reqDelimit + file + entryDelimit;
        // cria uma lista dos processos existentes no processador
        // fica na forma -> ## id %% query %% file ##
    }


    //-----------------------------------waitlist to string-----------------------------------//

    private String WaitListToString() {
        StringBuilder Stb = new StringBuilder();
        for (Map.Entry<String, ProcessRequest> entry: waitL.entrySet())
        {
            Stb.append( DataProc( delimiter,     // ##
                    entry.getKey(),                 // id
                    entry.getValue().getpScript(),  // query
                    entry.getValue().getFile(),     // file
                    reqDelimit)                     // %%
            );
        }

        AbstractMap.SimpleEntry<UUID, ProcessRequest> currProc = getCurrProcess();
        if(currProc != null) {
            if (currProc.getValue() != null && currProc.getKey() != null) {
                Stb.append(DataProc(
                        delimiter,"current:" + currProc.getKey(), // insere "current:" no processo atual
                        currProc.getValue().getpScript(),
                        currProc.getValue().getFile(), reqDelimit)
                );
            }
        }
        return Stb.toString();
    }
    
    
    //-----------------------------------heartbeat-----------------------------------//

    void SendHeartbeat(int port) throws IOException, InterruptedException {

        int i = 1;

        while(true) {
            thread1 x;
            x = new thread1();
            int CPU = x.Media();

            String mensagem = i + ", ESTADO: " + state + ", CPU: " + CPU + ", WAITLIST: " + waitL.size() + " " + WaitListToString();
            // Se Estado = 1 o processador recebeu pelo menos um pedido
            // Assim dá para identificar o escolhido pelo Estabilizador
            System.out.println("ProcessorID: " + id_processador + " [ MSG Nº: " + mensagem + "]");

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


    //-----------------------------------envio de dados para restauro-----------------------------------//

    void SendDataRestore() throws IOException {
        thread1 x;
        x = new thread1();
        int CPUmedia = x.Media();

        HashMap<String, triple<Integer,String, String>> hashMap = new HashMap<>();
        MulticastSocket socket1 = new MulticastSocket(4446);
        InetAddress group = InetAddress.getByName("230.0.0.0");
        socket1.joinGroup(group);

        StringBuilder s = new StringBuilder();
        for(Map.Entry<String, triple<Integer, String, String>> entry : hashMap.entrySet())
        {
            String infoData =
                    entry.getKey() + ";"
                    + entry.getValue().first + ";"
                    + entry.getValue().second + ";"
                    + entry.getValue().third + delimiter;
            s.append(infoData);
        }
        s.append(id_processador + ":" + port + ";" + CPUmedia + ";" + getCurrProcess()+ ";" + delimiter);

        byte[] buffer1 = s.toString().getBytes();
        DatagramPacket packet = new DatagramPacket(buffer1, buffer1.length, group, 4446);
        socket1.send(packet);
        socket1.close();
    }


    //-----------------------------------download script Rebex-----------------------------------//

    public void getFileRebex(String file) {
        String USERNAME = "user1";
        String PASSWORD = "user1";
        String HOST = "192.168.56.1";
        int PORT = 30;
        int SESSION_TIMEOUT = 10000;
        int CHANNEL_TIMEOUT = 5000;

        JSch JSch = new JSch();

        try{
            Session session = JSch.getSession(USERNAME, HOST, PORT);
            session.setPassword(PASSWORD);
            //System.out.println("Sessão criada com sucesso!");

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
                    System.out.println("\nScript: " + entry.getFilename());
                    channelSftp.get("/" + entry.getFilename());
                    //faz download se o nome do script(2º parâmetro)
                    //for igual a um existente na pasta data do Rebex
                }
            }

            //file download
            channelSftp.get("/" + file,"C:\\Users\\Ricardo\\IdeaProjects\\FilesRebex");
                    System.out.println("Download feito com sucesso!\n");

            session.disconnect();

        }
        catch (JSchException | SftpException e){
            e.printStackTrace();
        }
    }


    //------------------------CLASS TRIPLE------------------------//

    static class triple<A,B,C>{
        A first;
        B second;
        C third;

        triple(A a,B b,C c){
            first = a;
            second = b;
            third = c;
        }
    }
}