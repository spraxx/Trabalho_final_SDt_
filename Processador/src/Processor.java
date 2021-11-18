import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Processor {

    public static void main(String args[]) throws RemoteException {

        pRequestManager pReqMan = new pRequestManager();
        Registry r = null;

        try{
            r= LocateRegistry.createRegistry(2020);
        }
        catch (RemoteException e) {
            e.printStackTrace();
        }

        try{
            r.rebind("pRequestManager", pReqMan);
            System.out.println("Processador a correr!");
        }
        catch (Exception e) {
            System.out.println("Processador principal " + e.getMessage());
        }
    }
}
