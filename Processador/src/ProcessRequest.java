import java.io.Serializable;

public class ProcessRequest implements Serializable {

    private String pScript;
    private String pId;

    public ProcessRequest(String script) {
        this.pScript = script;
        this.pId = null;
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
}
