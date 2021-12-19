import java.io.*;

public class BrainModel implements Serializable {
    private String model;
    private String processorID;
    private String processID;

    public BrainModel(String model, String procID, String pID) {
        this.model = model;
        this.processorID = procID;
        this.processID = pID;
    }

    public String getModel() { return model; }

}