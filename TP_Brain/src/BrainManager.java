import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class BrainManager extends UnicastRemoteObject implements BrainManagerInterface {
    ArrayList<BrainModel> ModelList = new ArrayList<>();

    protected BrainManager() throws RemoteException {}

    public void saveModel(BrainModel model) throws RemoteException {
        ModelList.add(model);
    }

    public BrainModel getModel(String id) throws RemoteException {
        for (BrainModel model1 : ModelList) {
            if (model1.getbId().equals(id)) {
                return model1;
            }
        }
        return null;
    }
}