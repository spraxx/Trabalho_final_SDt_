import java.io.*;

public class BrainModel implements Serializable {
    private String bId;
    private String bFileName;
    private byte[] file;

    public BrainModel(String id, String fileName, byte[] file) {
        this.bId = id;
        this.bFileName = fileName;
        this.file = file;
    }

    public String getbId() { return bId; }

    public String getbFileName() { return bFileName; }

    public byte[] getFile() { return file; }


    public boolean ModelToFile(String path) {
        File f2 = new File(path + "/" + this.bFileName);

        try {
            f2.createNewFile();
        }
        catch (IOException e) {
            System.out.println("ModelToFile() 1: " + e.getMessage());
            return false;
        }

        FileOutputStream outputFile = null;

        try {
            outputFile = new FileOutputStream(f2);
        }
        catch (FileNotFoundException e2) {
            System.out.println("ModelToFile() 2: " + e2.getMessage());
            return false;
        }

        try {
            outputFile.write(this.file);
        }
        catch (IOException e3) {
            System.out.println("ModelToFile() 3: " + e3.getMessage());
            return false;
        }

        return true;
    }
}