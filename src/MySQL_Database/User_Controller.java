package MySQL_Database;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.Response;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class User_Controller {

    public User_Controller() {}

    public Response login_user(JsonObject credentials){
        Response response = Response.status(Response.Status.UNAUTHORIZED).build();
        String email = credentials.getString("email");
        String password = credentials.getString("password");

        if (email.equals("") || password.equals("")) {
            response = Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Json.createObjectBuilder().add("response", "Fyll i båda fälten innan du klickar på 'Logga in'"))
                        .build();
        } else {

            try {
                String SQL = "SELECT login(?,?) AS RESULT";
                PreparedStatement stmt = MySQL_Connection.getInstance().getConnection().prepareStatement(SQL);
                stmt.setString(1, email);
                stmt.setString(2, password);

                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    int response_value = rs.getInt("RESULT");

                    switch (response_value) {
                        case 1:
                            response = Response.ok(getUser(email)).build();
                            break;
                        case 2:
                            response = Response.status(Response.Status.UNAUTHORIZED)
                                    .entity(Json.createObjectBuilder().add("response", "Emejlen finns inte med i databasen.").build())
                                    .build();
                            break;

                        case 3:
                            response = Response.status(Response.Status.UNAUTHORIZED)
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

    public Response getFriends(int user_ID) {
        Response response = Response.status(Response.Status.NOT_FOUND).build();

        try{
            String SQL = "CALL view_friends(?);";
            PreparedStatement stmt = MySQL_Connection.getInstance().getConnection().prepareStatement(SQL);
            stmt.setInt(1, user_ID);

            ResultSet rs = stmt.executeQuery();

            JsonArrayBuilder friends_list = Json.createArrayBuilder();

            while (rs.next()) {
                JsonObjectBuilder friend = Json.createObjectBuilder()
                        .add("name", rs.getString("Friends"));

                friends_list.add(friend);
            }

            response = Response.ok(friends_list.build()).build();

        } catch (Exception e) {
            System.out.println("Error in User_Controller::getFriends" + e.getMessage());
            e.printStackTrace();
        }

        return response;
    }
}
