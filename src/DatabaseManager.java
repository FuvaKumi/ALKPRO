import java.sql.*;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:database.db";
    private static Connection conn;

    public static Connection getConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            conn = DriverManager.getConnection(DB_URL);
        }
        return conn;
    }

    public DatabaseManager(String dbPath) throws SQLException {
        conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        createTables();
    }

    private void createTables() throws SQLException {
        String[] tables = {
            "CREATE TABLE IF NOT EXISTS Users (id INTEGER PRIMARY KEY, name TEXT NOT NULL, email TEXT NOT NULL);",
            "CREATE TABLE IF NOT EXISTS Products (id INTEGER PRIMARY KEY, name TEXT NOT NULL, categoryId INTEGER NOT NULL, price REAL NOT NULL, stock INTEGER NOT NULL CHECK (stock >= 0), FOREIGN KEY(categoryId) REFERENCES Categories(id));",
            "CREATE TABLE IF NOT EXISTS Categories (id INTEGER PRIMARY KEY, name TEXT NOT NULL);",
            "CREATE TABLE IF NOT EXISTS Orders (id INTEGER PRIMARY KEY, userId INTEGER NOT NULL, productId INTEGER NOT NULL, quantity INTEGER NOT NULL, FOREIGN KEY(userId) REFERENCES Users(id), FOREIGN KEY(productId) REFERENCES Products(id));"
        };
        for (String sql : tables) {
            conn.createStatement().execute(sql);
        }
    }

    public ResultSet getAll(String tableName) throws SQLException {
        String query = "SELECT * FROM " + tableName;
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(query);
    }
    
    public void deleteById(String tableName, int id) throws SQLException {
        String query = "DELETE FROM " + tableName + " WHERE id = ?";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setInt(1, id);
        pstmt.executeUpdate();
    }

    public void insertProduct(String name, int categoryId, double price, int stock) throws SQLException {
        if (stock < 0) throw new IllegalArgumentException("A készlet nem lehet negatív.");
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO Products (name, categoryId, price, stock) VALUES (?, ?, ?, ?);");
        stmt.setString(1, name);
        stmt.setInt(2, categoryId);
        stmt.setDouble(3, price);
        stmt.setInt(4, stock);
        stmt.executeUpdate();
    }
    
    
    public void deleteProduct(int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM Products WHERE id = ?;");
        stmt.setInt(1, id);
        stmt.executeUpdate();
    }
    
    public void insertCategory(String name) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO Categories (name) VALUES (?);");
        stmt.setString(1, name);
        stmt.executeUpdate();
    }
    
    public void deleteCategory(int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM Categories WHERE id = ?;");
        stmt.setInt(1, id);
        stmt.executeUpdate();
    }    
    
    public void insertOrder(int userId, int productId, int quantity) throws SQLException {
    
        PreparedStatement checkStock = conn.prepareStatement("SELECT stock FROM Products WHERE id = ?;");
        checkStock.setInt(1, productId);
        ResultSet rs = checkStock.executeQuery();
        if (rs.next()) {
            int stock = rs.getInt("stock");
            if (stock < quantity) {
                throw new IllegalArgumentException("Nincs elegendő készlet a termékből.");
            }
        } else {
            throw new IllegalArgumentException("Termék nem található.");
        }
    
        PreparedStatement updateStock = conn.prepareStatement("UPDATE Products SET stock = stock - ? WHERE id = ?;");
        updateStock.setInt(1, quantity);
        updateStock.setInt(2, productId);
        updateStock.executeUpdate();
    
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO Orders (userId, productId, quantity) VALUES (?, ?, ?);");
        stmt.setInt(1, userId);
        stmt.setInt(2, productId);
        stmt.setInt(3, quantity);
        stmt.executeUpdate();
    }
    
    
    public void deleteOrder(int id) throws SQLException {

        PreparedStatement stmt = conn.prepareStatement("DELETE FROM Orders WHERE id = ?;");
        stmt.setInt(1, id);
        stmt.executeUpdate();
    }

    public void insertUser(String name, String email) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO Users (name, email) VALUES (?, ?);");
        stmt.setString(1, name);
        stmt.setString(2, email);
        stmt.executeUpdate();
    }
    
    
    public void deleteUser(int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM Users WHERE id = ?;");
        stmt.setInt(1, id);
        stmt.executeUpdate();
    }

    public boolean userExists(int userId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM Users WHERE id = ?");
        stmt.setInt(1, userId);
        ResultSet rs = stmt.executeQuery();
        return rs.next() && rs.getInt(1) > 0;
    }
    
    
}
