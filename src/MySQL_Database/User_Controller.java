package MySQL_Database;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.core.Response;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class User_Controller {

    public User_Controller() {}

    public Response login_user(JsonObject credentials){
        Response response = Response.status(Response.Status.BAD_REQUEST).build();

        try {
            String SQL = "SELECT login(?,?)";
            PreparedStatement stmt = MySQL_Connection.getInstance().getConnection().prepareStatement(SQL);
            stmt.setString(1, credentials.getString("email"));
            stmt.setString(2, credentials.getString("password"));

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int response_value = rs.getInt("login('"+ credentials.getString("email") + "','"+ credentials.getString("password") +"')");

                switch (response_value) {
                    case 1:
                        response = Response.ok(getUser(credentials.getString("email"))).build();
                        break;
                    case 2:
                        response = Response.status(Response.Status.BAD_REQUEST)
                                    .entity(Json.createObjectBuilder().add("response", "Emejlen finns inte med i databasen.").build())
                                    .build();
                        break;

                    case 3:
                        response = Response.status(Response.Status.BAD_REQUEST)
                                    .entity(Json.createObjectBuilder().add("response", "Fel lösenord.").build())
                                    .build();
                        break;
                }
            }

        } catch (Exception e) {
            System.out.println("Error in User_Controller::login_user! \n");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return response;
    }

    public Response create_user(JsonObject user_values) {
        Response response = Response.status(Response.Status.NOT_FOUND).build();

        try {
            String SQL = "SELECT create_user(?, ?, ?, ?, ?) AS 'RESULT';";
            PreparedStatement stmt = MySQL_Connection.getInstance().getConnection().prepareStatement(SQL);
            stmt.setString(1, user_values.getString("email"));
            stmt.setString(2, user_values.getString("pwd"));
            stmt.setString(3, user_values.getString("fName"));
            stmt.setString(4, user_values.getString("lName"));
            stmt.setString(5, user_values.getString("telNumber"));

            ResultSet rs = stmt.executeQuery();

            if (rs.next()){
                switch (rs.getInt("RESULT")){
                    case 0:
                        response = Response.status(Response.Status.BAD_REQUEST)
                                .entity(Json.createObjectBuilder().add("response", "Error while creating a account."))
                                .build();
                        break;
                    case 1:
                        response = Response.status(Response.Status.CREATED)
                                .entity(getUser(user_values.getString("email")))
                                .build();
                }
            }

        } catch (Exception e) {
            System.out.println("Error in User_Controller::create_user \n");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return response;
    }

    private JsonObject getUser(String email){
        JsonObject JO = Json.createObjectBuilder().build();

        try {
            String SQL = "SELECT * FROM users WHERE usr_email = ?;";
            PreparedStatement stmt = MySQL_Connection.getInstance().getConnection().prepareStatement(SQL);
            stmt.setString(1, email);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()){
                JO = Json.createObjectBuilder()
                        .add("usr_email", rs.getString("usr_email"))
                        .add("usr_ID", rs.getString("usr_ID"))
                        .add("usr_firstname", rs.getString("usr_firstname"))
                        .add("usr_lastname", rs.getString("usr_lastname"))
                        .add("usr_telnumber", rs.getString("usr_telnumber"))
                        .build();
            }
        } catch (Exception e) {
            System.out.println("Error in User_Controller::getUser \n");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
            return JO;
    }
}
