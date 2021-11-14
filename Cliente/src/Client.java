import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Client {

    public static void main(String args[]) {
        try{
            ProcessManagerInterface procManInt = null;
            procManInt = (ProcessManagerInterface) Naming.lookup("rmi://localhost:2022/pRequestManager");

                ProcessRequest request1 = new ProcessRequest("script 1");
                request1.setpId(procManInt.ProcRequest(request1));
                showPid(request1);

                ProcessRequest request2 = new ProcessRequest("script 2");
                request2.setpId(procManInt.ProcRequest(request2));
                showPid(request2);

                ProcessRequest request3 = new ProcessRequest("script 3");
                request3.setpId(procManInt.ProcRequest(request3));
                showPid(request3);
                // foram enviados vários pedidos (3) ao processador

                System.out.println("Todos os pedidos foram enviados com sucesso!");
                procManInt.allRequests().forEach(processRequest -> {
                    System.out.println(processRequest.getpScript());
                });
                // são mostrados todos os pedidos recebidos pelo processador -> allRequests()

        } catch (RemoteException e) {
            System.out.println(e.getMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void showPid(ProcessRequest pRequest) {
        System.out.println("script: " + pRequest.getpScript() + "// id: " + pRequest.getpId());
    }

}
