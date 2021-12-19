import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BrainManagerInterface extends Remote {
    void NewModel(String model1, String procID, String pID) throws RemoteException;
    public String ModelRequest(String pID) throws RemoteException;
}
