import java.sql.SQLException;

import javax.swing.*;

public class MainFrame extends JFrame {
    public MainFrame(DatabaseManager db) {
        try {
            DatabaseManager.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        setTitle("Adatbázis Kezelő");
        setSize(800, 600);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Vásárlók", new UsersPanel(db));
        tabbedPane.addTab("Termékek", new ProductsPanel(db));
        tabbedPane.addTab("Kategóriák", new CategoriesPanel(db));
        tabbedPane.addTab("Rendelések", new OrdersPanel(db));

    add(tabbedPane);
    }
}