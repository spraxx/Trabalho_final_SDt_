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

    public static void main(String[] args) throws IOException, NotBoundException {
        int port = 2025;
        Registry r = LocateRegistry.createRegistry(port);
        StabilizerManager stabManager = new StabilizerManager(false);
        r.rebind("stabManager", stabManager);
        System.out.println("Estabilizador a correr!");

        stabManager.SendRequest(
                new Map.Entry<String, StabilizerManager.triple<Integer, String, LocalDateTime>>() {

                    public String getKey() {
                        return "2021:1";
                    }

                    public StabilizerManager.triple<Integer, String, LocalDateTime> getValue() {
                        return new StabilizerManager.triple<>(
                                21,
                                " ## "+ UUID.nameUUIDFromBytes(
                                        ("ola"+ "." + LocalDateTime.now()).getBytes(StandardCharsets.UTF_8))
                                        + " - " + "teste" + " - " + "testfile.txt", LocalDateTime.now()
                        );
                    }

                    public StabilizerManager.triple<Integer, String, LocalDateTime> setValue(
                            StabilizerManager.triple<Integer, String, LocalDateTime> value) {
                                return null;
                            }
                }
        );
    }
}
