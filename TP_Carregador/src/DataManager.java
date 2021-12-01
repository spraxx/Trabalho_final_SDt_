import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class DataManager extends UnicastRemoteObject implements DataManagerInterface {

    private ArrayList<String> data;

    public DataManager() throws RemoteException {
        this.data = new ArrayList<>();
        this.data.add("cmd.exe /c start C:\\_script1.bat");
        this.data.add("cmd.exe /c start C:\\_script2.bat");
        this.data.add("cmd.exe /c start C:\\_script3.bat");
    }

    public ArrayList<String> getData() { return data; }

    public String DataRequest(String req) {
        int ns;
        ns = Integer.parseInt(String.valueOf(req.charAt(req.length()-1)));
        return this.data.get(ns-1);

        //devolve o ultimo char do request
    }

}















