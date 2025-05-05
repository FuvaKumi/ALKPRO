import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;

public class UsersPanel extends JPanel {
    private DatabaseManager dbManager;
    private DefaultTableModel tableModel;

    public UsersPanel(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        setLayout(new BorderLayout());
        try {
            DatabaseManager.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        tableModel = new DefaultTableModel(new Object[]{"ID", "Név", "Email"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);
        
        JPanel inputPanel = new JPanel(new GridLayout(1, 3));
        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        inputPanel.add(nameField);
        inputPanel.add(emailField);

        JPanel buttons = new JPanel();
        JButton refreshButton = new JButton("Frissítés");
        JButton addButton = new JButton("Hozzáadás");
        JButton deleteButton = new JButton("Törlés");
        buttons.add(refreshButton);
        inputPanel.add(addButton);
        buttons.add(deleteButton);
        
        JPanel labelPanel = new JPanel(new GridLayout(1, 3));
        labelPanel.add(new JLabel("Név"));
        labelPanel.add(new JLabel("Email"));
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

        addButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();

            if (!name.isEmpty() && email.contains("@")) {
                try {
                    PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                            "INSERT INTO Users (name, email) VALUES (?, ?)");
                    stmt.setString(1, name);
                    stmt.setString(2, email);
                    stmt.executeUpdate();
                    refreshTable();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Helyes nevet és email címet adj meg.");
            }
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                int id = (int) tableModel.getValueAt(selectedRow, 0);
                try {
                    dbManager.deleteById("Users", id);
                    refreshTable();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        try (ResultSet rs = dbManager.getAll("Users")) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
