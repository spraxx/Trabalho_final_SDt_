import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface ProcessManagerInterface extends Remote {

    String ProcRequest(ProcessRequest pRequest) throws RemoteException;
    ArrayList<ProcessRequest> allRequests() throws RemoteException;
    ArrayList<ProcessRequest> waitList() throws RemoteException;
}