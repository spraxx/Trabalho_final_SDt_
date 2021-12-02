import java.io.IOException;
import java.rmi.Remote;

public interface DataManagerInterface extends Remote {

    public String DataRequest(String req) throws Exception;
}