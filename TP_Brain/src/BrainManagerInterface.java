import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BrainManagerInterface extends Remote {
    void saveModel(BrainModel model) throws RemoteException;
    BrainModel getModel(String id) throws RemoteException;
}