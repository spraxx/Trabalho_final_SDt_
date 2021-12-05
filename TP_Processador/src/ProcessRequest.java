import java.io.Serializable;

public class ProcessRequest implements Serializable {

    private String pScript;
    private String pId;
    private String inFile;
    private String outFile;

    public ProcessRequest(String script, String iFile, String oFile) {
        this.pScript = script;
        this.pId = null;
        this.inFile = iFile;
        this.outFile = oFile;
    }

    public void setpScript(String script) {
        this.pScript = script;
    }

    public void setpId(String pid) {
        this.pId = pid;
    }

    public String getpScript() {
        return pScript;
    }

    public String getpId() {
        return pId;
    }

    public String getInFile() {return inFile;}

    public String getOutFile() {return outFile;}
}
