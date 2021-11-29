import java.io.File;
import java.io.Serializable;

public class LoaderAnswer implements Serializable {

    File answer;
    LoaderAnswer(File answer)
    {
        this.answer = answer;
    }
}
