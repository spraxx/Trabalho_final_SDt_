import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BrainManagerInterface extends Remote {
    void NewModel(String model1, String procID, String pID) throws RemoteException;
    String ModelRequest(String pID) throws RemoteException;
    boolean ModelGenerated(String id_script) throws RemoteException;
}