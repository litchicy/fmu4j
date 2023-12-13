import java.io.File;
import java.nio.file.Paths;

public class TestFMUs {

    public static File get(String path) {
        File projectFolder = new File(".").getAbsoluteFile();
        while (!projectFolder.getName().toLowerCase().equals("fmu")) {
            projectFolder = projectFolder.getParentFile();
        }
        String child = "test-fmus" + File.separator + "fmus" + File.separator + path;
        return new File(projectFolder, child).getAbsoluteFile();
    }

}