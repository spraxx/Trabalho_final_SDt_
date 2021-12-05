import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Brain {
    public static void main(String[] args) {
        Registry r = null;
        BrainManager bManager = null;

        try {
            r = LocateRegistry.createRegistry(2024);
        }
        catch(RemoteException a){
            a.printStackTrace();
        }

        try {
            bManager = new BrainManager();
            r.rebind("brain", bManager);

            System.out.println("Brain server ready");
        }
        catch(Exception e) {
            System.out.println("Brain server error: " + e.getMessage());
        }
    }
}