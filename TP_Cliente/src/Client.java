import java.rmi.Naming;
import java.rmi.RemoteException;

public class Client {

    public static void showPid(ProcessRequest pRequest) {
        System.out.println("Script: " + pRequest.getpScript() + "\nID: " + pRequest.getpId());
    }


    public static void main(String[] args) {
        try{
            BrainManagerInterface brainManInt = (BrainManagerInterface) Naming.lookup("rmi://localhost:2074/brain");
            StabilizerManagerInterface stabManInt = (StabilizerManagerInterface) Naming.lookup("rmi://localhost:2075/stabManager");

            String id = stabManInt.ChosenOne();
            System.out.println("ID do processador escolhido: " + id);
            Thread.sleep(1000);

            ProcessManagerInterface procManInt = (ProcessManagerInterface) Naming.lookup("rmi://localhost:2070/pRequestManager");
            System.out.println("Cliente a correr!");

            ProcessRequest request1 = new ProcessRequest("nslookup sapo.pt", "testfile.txt");
            request1.setpId(procManInt.ProcRequest(request1));
            showPid(request1);
            Thread.sleep(2000);

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
