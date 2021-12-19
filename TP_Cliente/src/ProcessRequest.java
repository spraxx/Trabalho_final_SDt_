import java.io.Serializable;

public class ProcessRequest implements Serializable {

    private String pScript;
    private String pId;
    private String file;


    public ProcessRequest(String script, String file) {
        this.pScript = script;
        this.pId = null;
        this.file = file;
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

    public String getFile() {return file;}
}
