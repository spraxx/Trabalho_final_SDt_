import java.rmi.Naming;
import java.rmi.RemoteException;

public class Client {

    public static void showPid(ProcessRequest pRequest) {
        System.out.println("Script: " + pRequest.getpScript() + "\nID: " + pRequest.getpId());
    }

    public static void main(String[] args) {
        try{
            ProcessManagerInterface procManInt = (ProcessManagerInterface) Naming.lookup("rmi://localhost:2023/pRequestManager");

            //ProcessRequest request1 = new ProcessRequest("script1");
            ProcessRequest request1 = new ProcessRequest("nslookup sapo.pt");
            request1.setpId(procManInt.ProcRequest(request1));
            showPid(request1);

            System.out.println("Script enviado com sucesso!");
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

}
