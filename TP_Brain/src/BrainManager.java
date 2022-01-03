import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.Integer.parseInt;

public class BrainManager extends UnicastRemoteObject implements BrainManagerInterface {
    ArrayList<BrainModel> ModelList; //array que guarda modelos
    ArrayList<String> ProcIDArray; //array com id do processo

    public BrainManager() throws RemoteException {
        ModelList = new ArrayList<>();
        ProcIDArray = new ArrayList<>();
    }

    public void NewModel(String model1, String procID, String pID) throws RemoteException
    {
        System.out.println("-----------Novo modelo-----------");
        System.out.println("modelo gerado: " + model1);
        System.out.println("ID processador: " + procID);
        System.out.println("ID processo: " + pID);
        BrainModel model = new BrainModel(model1, procID, pID);
        ProcIDArray.add(pID);
        //armazena o index do id do processo para ir buscar o modelo ao ModelList
        ModelList.add(model);
    }

    public String ModelRequest(String pID) throws RemoteException
    {
        while (true) {
            System.out.println("Modelo gerado do pedido com ID: " + pID);
            String index = ProcIDArray.get(Integer.parseInt(pID));
            BrainModel model;
            model = ModelList.get(Integer.parseInt(index));
            System.out.println("Modelo: " + model.getModel());
            return model.getModel();
        }
    }
}