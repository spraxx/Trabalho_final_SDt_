import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.lang.*;

public class StabilizerManager extends UnicastRemoteObject implements StabilizerManagerInterface {

    MulticastSocket socket;
    InetAddress group;
    HashMap<String, Integer> ProcCPU = new HashMap<>();
    HashMap<String, triple<Integer, UUID, LocalDateTime>> map = new HashMap<>();
    byte[] buffer = new byte[3000];

    public StabilizerManager(boolean notWorkProc) throws IOException {
        this.socket = new MulticastSocket(4446);
        this.group = InetAddress.getByName("230.0.0.0");
        this.socket.joinGroup(this.group);
        if (notWorkProc) {
            dataRequest();
            saveDataProc();
        }
        new Thread(this::checkProcessor, "verificarProcs").start();
    }



    //-----------------------------------escolha de processador-----------------------------------//

    public String[] getProcessador() throws RemoteException {
        int i = 10000;
        String[] id = null;
        for (Map.Entry<String, Integer> entry : ProcCPU.entrySet()) {
            if (entry.getValue() < i) {
                assert id != null;
                id[0] = entry.getKey().split(":")[0];
                id[1] = entry.getKey().split(":")[1];
            }
        }
        return null;
    }



    //------------------------------verificar se processador está ativo------------------------------//

    public void checkProcessor() {
        while (true) {
            for (Map.Entry<String, triple<Integer, UUID, LocalDateTime>> entry : map.entrySet())
            {
                if((Duration.between(entry.getValue().third, LocalDateTime.now())).getSeconds() > 10) //10 -> valor experimental
                {
                    System.out.println("Remover: " + entry.getKey());
                    map.remove(entry.getKey(), entry.getValue());
                }

            }
        }
    }



    //-----------------------------------requisição de dados-----------------------------------//

    void dataRequest() throws IOException {
        DatagramPacket packet = new DatagramPacket("restore;".getBytes(), "restore;".getBytes().length, group, 4446);
        socket.send(packet);
    }



    //--------------------------------guardar dados do processador--------------------------------//

    void saveDataProc() throws IOException {
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
                            info[2].equals("null") ? null : UUID.fromString(info[2]),
                            LocalDateTime.parse(info[3])
                    )
            );
        }
    }



    //------------------------CLASS TRIPLE------------------------//

    class triple<A,B,C>{
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
