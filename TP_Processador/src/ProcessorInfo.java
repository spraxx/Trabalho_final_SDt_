public class ProcessorInfo {

    private String id;
    private float cpu;
    private float memory;
    private int state;
    private int queue;
    private String address;

    public void setId(String id) { this.id = id; }

    public void setCpu(float cpu) { this.cpu = cpu; }

    public void setMemory(float mem) { this.memory = mem; }

    public void setState(int state) { this.state = state; }

    public void setQueue(int queue) { this.queue = queue; }

    public void setAddress(String address) { this.address = address; }

    public String getId() { return id; }

    public float getCpu() { return cpu; }

    public float getMemory() { return memory; }

    public String getAddress() { return address; }

    public int getQueue() { return queue; }

    public int getState() { return state; }
}