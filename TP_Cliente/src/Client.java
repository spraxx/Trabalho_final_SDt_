import java.rmi.Naming;
import java.rmi.RemoteException;

public class Client {

    public static void showPid(ProcessRequest pRequest) {
        System.out.println("Script: " + pRequest.getpScript() + "\nID: " + pRequest.getpId());
    }


    public static void main(String[] args) {
        try{
            StabilizerManagerInterface stabManInt = (StabilizerManagerInterface) Naming.lookup("rmi://localhost:2025/stabManager");

            String idProc, port;
            String[] proc = stabManInt.GetProcessor();
            if(proc == null)
            {
                idProc = "1";
                port = "2022";
            }
            else {
                idProc = proc[0];
                port = proc[1];
            }


            ProcessManagerInterface procManInt = (ProcessManagerInterface) Naming.lookup("rmi://localhost:"+port+"/pRequestManager");
            System.out.println("Cliente a correr!");
            System.out.println("Processador escolhido ID: " + idProc);

            ProcessRequest request1 = new ProcessRequest("nslookup sapo.pt", "testfile.txt");
            request1.setpId(procManInt.ProcRequest(request1));
            showPid(request1);
            Thread.sleep(2000);

            BrainManagerInterface brainManInt = (BrainManagerInterface) Naming.lookup("rmi://localhost:2024/brain");

            System.out.println("\nScript enviado com sucesso!");
            procManInt.allRequests().forEach(processRequest -> {
                System.out.println(processRequest.getpScript());
            });
            // são mostrados todos os pedidos recebidos pelo processador -> allRequests()

            String brainmodel = brainManInt.ModelRequest(request1.getpId());
            System.out.println("Modelo gerado: " + brainmodel);

        } catch (RemoteException e) {
            System.out.println(e.getMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
