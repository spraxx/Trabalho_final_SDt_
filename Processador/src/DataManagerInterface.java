import java.io.IOException;
import java.rmi.Remote;

public interface DataManagerInterface extends Remote {

    LoaderAnswer execRequest(ProcessRequest pRequest) throws IOException;
}