import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Processor {

    public static void main(String args[]) throws RemoteException {

        processManager pReqMan = new processManager();
        Registry r = null;

        try{
            r= LocateRegistry.createRegistry(2023);
            thread1 m1 = new thread1();
            Thread t1 = new Thread(m1); // Using the constructor Thread(Runnable r)
            t1.start();
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
