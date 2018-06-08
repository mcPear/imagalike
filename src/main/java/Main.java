
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.List;

public class Main {

    public static void main(String... args) throws FileNotFoundException {
        String dirPath = "/home/maciej/Programs/extract_features";
        String image1FileName = "a1.png";
        String image2FileName = "a1small.png";
        String image1Path = dirPath + "/" + image1FileName;
        String image2Path = dirPath + "/" + image2FileName;
        String haraffSiftSuffix = ".haraff.sift";
        String haraffSiftFile1Name = image1FileName + haraffSiftSuffix;
        String haraffSiftFile2Name = image2FileName + haraffSiftSuffix;
        String haraffSiftFile1Path = dirPath + "/" + haraffSiftFile1Name;
        String haraffSiftFile2Path = dirPath + "/" + haraffSiftFile2Name;

        Window window = new Window(image1Path, image2Path);
        window.setVisible(true);

        createInterestPointsFile(dirPath, image1FileName);
        createInterestPointsFile(dirPath, image2FileName);
        System.out.println("Log: Files created");
        HaraffSiftResult haraffSiftResult1 = HaraffSiftResult.loadFromFile(haraffSiftFile1Path);
        HaraffSiftResult haraffSiftResult2 = HaraffSiftResult.loadFromFile(haraffSiftFile2Path);
        Store.haraffSiftResult1 = haraffSiftResult1;
        Store.haraffSiftResult2 = haraffSiftResult2;
        window.paintImmediately();
        System.out.println("Log: Files loaded to memory");
        System.out.println("Log: image2 interestsPointsCount: " + haraffSiftResult2.interestPointsCount + " " + haraffSiftResult2.interestPoints.size());

        List<InterestPointsPair> interestPointsPairs = InterestPointPairsFinder.getInterestPointsPairs(haraffSiftResult1, haraffSiftResult2);
        Store.interestPointsPairs = interestPointsPairs;
        window.paintImmediately();
        System.out.println("Log: Interest point pairs found");
        List<InterestPointsPair> cohesiveInterestPointsPairs = InterestPointPairsFinder.getCohesiveInterestPointsPairs(interestPointsPairs, 20, 0.5);
        Store.cohesiveInterestPointsPairs = cohesiveInterestPointsPairs;
        System.out.println("Log: Cohesive pairs found");
        window.paintImmediately();
        List<InterestPointsPair> bestModelFittedPairs = Ransac.getBestModelFittedPairs(interestPointsPairs, 10000, 10);
        Store.bestModelFittedInterestPointsPairs = bestModelFittedPairs;
        System.out.println("Log: The best model found");
        System.out.println("Log: Count of fitted pairs: " + bestModelFittedPairs.size());
        window.paintImmediately();
        //TODO second transform, heuristic and better images
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
