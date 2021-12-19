import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.lang.*;

public class StabilizerManager extends UnicastRemoteObject implements StabilizerManagerInterface {

    MulticastSocket socket;
    InetAddress group;
    HashMap<String, Integer> ProcCPU = new HashMap<>();

    public StabilizerManager() throws IOException {
        this.socket = new MulticastSocket(4446);
        this.group = InetAddress.getByName("230.0.0.0");
        this.socket.joinGroup(this.group);
    }

    public String[] getProcessador() throws RemoteException {
        int i = 10000;
        String[] id = null;
        for(Map.Entry<String,Integer> entry : ProcCPU.entrySet()){
            if(entry.getValue() < i){
                assert id != null;
                id[0] = entry.getKey().split(":")[0];
                id[1] = entry.getKey().split(":")[1];
            }
        }
        return null;
    }
}
