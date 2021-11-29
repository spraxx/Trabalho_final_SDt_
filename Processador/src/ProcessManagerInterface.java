import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface ProcessManagerInterface extends Remote {

    String ProcRequest(ProcessRequest pRequest) throws RemoteException, Exception;
    ArrayList<ProcessRequest> allRequests() throws RemoteException;
    ArrayList<ProcessRequest> waitList() throws RemoteException;

}