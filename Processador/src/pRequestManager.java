import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.UUID;

public class pRequestManager extends UnicastRemoteObject implements pRequestManagerInterface{

    private ArrayList<ProcessRequest> allReq;
    private ArrayList<ProcessRequest> waitL;


    public pRequestManager() throws RemoteException {
        this.allReq = new ArrayList<>();
        this.waitL = new ArrayList<>();
    }

    public int infoCPU() throws RemoteException {
        Process p1 = null;

        try{
            p1 = Runtime.getRuntime().exec("wmic cpu get loadpercentage");
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
        System.out.println("["+value+"]");
        String[] st1 = value.split("[\\r\\n]+");
        String[] st2 = st1[1].split(" ");
        System.out.println(Integer.parseInt(st2[0]));



        return Integer.parseInt(st2[0]);

    }

    public String ProcRequest(ProcessRequest pRequest) throws RemoteException{

        int usageCPU = infoCPU();
        if(usageCPU < 30) {
            System.out.println("Pedido: " + pRequest.getpScript() + " foi processado com sucesso!");
        }
        else {
            System.out.println("O pedido: " + pRequest.getpScript() + " ficará em lista de espera");
            waitL.add(pRequest);
            // devido à falta de recursos, o pedido fica em lista de espera
        }

        String pid = UUID.randomUUID().toString();
        pRequest.setpId(pid);
        System.out.println("PID: " + pid);
        return pid;
        // geração de id e respetiva devolução
    }

    public ArrayList<ProcessRequest> allRequests() throws RemoteException {
        return allReq;
    }

    public ArrayList<ProcessRequest> waitList() throws RemoteException {
        return waitL;
    }
}
