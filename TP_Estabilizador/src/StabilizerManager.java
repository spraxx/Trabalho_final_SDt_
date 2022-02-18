import javax.annotation.processing.Processor;
import java.io.IOException;
import java.net.*;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.lang.*;
import java.util.concurrent.ConcurrentHashMap;

public class StabilizerManager extends UnicastRemoteObject implements StabilizerManagerInterface {

    private ArrayList<String> IDProc = new ArrayList<>(); //array de ID's de processadores
    private HashMap<String, iProcessor> InfoProc = new HashMap<>(); // hashmap de processadores ativos
    private HashMap<String, ArrayList<ProcessRequest>> ProcQueue = new HashMap<>(); //hashmap com as filas dos outros processadores


    public StabilizerManager() throws IOException {
        Thread t1 = new Thread(new Runnable() {
            public void run() {
                try { AliveProcListener(4445); } catch (IOException e) { e.printStackTrace(); } }
        });

        t1.start();

        DatagramSocket socket = new DatagramSocket();
        InetAddress group = InetAddress.getByName("230.0.0.0");
        byte[] buffer = "stab_setup".getBytes();
        //envia a mensagem para este estabilizador saber quais os processadores ativos
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, 4445);
        socket.send(packet);
        socket.close();

        Thread t2 = new Thread(new Runnable() {
            public void run() {
                try { Thread.sleep(5000); } catch (InterruptedException e) { e.printStackTrace(); }
                t1.stop();
                //setup terminado

                if(IDProc.size() == 0)
                {
                    System.out.println("Sem processadores ativos");
                }
            }
        });
        t2.start(); //termina setup após 5 segundos

        Thread t3 = new Thread(new Runnable() {
            public void run() {
                try { HeartbeatCatch(4446); } catch (IOException ignored) { } }
        });

        Thread t4 = new Thread(new Runnable() {
            public void run() {
                try { ActivityCheck(); } catch (NotBoundException | InterruptedException | IOException ignored) { } }
        });

        Thread t5 = new Thread(new Runnable() {
            public void run() {
                try { QueueCatch(4447); } catch (IOException ignored) { } }
        });

        t3.start();
        t4.start();
        t5.start();
    }


    //------------------------------------------escuta processadores ativos------------------------------------------//

    private void AliveProcListener(int port) throws IOException {
        MulticastSocket socket1 = null;
        byte[] buffer = new byte[256];
        socket1 = new MulticastSocket(port);
        InetAddress group = InetAddress.getByName("230.0.0.0");
        socket1.joinGroup(group);

        while (true) {
            DatagramPacket packet1 = new DatagramPacket(buffer, buffer.length);
            socket1.receive(packet1);
            String msg = new String(packet1.getData(), 0, packet1.getLength());
            if(!msg.equals("stab_setup")) //ignora a própria mensagem
            {
                System.out.println("Processador ativo: " + msg);
                if(!IDProc.contains(msg)) //vai receber ID's repetidos dado que todos os processadores vão responder
                {
                    IDProc.add(msg);
                }
            }
        }
    }


    //--------------------------------------------tratamento de heartbeats--------------------------------------------//

    private void HeartbeatProcess(String msg){
        String[] part = msg.split(","); //mensagem separada por vírgulas
        iProcessor p1 = new iProcessor();

        if (part[1].equals("setup")) { //se heartbeat = "setup", vamos adicionar o processador que o enviou à lista de IDs ativos
            IDProc.add(part[0]); //Array de IDs de processadores para utiliza posteriormente
            p1.setPid(part[0]);
            p1.setState(Integer.parseInt(part[2]));
            p1.setCpu(Float.parseFloat(part[3]));
            p1.setQueue(Integer.parseInt(part[4]));
            p1.setActive(true); //coloca os processadores como "ativos"
            InfoProc.put(part[0], p1); //HashMap <ID processador, dados processador>
        }

        if(part[1].equals("hb")){ //se heartbeat = "hb" vamos verificar se este processador já fez o setup
            if(IDProc.contains(part[0])) // se fez setup
            {
                p1.setPid(part[0]);
                p1.setState(Integer.parseInt(part[2]));
                p1.setCpu(Float.parseFloat(part[3]));
                p1.setQueue(Integer.parseInt(part[4]));
                InfoProc.put(part[0], p1);
                p1.setActive(true); //coloca os processadores como "ativos"
            }
            else { System.out.println("Processador não fez setup"); } //ignora mensagem
        }
    }


    //---------------------------------------------captura de heartbeats---------------------------------------------//

    private void HeartbeatCatch(int port) throws IOException {
        MulticastSocket socket1 = null;
        byte[] buffer = new byte[256];
        socket1 = new MulticastSocket(port);
        InetAddress group = InetAddress.getByName("230.0.0.0");
        socket1.joinGroup(group);
        // System.out.println("A capturar heartbeats...");

        while (true) {
            DatagramPacket packet1 = new DatagramPacket(buffer, buffer.length);
            socket1.receive(packet1);
            String msg1 = new String(packet1.getData(), 0, packet1.getLength()); //mensagem recebida
            System.out.println(msg1);
            HeartbeatProcess(msg1);
        }
    }


    //-----------------------------------------processador com mais recursos-----------------------------------------//

    public String ChosenOne() throws RemoteException {
        iProcessor p1 = new iProcessor();
        float cpu = 100;
        int queue = 100;
        if(InfoProc.isEmpty())
        {
            System.out.println("Sem processadores disponíveis!");
            return "NULL";
        }
        else
        {
            String theChosenOne = InfoProc.get(IDProc.get(0)).getPid();
            //o processador com mais recursos é considerado o primeiro da fila se nenhum estiver disponível

            for(int i = 0; i < IDProc.size() ; i++)
            {
                p1 = InfoProc.get(IDProc.get(i));
                if(p1.getState() == 0) //verifica apenas processadores que não estão a processar
                {
                    if(p1.getCpu() < cpu && p1.getQueue() < queue)
                    {
                        cpu = p1.getCpu();
                        queue= p1.getQueue();
                        theChosenOne = p1.getPid();
                    }
                }
            }
            System.out.println("Processador com mais recursos: " + theChosenOne);
            return theChosenOne; //devolve id do processador
        }
    }


    //---------------------------------------------notifica processador---------------------------------------------//

    private void AnnounceProcessor(String id_defunto) throws RemoteException, MalformedURLException, NotBoundException {
        String address = ChosenOne(); //vai buscar o endereço do processador com mais recursos
        System.out.println("A notificar o processador: " + address);
        ProcessManagerInterface procManInt = (ProcessManagerInterface) Naming.lookup(address);
        procManInt.ResumeProc(id_defunto);
        //processador com mais recursos vai tratar dos pedidos que o processador que falhou tinha na fila
    }


    //-----------------------------------verificar (in)atividade de processadores-----------------------------------//

    private void ActivityCheck() throws InterruptedException, IOException, NotBoundException {

        iProcessor p1 = new iProcessor();
        String id = null;

        while(true)
        {
            Thread.sleep(30000); //A cada trinta segundos...
            //System.out.println("A verificar atividade...");

            if(InfoProc.size() == 0) { System.out.println("A aguardar..."); } //se o HasMap ainda estiver vazio
            else{
                for(int i = 0; i < InfoProc.size() ; i++)
                {
                    p1 = InfoProc.get(IDProc.get(i));
                    if(!p1.getActive()) //se o processador estiver inativo
                    {
                        id = IDProc.get(i);
                        System.out.println("Processador com id: [" + id + "] falhou!");
                        System.out.println("A remover...");
                        InfoProc.remove(id);
                        IDProc.remove(id);
                        Thread.sleep(1000);

                        //anúncio da falha do processador
                        DatagramSocket socket = new DatagramSocket();
                        InetAddress group = InetAddress.getByName("230.0.0.0");
                        byte[] buffer = id.getBytes();
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, 4448);
                        socket.send(packet);
                        socket.close();

                        if(IDProc.isEmpty())
                        {
                            System.out.println("Não existem processadores ativos");
                        }
                        else { AnnounceProcessor(id); } //avisa o processador com mais recursos
                    }
                    else //se o processador estiver ativo
                    {
                        System.out.println("Processador com id: " + p1.getPid() + " está ativo");
                    }
                }
                for(int i = 0; i < InfoProc.size() ; i++)
                {
                    p1 = InfoProc.get(IDProc.get(i));
                    p1.setActive(false); //coloca os processadores como inativos
                    InfoProc.put(IDProc.get(i), p1);
                }
            }
        }
    }


    //--------------------------------------tratamento da fila de processadores--------------------------------------//

    //fila: [id processador , flag , script , id pedido]

    private void QueueProcess(String msg) {
        ArrayList<ProcessRequest> requestArray = new ArrayList<>();
        String[] part = msg.split(",");
        if(IDProc.contains(part[0])) //se este processador já fez o setup
        {
            ProcessRequest preq1 = new ProcessRequest(part[2], part[3]);
            if (ProcQueue.containsKey(part[0])) //se este processador já tiver a fila registada
            {
                System.out.println("Processador com fila");
                if (part[1].equals("+")) //'+' -> vamos adicionar à fila
                {
                    ProcQueue.get(part[0]).add(preq1);
                    System.out.println("Pedido com id: " + part[3] + "adicionado à fila do processador: " + part[0]);
                }
                else if (part[1].equals("-")) //'-' -> vamos remover da fila
                {
                    boolean delete = false;
                    for(int i = 0; i < ProcQueue.get(part[0]).size(); i++)
                    {
                        if(ProcQueue.get(part[0]).get(i).getpId().equals(part[3])) //se existir o pedido a eliminar
                        {
                            ProcQueue.get(part[0]).remove(i);
                            delete = true;
                            System.out.println("Pedido eliminado da fila do processador");
                        }
                    }
                    if(!delete) { System.out.println("Pedido a eliminar não consta na fila do processador"); }
                }
                else { System.out.println("Flag inválida. Sem alterações na fila"); }
            }
            else //se este processador não tiver fila registada
            {
                System.out.println("Processador sem fila registada...");
                if (part[1].equals("+")) //'+' -> vamos adicionar à fila
                {
                    requestArray.add(preq1);
                    ProcQueue.put(part[0], requestArray);
                    System.out.println("Pedido com id: " + part [3] + " adicionado à fila do processador: " + part[0]);
                }
                else if (part[1].equals("-")) { //'-' -> vamos remover da fila
                    System.out.println("Processador sem fila registada. Nada a eliminar");
                }
                else { System.out.println("Flag inválida. Sem alterações na fila"); }
            }
        }
        else //se o processador não fez setup
        {
            System.out.println("Processador não fez setup"); //ignora fila do processador
        }
    }


    //----------------------------------------captura filas de processadores----------------------------------------//

    private void QueueCatch(int port) throws IOException {
        MulticastSocket socket1 = null;
        byte[] buffer = new byte[256];

        socket1 = new MulticastSocket(port);
        InetAddress group = InetAddress.getByName("230.0.0.0");
        socket1.joinGroup(group);

        while (true) {
            DatagramPacket packet1 = new DatagramPacket(buffer, buffer.length);
            socket1.receive(packet1);
            String msg = new String(packet1.getData(), 0, packet1.getLength());
            System.out.println("Alteração na fila");
            System.out.println(msg);
            QueueProcess(msg);
        }
    }
}
