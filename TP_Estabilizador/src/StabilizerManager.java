import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
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

    MulticastSocket socket;
    InetAddress group;
    ConcurrentHashMap<String, triple<Integer,String, LocalDateTime>> map = new ConcurrentHashMap<>();
    byte[] buffer = new byte[3000];

    public StabilizerManager(boolean notWorkProc) throws IOException {
        this.socket = new MulticastSocket(4446);
        this.group = InetAddress.getByName("230.0.0.0");
        this.socket.joinGroup(this.group);
        if (notWorkProc) {
            SaveDataProc(); //guarda dados do processador, no caso de falha do estabilizador

        }
        new Thread(() ->{
            try{
                CheckProcessor();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        },"CheckProc").start();
    }



    //-----------------------------------escolha de processador-----------------------------------//

    public String[] GetProcessor() throws RemoteException {
        int i = 10000;
        String[] id = null;
        for (Map.Entry<String, triple<Integer, String, LocalDateTime>> entry : map.entrySet()) {
            if (entry.getValue().first < i) {
                assert id != null;
                id[0] = entry.getKey().split(":")[0];
                id[1] = entry.getKey().split(":")[1];
            }
        }
        return null;
    }


    //------------------------------envia lista de espera ao processador------------------------------//

    public void SendRequest(Map.Entry<String, triple<Integer, String, LocalDateTime>> deadProc) throws RemoteException, MalformedURLException, NotBoundException {
        int max = -99;
        String[] proc = null;

        for(Map.Entry<String, triple<Integer, String, LocalDateTime>> entry : map.entrySet())
        {
            if(entry.getValue().first > max) {
                max = entry.getValue().first;
                assert false;
                proc[0] = entry.getKey().split(":")[0];
                proc[1] = entry.getKey().split(":")[1];
            }
        }

        if(proc != null)
        {
            String port = proc[1];
            ProcessManagerInterface pmi = (ProcessManagerInterface) Naming.lookup("rmi://localhost:" + port + "/pRequestManager");
            pmi.ResumeProc(deadProc.getValue().second);
        }
        //envia a waitlist ao processador
    }


    //------------------------------verificar se processador está ativo------------------------------//

    public void CheckProcessor() throws InterruptedException {
        while (true) {
            for (Map.Entry<String, triple<Integer, String, LocalDateTime>> entry : map.entrySet()) {
                if((Duration.between(entry.getValue().third,LocalDateTime.now())).getSeconds() > 10){   //sleeptime heartbeat
                    try {
                        if(!entry.getValue().second.equals("null")) {
                            SendRequest(entry);
                            System.out.println("Removendo o: " + entry.getKey());
                            map.remove(entry.getKey(), entry.getValue());
                        }
                    }catch (RemoteException | MalformedURLException | NotBoundException ignored){
                    }
                }
            }
            Thread.sleep(1000);
        }
    }


    //--------------------------------guardar dados do processador--------------------------------//

    void SaveDataProc() throws IOException {
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        String received = new String(packet.getData(),0, packet.getLength());

        if(received.equals("restore;"))
        {
            socket.receive(packet);
        }
        received = new String(packet.getData(),0, packet.getLength());
        String[] procs = received.split("#");

        for (String p : procs) {
            String[] info = p.split(";");
            map.put(info[0],
                    new triple<>(
                            Integer.parseInt(info[1]),
                            info[2].equals("null") ? null : info[2],
                            LocalDateTime.parse(info[3])
                    )
            );
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
