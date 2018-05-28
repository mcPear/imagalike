import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

public class Main {
    public static void main(String... args) throws FileNotFoundException {
        String dirPath = "/home/maciej/Programs/extract_features";
        String imageFileName = "image.png";
        String haraffSiftFileName = "image.png.haraff.sift";
        String haraffSiftFilePath = dirPath + "/" + haraffSiftFileName;

        createInterestPointsFile(dirPath, imageFileName);
        System.out.println(HaraffSiftResult.loadFromFile(haraffSiftFilePath));
    }

    static boolean createInterestPointsFile(String dirPath, String filename) {
        String s;
        Process p;
        int exitCode = 1;
        try {
            p = Runtime.getRuntime().exec(
                    "./extract_features.ln -haraff -i " + filename + " -sift",
                    null,
                    new File(dirPath));
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
            while ((s = br.readLine()) != null)
                System.out.println("| " + s);
            p.waitFor();
            System.out.println("exit: " + p.exitValue());
            exitCode = p.exitValue();
            p.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return exitCode == 0;
    }
}
