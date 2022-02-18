import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class Stabilizer {

    public static void main(String[] args) throws IOException {
        int port = 2075;
        Registry r = LocateRegistry.createRegistry(port);
        StabilizerManager stabManager = new StabilizerManager();
        r.rebind("stabManager", stabManager);
        System.out.println("Estabilizador a correr!");
    }
}
