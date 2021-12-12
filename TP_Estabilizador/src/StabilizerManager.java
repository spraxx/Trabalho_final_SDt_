import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.Float.parseFloat;

public class StabilizerManager extends UnicastRemoteObject implements StabilizerManagerInterface {

    private HashMap<String, ProcessorInfo> ProcInfo = new HashMap<>();
    private ArrayList<String> IdArray = new ArrayList<>(); //array de Id's de processadores

    public StabilizerManager() throws RemoteException {

        Thread t1 = new Thread(new Runnable()
        {
            public void run()
            {
                try { CaptureMsg(4446); }
                catch (IOException ignored) { }
            }
        });
        t1.start();
    }

    public void CaptureMsg(int port) throws IOException {
        MulticastSocket socket1 = null;
        byte[] buffer = new byte[256];

        socket1 = new MulticastSocket(port);
        InetAddress group = InetAddress.getByName("230.0.0.0"); //captura msg no IP 230.0.0.0 : port
        socket1.joinGroup(group);

        while (true) {
            DatagramPacket p1 = new DatagramPacket(buffer, buffer.length);
            socket1.receive(p1);
            String msg1 = new String(p1.getData(), 0, p1.getLength());
            System.out.println(msg1);
            FormatMsg(msg1);
        }
    }

    public void FormatMsg(String msg){

        String[] part = msg.split(","); //separa msg por virgulas)
        //formato msg => (setup/hb, id, cpu, mem, estado, fila, endereço)

        ProcessorInfo pInfo = new ProcessorInfo();
        pInfo.setId(part[1]);
        pInfo.setCpu(Float.parseFloat(part[2]));
        pInfo.setMemory(Float.parseFloat(part[3]));
        pInfo.setState(Integer.parseInt(part[4]));
        pInfo.setQueue(Integer.parseInt(part[5]));
        pInfo.setAddress(part[6]);

        ProcInfo.put(part[1], pInfo); //HasMap na forma <ID processador, Info>
        IdArray.add(part[1]);
    }

    public String ProcMoreResources() throws RemoteException{

        ProcessorInfo pInfo = new ProcessorInfo();
        float cpu = 100;
        float memory = 100;

        String pmoreResources = ProcInfo.get(IdArray.get(0)).getAddress();
        //no caso de nenhum processador se encontrar disponível, o primeiro da fila é o com mais recursos

        for(int i = 0; i < ProcInfo.size() ; i++)
        {
            pInfo = ProcInfo.get(IdArray.get(i));
            if(pInfo.getCpu() < cpu && pInfo.getMemory() < memory)
            {
                cpu = pInfo.getCpu();
                memory = pInfo.getMemory();
                pmoreResources = pInfo.getAddress();
            }
        }
        return pmoreResources;
    }
}
