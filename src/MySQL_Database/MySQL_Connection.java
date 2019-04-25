package MySQL_Database;
import java.sql.Connection;
import java.sql.DriverManager;

public class MySQL_Connection {

    private static MySQL_Connection instance;
    private Connection connection;
    private String hostURL = "jdbc:mysql://localhost:3306/SE_Database?useSSL=false&serverTimezone=UTC";

    private MySQL_Connection(String username, String password){
        try {
            this.connection = DriverManager.getConnection(this.hostURL,username,password);
        } catch (Exception e) {
            System.out.println("Error connecting to database!");
            System.out.println(e.getMessage());
        }
    }

    public static MySQL_Connection getInstance(){
        if (instance == null){

            instance = new MySQL_Connection("testuser", "Testuser123!");
        }
        return instance;
    }

    public Connection getConnection(){
        return this.connection;
    }
}
