import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.Integer.parseInt;

public class BrainManager extends UnicastRemoteObject implements BrainManagerInterface {
    public ArrayList<BrainModel> ModelList; //array que guarda modelos
    public HashMap<String, Integer> ReqHMap; //hashmap que guarda <ID do pedido, ModelList>

    public BrainManager() throws RemoteException {
        ModelList = new ArrayList<>();
        ReqHMap = new HashMap<>();
    }


    //--------------------------------------------criação de novo modelo--------------------------------------------//

    public void NewModel(String model1, String procID, String pID) throws RemoteException
    {
        System.out.println("Novo modelo");
        System.out.println("modelo gerado: " + model1 + "ID processador: " + procID + "ID pedido: " + pID);
        BrainModel model = new BrainModel(model1, procID, pID);
        ReqHMap.put(pID, ModelList.size()); //guarda o pID para depois ir buscar o modelo gerado ao Array
        ModelList.add(model);
    }


    //---------------------------------------------devolve modelo gerado---------------------------------------------//

    public String ModelRequest(String pID) throws RemoteException, NumberFormatException
    {
        System.out.println("Modelo gerado do pedido com ID: " + pID);
        if(ReqHMap.containsKey(pID))
        {
            System.out.println("Model Request: OK!");
            int idModel = ReqHMap.get(pID);
            BrainModel model = ModelList.get(idModel);
            System.out.println("Modelo devolvido: " + model.getModel());
            return model.getModel();
        }
        else
        {
            System.out.println("Modelo não gerado!");
            return "NULL";
        }
    }


    //-------------------------------------------confirmação modelo gerado-------------------------------------------//

    public boolean ModelGenerated(String id_script) throws RemoteException //true se o modelo já foi gerado
    {
        System.out.println("O pedido com ID: " + id_script + " foi processado?");
        if(ReqHMap.containsKey(id_script))
        {
            System.out.println("SIM");
            return true;
        }
        else
        {
            System.out.println("NÃO");
            return false;
        }
    }

}