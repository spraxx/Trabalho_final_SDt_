import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Brain {
    public static void main(String[] args) throws RemoteException {
        int port = 2074;
        Registry r = LocateRegistry.createRegistry(port);
        BrainManager bManager = new BrainManager();
        r.rebind("brain", bManager);
        System.out.println("Cérebro a correr!");
    }
}
