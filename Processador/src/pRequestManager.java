import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.UUID;

public class pRequestManager extends UnicastRemoteObject implements pRequestManagerInterface{

    private static final long serialVersionUID = 1L;
    private ArrayList<ProcessRequest> allReq;
    private ArrayList<ProcessRequest> waitL;


    public pRequestManager() throws RemoteException {
    }

    private float usageCPU() throws Exception {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        ObjectName Objname = ObjectName.getInstance("java.lang:type=OperatingSystem");
        AttributeList AttList = mBeanServer.getAttributes(Objname, new String[]{ "LoadProcessCPU" });
        Attribute att = (Attribute)AttList.get(0);
        Double value  = (Double)att.getValue();
        boolean incompleteRead = true;
        while(incompleteRead)
        {
            value = (Double)att.getValue();
            if( ((float)(value * 100)) > 0 ) incompleteRead = false;
        }
        System.out.println( "CPU usage: " + ((float)(value * 100)) );
        return ((float)(value * 100));
    }

    private float memoryCPU() {

        Runtime r = Runtime.getRuntime();
        r.gc();

        float usedMemory = (float)r.totalMemory()/1000000 - (float)r.freeMemory()/1000000;
        float totalMemory = (float)r.totalMemory()/1000000;
        float percMemory = ((usedMemory/totalMemory)*100);

        System.out.println("Memory used: " + percMemory + "% ");
        return percMemory;
    }

    public String ProcRequest(ProcessRequest pRequest) throws RemoteException, Exception {

        System.out.println("Process Request: " + pRequest.getpScript());
        allReq.add(pRequest);
        // adiciona este pedido ao array de todos os pedidos -> allRequests()

        if(usageCPU() < 25 && memoryCPU() < 1) {
            System.out.println("Pedido: " + pRequest.getpScript() + " foi processado com sucesso!");
        }
        else {
            System.out.println("O pedido ficará em lista de espera");
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
