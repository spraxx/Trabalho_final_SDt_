import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Processor {

    public static void startProcessor(int port, int idProcessor) throws IOException {
        Registry r = LocateRegistry.createRegistry(port);
        processManager pReqMan = new processManager(String.valueOf(idProcessor), 4446);
        r.rebind("pRequestManager", pReqMan);
        System.out.println("Processador a correr!");
        thread1 m1 = new thread1();
        Thread t1 = new Thread(m1);
        t1.start();
    }

    public static void main(String[] args) throws IOException {

        startProcessor(2022, 1);
        startProcessor(2023, 2);
    }
}
