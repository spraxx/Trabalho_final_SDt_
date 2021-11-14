import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Processor {

    public static void main(String args[]) {

        Registry r = null;
        pRequestManagerInterface preq;

        try{
            r= LocateRegistry.createRegistry(2022);
        }
        catch (RemoteException e) {
            e.printStackTrace();
        }

        try{
            preq= new pRequestManager();
            r.rebind("pRequestManager", preq);
            System.out.println("Processador a correr!");
        }
        catch (Exception e) {
            System.out.println("Processador principal " + e.getMessage());
        }
    }
}
