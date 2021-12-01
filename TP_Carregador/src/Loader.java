import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Loader {

    public static void main(String[] args) throws IOException {

        DataManager dtMan = new DataManager();
        Registry r = null;

        for(int i = 0; i < dtMan.getData().size(); i++)
        {
            System.out.println("dado carregado: " + dtMan.getData().get(i));
            //carregamento de dados
        }

        try {
            r = LocateRegistry.createRegistry(2021);
        }
        catch (RemoteException e) { e.printStackTrace(); }

        try {
            assert r != null;
            r.rebind("dtMan", (Remote) dtMan);
            System.out.println("Carregador a correr!");
        }
        catch (Exception e2) { System.out.println("Carregador principal " + e2.getMessage()); }
    }
}