import java.rmi.Naming;
import java.rmi.RemoteException;

public class Client {

    public static void showPid(ProcessRequest pRequest) {
        System.out.println("Script: " + pRequest.getpScript() + "\nID: " + pRequest.getpId());
    }

    public static void main(String[] args) {
        try{

            BrainManagerInterface brainManInt = (BrainManagerInterface) Naming.lookup("rmi://localhost:2024/brain");

            StabilizerManagerInterface stabManInt = (StabilizerManagerInterface) Naming.lookup("rmi://localhost:2025/stabManager");

            /*String addressCPU = stabManInt.ProcMoreResources();
            System.out.println("endereço do processador: " + addressCPU);
            Thread.sleep(2000);*/

            //ProcessManagerInterface procManInt = (ProcessManagerInterface) Naming.lookup(addressCPU);
            //System.out.println("Cliente a correr!");

            ProcessManagerInterface procManInt = (ProcessManagerInterface) Naming.lookup("rmi://localhost:2022/pRequestManager");
            System.out.println("Cliente a correr!");


            ProcessRequest request1 = new ProcessRequest("nslookup sapo.pt", "testfile.txt");
            request1.setpId(procManInt.ProcRequest(request1));
            showPid(request1);
            Thread.sleep(10000);

            System.out.println("Modelo gerado: " + brainManInt.ModelRequest(request1.getpId()));
            processManager.deleteScriptRebex(request1.getpScript());
            //após mostrar o modelo o script é apagado

            System.out.println("\nScript enviado com sucesso!");
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
