import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface ProcessManagerInterface extends Remote {

    String ProcRequest(ProcessRequest pRequest) throws Exception;
    ArrayList<ProcessRequest> allRequests() throws RemoteException;
    void ResumeProc(String reqList) throws RemoteException;
}