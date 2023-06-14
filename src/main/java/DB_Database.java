import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DB_Database {

    private static DB_Database db_database;
    private Connection connection;
    private final DB_CachedConnection cachedConnection = DB_CachedConnection.getInstance();

    private DB_Database() {
        try {
            System.out.println(DB_Properties.getProperty("driver"));
            Class.forName(DB_Properties.getProperty("driver"));
            connect();
        } catch (ClassNotFoundException | RuntimeException | SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void connect() throws SQLException {
        if (connection != null) {
            disconnect();
        }
            connection = DriverManager.getConnection(DB_Properties.getProperty("url"),
                    DB_Properties.getProperty("username"),
                    DB_Properties.getProperty("password"));
        cachedConnection.setConnection(connection);
    }

    public void disconnect() throws SQLException {
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public Statement getStatement() throws SQLException{
        if (connection != null) {
            return cachedConnection.getStatement();
        }
        throw new RuntimeException("Not connected to DB");
    }

    public void releaseStatement(Statement statement) {
        if (connection != null) {
            cachedConnection.releaseStatement(statement);
        }
        else {
            throw new RuntimeException("Not connected to db");
        }
    }

    public static DB_Database getDBDatabase() {
        if (db_database == null) db_database = new DB_Database();
        return db_database;
    }

    public static void main(String[] args) {
        DB_Database db_database = DB_Database.getDBDatabase();
    }


}
