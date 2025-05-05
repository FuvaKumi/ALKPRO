import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProductsPanel extends JPanel {
    private final DatabaseManager dbManager;
    private final DefaultTableModel tableModel;
    
    public ProductsPanel(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        setLayout(new BorderLayout());

        try {
            DatabaseManager.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        tableModel = new DefaultTableModel(new Object[]{"ID", "Név", "Kategória", "Ár", "Készlet"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);
        
        JPanel inputPanel = new JPanel(new GridLayout(1, 5));
        JTextField nameField = new JTextField();
        JComboBox<String> categoryIdComboBox = new JComboBox<String>();
        JTextField priceField = new JTextField();
        JTextField stockField = new JTextField();
        inputPanel.add(nameField);
        inputPanel.add(categoryIdComboBox);
        inputPanel.add(priceField);
        inputPanel.add(stockField);
        
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:database.db");
        PreparedStatement stmt = conn.prepareStatement("SELECT id, name FROM Categories");
        ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                categoryIdComboBox.addItem(id + " - " + name);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Hiba a kategóriák beolvasásakor: " + e.getMessage());
            return;
        }
        
        JPanel buttons = new JPanel();
        JButton refreshButton = new JButton("Frissítés");
        JButton addButton = new JButton("Hozzáadás");
        JButton deleteButton = new JButton("Törlés");
        JButton updateStockButton = new JButton("Készletmódosítás");
        updateStockButton.addActionListener(e -> openUpdateStockDialog());
        buttons.add(updateStockButton);
        buttons.add(refreshButton);
        inputPanel.add(addButton);
        buttons.add(deleteButton);
        
        JPanel labelPanel = new JPanel(new GridLayout(1, 4));
        labelPanel.add(new JLabel("Név"));
        labelPanel.add(new JLabel("Kategória"));
        labelPanel.add(new JLabel("Ár"));
        labelPanel.add(new JLabel("Készlet"));
        labelPanel.add(new JLabel());
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(labelPanel, BorderLayout.NORTH);
        topPanel.add(inputPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);
        add(buttons, BorderLayout.SOUTH);
        
        refreshTable();

        refreshButton.addActionListener((ActionEvent e) -> {
            try {
                DatabaseManager.getConnection();
                refreshTable();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }

        });

        addButton.addActionListener((ActionEvent e) -> {
            try {
                DatabaseManager.getConnection();

                String name = nameField.getText().trim();
                String selectedCategory = (String) categoryIdComboBox.getSelectedItem();
                int categoryId = Integer.parseInt(selectedCategory.split(" - ")[0]);
                double price = Double.parseDouble(priceField.getText().trim());
                int stock = Integer.parseInt(stockField.getText().trim());

                if (name.isEmpty() || price < 0 || stock < 0) {
                    JOptionPane.showMessageDialog(this, "Helytelen adatbevitel.");
                    return;
                }

                dbManager.insertProduct(name, categoryId, price, stock);
                refreshTable();
                nameField.setText("");
                priceField.setText("");
                stockField.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Az árnak és készletnek számnak kell lennie.");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Hiba a termék felvitelekor: " + ex.getMessage());
            }
        });

        deleteButton.addActionListener((ActionEvent _) -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                int productId = (int) tableModel.getValueAt(selectedRow, 0);
                try {
                    dbManager.deleteById("Products", productId);
                    refreshTable();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Hiba a törlés során: " + ex.getMessage());
                }
            }
        });

    }

    private void refreshTable() {
        try {
            DatabaseManager.getConnection();
            tableModel.setRowCount(0);
            var rs = dbManager.getAll("Products");
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int categoryId = rs.getInt("categoryId");
                double price = rs.getDouble("price");
                int stock = rs.getInt("stock");
                tableModel.addRow(new Object[]{id, name, categoryId, price, stock});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void openUpdateStockDialog() {
    JDialog dialog = new JDialog((Frame) null, "Készlet módosítása", true);
    dialog.setLayout(new GridLayout(3, 2, 10, 10));
    dialog.setSize(300, 150);
    dialog.setLocationRelativeTo(null);

    JLabel productLabel = new JLabel("Termék:");
    JComboBox<String> productComboBox = new JComboBox<>();
    JLabel stockLabel = new JLabel("Új darabszám:");
    JTextField stockField = new JTextField();

    // Termékek beolvasása az adatbázisból
    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement("SELECT id, name FROM Products");
         ResultSet rs = stmt.executeQuery()) {

        while (rs.next()) {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            productComboBox.addItem(id + " - " + name);
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Hiba a termékek beolvasásakor: " + e.getMessage());
        return;
    }

    JButton saveButton = new JButton("Mentés");
    saveButton.addActionListener(e -> {
        String selectedItem = (String) productComboBox.getSelectedItem();
        if (selectedItem == null || stockField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Minden mezőt ki kell tölteni!");
            return;
        }

        try {
            int id = Integer.parseInt(selectedItem.split(" - ")[0]);
            int newStock = Integer.parseInt(stockField.getText());

            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("UPDATE Products SET stock = ? WHERE id = ?")) {
                stmt.setInt(1, newStock);
                stmt.setInt(2, id);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(dialog, "Készlet sikeresen módosítva.");
                refreshTable();
                dialog.dispose();
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(dialog, "A darabszámnak számnak kell lennie!");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(dialog, "Adatbázishiba: " + ex.getMessage());
        }
    });

    dialog.add(productLabel);
    dialog.add(productComboBox);
    dialog.add(stockLabel);
    dialog.add(stockField);
    dialog.add(new JLabel()); // üres hely
    dialog.add(saveButton);

    dialog.setVisible(true);
}

}
