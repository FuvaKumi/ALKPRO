import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) {
        try {
            DatabaseManager db = new DatabaseManager("database.db");
            SwingUtilities.invokeLater(() -> new MainFrame(db).setVisible(true));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
