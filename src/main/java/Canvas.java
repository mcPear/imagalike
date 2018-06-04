
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class Canvas extends JPanel {
    private final String image1Path;
    private final String image2Path;

    public Canvas(String image1Path, String image2Path) {
        super();
        this.image1Path = image1Path;
        this.image2Path = image2Path;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        try {
            doDrawing((Graphics2D) g);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void doDrawing(Graphics2D graphics2D) throws IOException {
        Image image1 = ImageIO.read(new File(image1Path));
        Image image2 = ImageIO.read(new File(image2Path));
        graphics2D.drawImage(image1, 0, 0, null);
        graphics2D.drawImage(image2, 800, 0, null);
        if (Store.interestPointsPairs != null) {
            graphics2D.setColor(Color.GREEN);
            for (InterestPointsPair pair : Store.interestPointsPairs) {
                graphics2D.drawLine(Math.round(pair.interestPoint1.x), Math.round(pair.interestPoint1.y),
                        Math.round(pair.interestPoint2.x) + 800, Math.round(pair.interestPoint2.y));
            }
        }
        if (Store.cohesiveInterestPointsPairs != null) {
            graphics2D.setColor(Color.YELLOW);
            for (InterestPointsPair pair : Store.cohesiveInterestPointsPairs) {
                graphics2D.drawLine(Math.round(pair.interestPoint1.x), Math.round(pair.interestPoint1.y),
                        Math.round(pair.interestPoint2.x) + 800, Math.round(pair.interestPoint2.y));
            }
        }
        if (Store.haraffSiftResult2 != null) {
            graphics2D.setColor(Color.GREEN);
            for (InterestPoint interestPoint : Store.haraffSiftResult1.interestPoints) {
                graphics2D.fillOval(Math.round(interestPoint.x), Math.round(interestPoint.y), 4, 4);
            }
            for (InterestPoint interestPoint : Store.haraffSiftResult2.interestPoints) {
                graphics2D.fillOval(Math.round(interestPoint.x) + 800, Math.round(interestPoint.y), 4, 4);
            }
        }
        if (Store.bestModelFittedInterestPointsPairs != null) {
            graphics2D.setColor(Color.RED);
            for (InterestPointsPair pair : Store.bestModelFittedInterestPointsPairs) {
                graphics2D.drawLine(Math.round(pair.interestPoint1.x), Math.round(pair.interestPoint1.y),
                        Math.round(pair.interestPoint2.x) + 800, Math.round(pair.interestPoint2.y));
            }
        }
    }

}