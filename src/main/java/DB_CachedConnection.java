import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

public class DB_CachedConnection {
    private static DB_CachedConnection theinstance;

    private DB_CachedConnection() {
    }

    public static DB_CachedConnection getInstance() {
        synchronized (DB_CachedConnection.class) {
            if (theinstance == null) theinstance = new DB_CachedConnection();
            return theinstance;
        }
    }
    private LinkedList<Statement> SQLqueue = new LinkedList<>();
    private Connection connection;

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public synchronized Statement getStatement() throws SQLException {
        if (SQLqueue.isEmpty()) {
            return connection.createStatement();
        }
        return SQLqueue.poll();
    }

    public synchronized void releaseStatement(Statement statement) {
        SQLqueue.offer(statement);
    }

}
