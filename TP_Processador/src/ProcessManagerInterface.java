import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface ProcessManagerInterface extends Remote {

    String ProcRequest(ProcessRequest pRequest) throws Exception;
    ArrayList<ProcessRequest> allRequests() throws RemoteException;
    ArrayList<ProcessRequest> waitList() throws RemoteException;
    void ResumeProc(String deadProcID) throws MalformedURLException, NotBoundException, RemoteException;
}