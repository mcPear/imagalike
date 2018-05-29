

import javax.swing.*;

public class Window extends JFrame {

    private Canvas canvas;

    public Window(String image1Path, String image2Path) {
        initUI(image1Path, image2Path);
    }

    private void initUI(String image1Path, String image2Path) {
        canvas = new Canvas(image1Path, image2Path);
        add(canvas);
        setTitle("Imagalike");
        //setSize(Dimensions.WINDOW_WIDTH, Dimensions.WINDOW_HEIGHT);
        setSize(1600, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void paintImmediately() {
        canvas.paintImmediately(canvas.getBounds());
    }

}