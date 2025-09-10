import javax.swing.*;

public class Tester {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new  WeatherAppGUI().setVisible(true));
    }
}
