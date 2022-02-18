import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Processor {

    public static void startProcessor(int idProcessor, int port, int queue_port, int dead_port, int stab_port) throws IOException {
        Registry r = LocateRegistry.createRegistry(port);
        processManager pReqMan = new processManager(idProcessor, port, queue_port, dead_port, stab_port);
        r.rebind("pRequestManager", pReqMan);
        System.out.println("Processador " + idProcessor + " a correr!");
        thread1 m1 = new thread1();
        Thread t1 = new Thread(m1);
        t1.start();
    }

    public static void main(String[] args) throws IOException {

        startProcessor(1, 2070, 4448, 4451, 4461);
        startProcessor(2, 2071, 4447, 4450, 4460);
    }
}
