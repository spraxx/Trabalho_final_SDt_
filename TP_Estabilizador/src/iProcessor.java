public class iProcessor {

    private String pid;
    private float cpu;
    private int state;
    private int queue;
    private String address;
    private boolean active;

    public void setPid(String id) { this.pid = id; }

    public void setCpu(float cpu) { this.cpu = cpu; }

    public void setState(int estado) { this.state = estado; }

    public void setQueue(int fila) { this.queue = fila; }

    public void setAddress(String address) { this.address = address; }

    public boolean getActive() { return active; }

    public String getPid() { return pid; }

    public float getCpu() { return cpu; }

    public int getState() { return state; }

    public int getQueue() { return queue; }

    public String getAddress() { return address; }

    public void setActive(boolean active) { this.active = active; }
}