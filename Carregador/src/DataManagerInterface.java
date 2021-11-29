import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;

import java.io.IOException;
import java.rmi.Remote;
import java.util.ArrayList;

public interface DataManagerInterface extends Remote {

    LoaderAnswer execRequest(ProcessRequest pRequest) throws IOException;
}
