import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;

public class BrainManager extends UnicastRemoteObject implements BrainManagerInterface {
    ArrayList<BrainModel> ModelList = new ArrayList<>(); //array que guarda modelos
    public HashMap<String, Integer> ProcIDArray; //hashmap com id do processo

    public BrainManager() throws RemoteException {
        ModelList = new ArrayList<>();
        ProcIDArray = new HashMap<>();
    }

    public void NewModel(String model1, String procID, String pID) throws RemoteException
    {
        System.out.println("-----------Novo modelo-----------");
        System.out.println("modelo gerado: " + model1);
        System.out.println("ID processador: " + procID);
        System.out.println("ID processo: " + pID);
        BrainModel model = new BrainModel(model1, procID, pID);
        ProcIDArray.put(pID, ModelList.size());
        //armazena o index do id do processo para ir buscar o modelo ao ModelList
        ModelList.add(model);
    }

    public String ModelRequest(String pID) throws RemoteException
    {
        System.out.println("Modelo gerado do pedido com ID: " + pID);
        int index = ProcIDArray.get(pID);
        BrainModel model = ModelList.get(index);
        System.out.println("Modelo: " + model.getModel());
        return model.getModel();
    }
}