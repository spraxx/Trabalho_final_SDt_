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

    ArrayList<ProcessRequest> allReq; //array com todos os pedidos
    ArrayList<ProcessRequest> waitL; //array com pedidos em espera
    ArrayList<String> aliveProc; //array com processadores ativos
    HashMap<String, ArrayList<ProcessRequest>> ProcQueue = new HashMap<>(); //hashmap com as filas dos outros processadores

    int id_processador;
    int state = 0;
    int queue_port;
    boolean setup = true;
    String delimiter = "#";
    String reqDelimit = "%";


    public processManager(int idProc, int hb_port, int qp, int dp, int sp) throws RemoteException {
        this.allReq = new ArrayList<>();
        this.waitL = new ArrayList<>();
        this.aliveProc = new ArrayList<>();

        this.id_processador = idProc;
        this.aliveProc.add(String.valueOf(id_processador)); //Adiciona-se a si mesmo ao array com processadores ativos
        this.queue_port = qp;


        Thread t1 = new Thread(new Runnable() {
            public void run() {
                try { CheckQueue(); } catch (Exception ignored) {}}
        });

        Thread t2 = new Thread(new Runnable() {
            public void run() {
                try { SendHeartbeat(hb_port); } catch (IOException | InterruptedException ignored) { }}
        });

        Thread t3 = new Thread(new Runnable() {
            public void run() {
                try { ProcListener(hb_port); } catch (IOException | InterruptedException e) { e.printStackTrace(); }}
        });

        Thread t4 = new Thread(new Runnable() {
            public void run() {
                try { QueueListener(qp); } catch (IOException e) { e.printStackTrace(); }}
        });

        Thread t5 = new Thread(new Runnable() {
            public void run() {
                try { DeadListener(dp); } catch (IOException e) { e.printStackTrace(); } }
        });

        Thread t6 = new Thread(new Runnable() {
            public void run() {
                try { StabilizerListener(sp); } catch (IOException | InterruptedException e) { e.printStackTrace(); }}
        });

        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t5.start();
        t6.start();
    }

    public ArrayList<ProcessRequest> allRequests() throws RemoteException{
        return allReq;
    }
    public ArrayList<ProcessRequest> waitList() throws RemoteException{
        return waitL;
    }

    //-------------------------------------------processamento de pedidos-------------------------------------------//

    public String ProcRequest (ProcessRequest pRequest) throws Exception {
        thread1 x;
        x = new thread1();
        int usageCPU = x.sumOfList();

        String pid = UUID.randomUUID().toString(); //geração de id
        pRequest.setpId(pid);
        allReq.add(pRequest); //adiciona o pedido ao array de todos os pedidos

        if(usageCPU < 40) { //se há recursos para processar o pedido
            while(!waitL.isEmpty()){
                ExecRequest(pRequest);
                ProcRequestFunc(pRequest);
                waitL.remove(0);
            }
            ExecRequest(pRequest);
            ProcRequestFunc(pRequest);
        }
        else { //se não há recursos para processar o pedido
            System.out.println("O pedido: " + pRequest.getpScript() + " ficará em lista de espera");
            waitL.add(pRequest);
            System.out.println("A lista de espera tem : ["+waitL.size()+"] scripts por executar");
            // devido à falta de recursos, o pedido fica em lista de espera
            try{
                sendQueue(queue_port, "+", pRequest.getpScript(), pRequest.getpId()); //envia o pedido na sua fila
            }
            catch (IOException | InterruptedException ignored) { }
        }
        System.out.println("PID: " + pid);
        return pid;
    }


    //----------------------------------------------execução de pedidos----------------------------------------------//

    void ExecRequest (ProcessRequest pRequest) throws Exception {
        Process p1 = null;
        getFileRebex(pRequest.getFile()); //download file

        try {
            p1 = Runtime.getRuntime().exec(pRequest.getpScript());
        }
        catch (IOException e) { e.printStackTrace(); }

        assert p1 != null;
        InputStream inpStream = p1.getInputStream();
        Scanner s1 = new Scanner(inpStream).useDelimiter("\\A");
        String value;
        if(s1.hasNext()) { value = s1.next(); }
        else { value = ""; }
        System.out.println(value);
    }


    //----------------------------------------------conteúdo da script----------------------------------------------//

    public String ScriptText(int file) {
        String file1 = file + ".txt";
        String content = null;
        try
        {
            content = new String ( Files.readAllBytes( Paths.get(file1) ) );
        }
        catch (IOException e) { e.printStackTrace(); }

        System.out.println("conteúdo da script: " + content);
        return content;
    }


    //---------------------------------------processamento de pedidos cérebro---------------------------------------//

    private void ProcRequestFunc (ProcessRequest pRequest) throws RemoteException, MalformedURLException, NotBoundException, InterruptedException {
        state = 1;

        int iScript = Integer.parseInt(pRequest.getpScript().substring(pRequest.getpScript().length() - 1).trim());
        //o último caracter da script é convertido para int, como sendo o numero do script
        Thread.sleep(1000);
        String model = ScriptText(iScript); //gera o modelo

        BrainManagerInterface brainManInt = (BrainManagerInterface) Naming.lookup("rmi://localhost:2024/brain");
        try {
            brainManInt.NewModel(model, String.valueOf(id_processador), pRequest.getpId());
            //envia modelo gerado ao cérebro
            brainManInt.ModelRequest(pRequest.getpId());
        }
        catch (Exception ignored) {
            System.out.println("Modelo gerado: " + model);
            File fDelete = new File(iScript + ".txt");
            fDelete.delete();
            state = 0;
        }
    }


    //------------------------------------------------envia broadcast------------------------------------------------//

    private void sendBroadcast(int port, String msg) throws IOException, InterruptedException {
        DatagramSocket socket = new DatagramSocket();
        InetAddress group = InetAddress.getByName("230.0.0.0");
        byte[] buffer = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
        socket.send(packet);
        socket.close();
    }


    //--------------------------------------------------envia fila--------------------------------------------------//

    private void sendQueue(int port, String flag, String script, String id) throws IOException, InterruptedException {
        String msg = id_processador + "," + flag + "," + script + "," + id;
        System.out.println("fila: " + msg);
        sendBroadcast(port, msg);
    }


    //-------------------------------------------------verifica fila-------------------------------------------------//

    public void CheckQueue() throws IOException, InterruptedException {
        thread1 x;
        x = new thread1();
        int usageCPU = x.Media();

        while(true) {
            Thread.sleep(5000); //A cada 5 segundos verifica a lista de espera

            if(usageCPU <= 40 && !waitL.isEmpty()) //se houver recursos e requests na waitlist
            {
                System.out.println("A processar script na fila com ID: " + waitL.get(0).getpScript());
                ProcessRequest pRequest = waitL.get(0);
                waitL.remove(0);
                sendQueue(queue_port, "-", pRequest.getpScript(), pRequest.getpId());
                //envia a confirmar o processamento do request na fila
                System.out.println("\nScripts da fila de espera processados com sucesso!");
                try {
                    ProcRequest(pRequest);
                }
                catch (MalformedURLException | NotBoundException ignored) {} catch (Exception e) { e.printStackTrace(); }
            }
        }
    }


    //------------------------------escuta processadores que fazem Setup e estão ativos------------------------------//

    private void ProcListener(int port) throws IOException, InterruptedException {
        MulticastSocket socket1 = null;
        byte[] buffer = new byte[256];
        socket1 = new MulticastSocket(port);
        InetAddress group = InetAddress.getByName("230.0.0.0");
        socket1.joinGroup(group);

        while (true) {
            DatagramPacket packet1 = new DatagramPacket(buffer, buffer.length);
            socket1.receive(packet1);
            String msg = new String(packet1.getData(), 0, packet1.getLength());
            String[] parts = msg.split(",");
            if(parts[0].equals("setup") && !parts[1].equals(id_processador))
                //Se o processador faz setup e se a mensagem não for do próprio processador
            {
                System.out.println("Processador: [" + parts[1] + "] fez Setup! Adicionando a aliveProc...");
                aliveProc.add(parts[1]);
                sendBroadcast(4446, "alive," + id_processador); //anuncia que está ativo
            }
            if(parts[0].equals("alive") && !parts[1].equals(id_processador)) //se o anúncio vier de outro processador
            {
                System.out.println("Processador [" + parts[1] + "] está ativo! Adicionando a aliveProc...");
                aliveProc.add(parts[1]); //o processador adiciona-o à fila de processadores ativos
            }
        }
    }


    //------------------------------escuta se outros processadores deixam de funcionar------------------------------//

    private void DeadListener(int port) throws IOException {
        MulticastSocket socket1 = null;
        byte[] buffer = new byte[256];
        socket1 = new MulticastSocket(port);
        InetAddress group = InetAddress.getByName("230.0.0.0");
        socket1.joinGroup(group);

        while (true) {
            DatagramPacket packet1 = new DatagramPacket(buffer, buffer.length);
            socket1.receive(packet1);
            String msg = new String(packet1.getData(), 0, packet1.getLength());
            if(aliveProc.contains(msg)) //se o processador consta na lista aliveProc
            {
                System.out.println("Processador: [" + msg + "] falhou!");
                aliveProc.remove(msg);
                System.out.println("Processador removido da lista de processadores ativos.");
            }
            else //se o processador não consta na lista aliveProc
            { System.out.println("Processador: [" + msg + "] falhou, mas não consta na lista."); }
        }
    }


    //------------------------------------escuta estabilizadores a tentar iniciar------------------------------------//

    private void StabilizerListener(int port) throws IOException, InterruptedException {
        MulticastSocket socket1 = null;
        byte[] buffer = new byte[256];
        socket1 = new MulticastSocket(port);
        InetAddress group = InetAddress.getByName("230.0.0.0");
        socket1.joinGroup(group);

        while (true) {
            DatagramPacket packet1 = new DatagramPacket(buffer, buffer.length);
            socket1.receive(packet1);
            String msg = new String(packet1.getData(), 0, packet1.getLength());
            if(msg.equals("stab_setup")) //mensagem enviada pelo estabilizador
            {
                System.out.println("Novo estabilizador a querer inicializar");
                //envia os processadores ativos que fizeram setup
                for (int i = 0, aliveProcSize = aliveProc.size(); i < aliveProcSize; i++) {
                    String s = aliveProc.get(i);
                    System.out.println("Processador ativo: " + s);
                    sendBroadcast(port, s);
                    Thread.sleep(500);
                }
            }
        }
    }


    //----------------------------------------------tratamento de filas----------------------------------------------//

    //String mensagem = id_processador + "," + type + "," + state + "," + CPU + "," + waitL.size() + "," + WaitListToString();

    public void QueueProcessing(String msg) {
        ArrayList<ProcessRequest> list = new ArrayList<>();
        String[] part = msg.split(",");
        ProcessRequest pRequest = new ProcessRequest(part[2], "test123.txt");
        pRequest.setpId(part[3]);

        if (ProcQueue.containsKey(part[0])) { //se este processador já tiver a fila registada
            if (part[1].equals("+")) //se flag = '+', adiciona pedido à fila
            {
                ProcQueue.get(part[0]).add(pRequest);
                System.out.println("Pedido: " + part [3] + " adicionado à fila do processador: " + part[0]);
            }
            else if (part[1].equals("-")) //se flag = '-', remove pedido da fila
            {
                boolean delete = false;
                for(int i = 0; i < ProcQueue.get(part[0]).size(); i++)
                {
                    if(ProcQueue.get(part[0]).get(i).getpId().equals(part[3])) //se existir o pedido a eliminar
                    {
                        ProcQueue.get(part[0]).remove(i);
                        delete = true;
                        System.out.println("Pedido: " + part[3] + " eliminado da fila do processador: " + part[0]);
                    }
                }
                if(!delete) { //se não existir o pedido a eliminar
                    System.out.println("Pedido a eliminar: " + part[3] + " não existe na fila do processador: " + part[0]);
                }
            }
            else { System.out.println("Flag inválida. Sem alterações na fila"); }
        }
        else //se este processador não tiver a fila ainda registada
        {
            if (part[1].equals("+")) //se flag = '+', adiciona pedido à fila
            {
                list.add(pRequest);
                ProcQueue.put(part[0], list);
            }
            else if (part[1].equals("-")) { //se flag = '-', remove pedido. Como não tem fila registada, não pode eliminar nada
                System.out.println("Processador sem fila registada. Nada a eliminar");
            }
            else { System.out.println("Flag inválida. Sem alterações na fila"); }
        }
    }


    //---------------------------------escuta/armazena filas de outros processadores---------------------------------//

    private void QueueListener(int port) throws IOException {
        MulticastSocket socket1 = null;
        byte[] buffer = new byte[256];
        socket1 = new MulticastSocket(port);
        InetAddress group = InetAddress.getByName("230.0.0.0");
        socket1.joinGroup(group);

        while (true) {
            DatagramPacket packet1 = new DatagramPacket(buffer, buffer.length);
            socket1.receive(packet1);
            String msg = new String(packet1.getData(), 0, packet1.getLength());
            System.out.println("Escuta fila: " + msg);
            QueueProcessing(msg);
        }
    }


    //----------------------------------novo processador continua pedidos do que falhou-------------------------------//

    public void ResumeProc(String deadProcID) throws MalformedURLException, NotBoundException, RemoteException {
        System.out.println("O processador com ID: " + deadProcID + " falhou.");
        BrainManagerInterface brainManInt = (BrainManagerInterface) Naming.lookup("rmi://localhost:2024/brain");

        if(ProcQueue.containsKey(deadProcID))
        {
            System.out.println("O processador com ID: " + deadProcID + " tinha pedidos na fila");
            ArrayList<ProcessRequest> dead_queue = ProcQueue.get(deadProcID); //fila do processador que falhou

            for (ProcessRequest processRequest : dead_queue) { //para cada pedido da fila
                if (!brainManInt.ModelGenerated(processRequest.getpId())) //se o modelo para este pedido ainda não foi gerado
                {
                    System.out.print("É preciso processar o pedido: " + processRequest.getpId());
                    waitL.add(processRequest); //este processador adiciona o pedido à sua própria fila
                }
            }
        }
        else
        {
            System.out.println("O procesador com ID: " + deadProcID + " não tinha pedidos na fila");
        }
    }


    //----------------------------------------------waitlist to string-----------------------------------------------//

    private String append(String delimiter, String id, String query, String file, String reqDelimit){
        return delimiter + id + reqDelimit + query + reqDelimit + file + reqDelimit;
    }

    private String WaitListToString() {
        StringBuilder Stb = new StringBuilder();
        for (ProcessRequest entry: waitL)
        {
            Stb.append(append(
                    delimiter,
                    entry.getpId(),
                    entry.getpScript(),
                    entry.getFile(),
                    reqDelimit)
            );
        }
        return Stb.toString();
    }

    
    //---------------------------------------------------heartbeat---------------------------------------------------//

    void SendHeartbeat(int port) throws IOException, InterruptedException {
        int i = 1;
        String type;

        while(true)
        {
            thread1 x;
            x = new thread1();
            int CPU = x.Media();

            type = "hb";
            if(setup)
            {
                type = "setup";
                setup = false;
            }
            String mensagem = id_processador + "," + type + "," + state + "," + CPU + "," + waitL.size() + "," + WaitListToString();
            /*String mensagem = i + " " + "TIPO: " + type + ", ESTADO: " + state + ", CPU: " + CPU + ", WAITLIST: "
                    + waitL.size() + " -> " + WaitListToString();*/
            // Se Estado = 1 o processador recebeu pelo menos um pedido
            System.out.println("MSG:" + mensagem + "]");
            //System.out.println("ProcessorID: " + id_processador + " [ MSG Nº:" + mensagem + "]");
            sendBroadcast(port, mensagem);
            Thread.sleep(10000); //Envia Heartbeat a cada 10 segundos
            i++;
        }
    }


    //---------------------------------------------download script Rebex---------------------------------------------//

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
            channelSftp.get("/" + file,"C:\\Users\\Ricardo\\IdeaProjects\\FilesRebex"); //file download
                    System.out.println("Download feito com sucesso!\n");
            session.disconnect();
        }
        catch (JSchException | SftpException e){
            e.printStackTrace();
        }
    }
}