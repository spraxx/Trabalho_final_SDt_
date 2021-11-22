import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Scanner;
import java.util.UUID;

public class thread1 implements Runnable{
    static Node head = null;
    public void run(){
        int i = 0;
        try {
            while (i<10) {
                head = push(head, infoCPU());
                Thread.sleep(500);
                i++;
                if (i == 9){
                    i = 0;
                    head = null;
                }
            }

        } catch (RemoteException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    static class Node {
        int data;
        Node link;
    }

    public int infoCPU() throws RemoteException {
        Process p1 = null;

        try{
            p1 = Runtime.getRuntime().exec("wmic cpu get loadpercentage");
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert p1 != null;
        InputStream inpStream = p1.getInputStream();
        Scanner s1 = new Scanner(inpStream).useDelimiter("\\A");
        String value = "";
        if(s1.hasNext()) {
            value = s1.next();
        }
        else {
            value = "";
        }

        String[] st1 = value.split("[\r\n]+");
        String[] st2 = st1[1].split(" ");

        return Integer.parseInt(st2[0]);
    }

    static Node push(Node head_ref, int data){
        Node ptr1 = new Node();
        Node temp = head_ref;
        ptr1.data = data;
        ptr1.link = head_ref;

        // If linked list is not null then
        // set the next of last node
        if (head_ref != null)
        {
            while (temp.link != head_ref)
                temp = temp.link;
                temp.link = ptr1;
        }
        else
            ptr1.link = ptr1; // For the first node

        head_ref = ptr1;

        return head_ref;
    }

    public int sumOfList() {
        Node temp = head;
        int sum = 0;
        int i = 0;
        if (head != null)
        {
            do
            {
                temp = temp.link;
                sum += temp.data;
                i++;
            } while (temp != head);
        }
        System.out.println("//-----------------------------//");
        System.out.println("Total da soma: ["+sum+"]");
        System.out.println("Numero de leituras: ["+i+"]");
        System.out.println("Media: ["+sum/i+"]");
        return sum / i;
    }
}
