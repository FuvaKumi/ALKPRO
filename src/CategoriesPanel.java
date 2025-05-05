import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;

public class CategoriesPanel extends JPanel {
    private DatabaseManager dbManager;
    private DefaultTableModel tableModel;

    public CategoriesPanel(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        setLayout(new BorderLayout());

        try {
            DatabaseManager.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        tableModel = new DefaultTableModel(new Object[]{"ID", "Név"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new GridLayout(1, 2));
        JTextField nameField = new JTextField();
        inputPanel.add(nameField);

        JPanel buttons = new JPanel();
        JButton refreshButton = new JButton("Frissítés");
        JButton addButton = new JButton("Hozzáadás");
        JButton deleteButton = new JButton("Törlés");
        buttons.add(refreshButton);
        inputPanel.add(addButton);
        buttons.add(deleteButton);

        JPanel labelPanel = new JPanel(new GridLayout(1, 4));
        labelPanel.add(new JLabel("Név"));
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

        addButton.addActionListener(_ -> {
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                try {
                    PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                            "INSERT INTO Categories (name) VALUES (?)");
                    stmt.setString(1, name);
                    stmt.executeUpdate();
                    refreshTable();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Adj meg egy kategória nevet.");
            }
        });

        deleteButton.addActionListener(_ -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                int id = (int) tableModel.getValueAt(selectedRow, 0);
                try {
                    dbManager.deleteById("Categories", id);
                    refreshTable();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        try (ResultSet rs = dbManager.getAll("Categories")) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
