import java.rmi.Remote;
import java.rmi.RemoteException;

public interface StabilizerManagerInterface extends Remote {
    String ChosenOne() throws RemoteException;
}

