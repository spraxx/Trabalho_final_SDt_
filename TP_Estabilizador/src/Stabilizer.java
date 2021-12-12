import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Stabilizer {

    public static void main(String[] args) throws RemoteException {

        StabilizerManager stabManager = new StabilizerManager();
        Registry r = null;

        try{
            r = LocateRegistry.createRegistry(2025);
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
        }

        try{
            r.rebind("stabManager", stabManager);
            System.out.println("Estabilizador a correr!");
        }
        catch(Exception e)
        {
            System.out.println("Estabilizador principal: " + e.getMessage());
        }

    }
}
