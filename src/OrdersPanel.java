import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;

public class OrdersPanel extends JPanel {
    private DatabaseManager dbManager;
    private DefaultTableModel tableModel;

    public OrdersPanel(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        setLayout(new BorderLayout());

        try {
            DatabaseManager.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        tableModel = new DefaultTableModel(new Object[]{"ID", "Vásárló ID", "Termék ID", "Mennyiség"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new GridLayout(1, 4));
        JComboBox<String> userIdComboBox = new JComboBox<String>();
        JComboBox<String> productIdComboBox = new JComboBox<String>();
        JTextField quantityField = new JTextField();
        inputPanel.add(userIdComboBox);
        inputPanel.add(productIdComboBox);
        inputPanel.add(quantityField);

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:database.db");
         PreparedStatement stmt = conn.prepareStatement("SELECT id, name FROM Users");
         ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                userIdComboBox.addItem(id + " - " + name);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Hiba a vásárlók beolvasásakor: " + e.getMessage());
            return;
        }

        try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement("SELECT id, name FROM Products");
         ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                productIdComboBox.addItem(id + " - " + name);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Hiba a termékek beolvasásakor: " + e.getMessage());
            return;
        }

        JPanel buttons = new JPanel();
        JButton refreshButton = new JButton("Frissítés");
        JButton addButton = new JButton("Hozzáadás");
        JButton deleteButton = new JButton("Törlés");
        buttons.add(refreshButton);
        inputPanel.add(addButton);
        buttons.add(deleteButton);

        JPanel labelPanel = new JPanel(new GridLayout(1, 4));
        labelPanel.add(new JLabel("Vevő"));
        labelPanel.add(new JLabel("Termék"));
        labelPanel.add(new JLabel("Mennyiség"));

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

        addButton.addActionListener(_ -> {
            try {
                DatabaseManager.getConnection();
                String selectedUser = (String) userIdComboBox.getSelectedItem();
                int userId = Integer.parseInt(selectedUser.split(" - ")[0]);
                String selectedProduct = (String) productIdComboBox.getSelectedItem();
                int productId = Integer.parseInt(selectedProduct.split(" - ")[0]);
                int quantity = Integer.parseInt(quantityField.getText().trim());
                
                dbManager.insertOrder(userId, productId, quantity);

                refreshTable();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Hibás adatbevitel.\n" + ex.getMessage());
            }
        });

        deleteButton.addActionListener(_ -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                int id = (int) tableModel.getValueAt(selectedRow, 0);
                try {
                    dbManager.deleteById("Orders", id);
                    refreshTable();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        try (ResultSet rs = dbManager.getAll("Orders")) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getInt("userId"),
                        rs.getInt("productId"),
                        rs.getInt("quantity")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
