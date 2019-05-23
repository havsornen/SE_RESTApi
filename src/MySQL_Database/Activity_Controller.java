package MySQL_Database;


import javax.json.*;
import javax.ws.rs.core.Response;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Activity_Controller {

    public  Activity_Controller() {}

    public Response create_activity(JsonObject activity_info) {
        Response response = Response.status(Response.Status.BAD_REQUEST).build();


        try {
            String SQL = "SELECT create_activity(?,?,?,?,?,?,?) AS 'RESULT';";
            PreparedStatement stmt = MySQL_Connection.getInstance().getConnection().prepareStatement(SQL);
            stmt.setInt(1, activity_info.getInt("act_owner"));
            stmt.setString(2, activity_info.getString("act_title"));
            stmt.setString(3, activity_info.getString("start_d"));
            stmt.setString(4, activity_info.getString("end_d"));
            stmt.setInt(5, activity_info.getInt("act_permis"));
            stmt.setString(6, activity_info.getString("act_desc"));
            stmt.setString(7, activity_info.getString("act_type"));

            ResultSet rs = stmt.executeQuery();
            if (rs.next()){
                int response_value = rs.getInt("RESULT");

                switch (response_value) {
                    case 1:
                        response = invite_invitees(activity_info);
                        break;
                    case 2:
                        response = Response.status(Response.Status.BAD_REQUEST)
                                .entity(Json.createObjectBuilder().add("response", "Användaren har redan en activitet vid detta namnet.").build())
                                .build();
                        break;

                    case 3:
                        response = Response.status(Response.Status.BAD_REQUEST)
                                .entity(Json.createObjectBuilder().add("response", "Alla fält är inte ifyllda").build())
                                .build();
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println("Error in Activity_Controller::create_activity: " + e.getMessage());
            e.printStackTrace();
        }

        return response;
    }

    private Response invite_invitees(JsonObject activity_info) {
        Response response = Response.status(Response.Status.BAD_REQUEST).build();

        try {
            String SQL = "SELECT * FROM activity WHERE activity_owner = ? AND activity_title = ?;";
            PreparedStatement stmt = MySQL_Connection.getInstance().getConnection().prepareStatement(SQL);
            stmt.setInt(1, activity_info.getInt("act_owner"));
            stmt.setString(2, activity_info.getString("act_title"));

            ResultSet full_activity = stmt.executeQuery();

            if (full_activity.next()) {
                String activity_title = full_activity.getString("activity_title");
                int owner_ID = full_activity.getInt("activity_owner");

                String SQL_friends = "SELECT usr_ID FROM users WHERE usr_ID IN (SELECT REPLACE(CONCAT(usr_1, usr_2), ?, '') AS Friends FROM usr_relations WHERE usr_1 = ? OR usr_2 = ?);";
                PreparedStatement friend_stmt = MySQL_Connection.getInstance().getConnection().prepareStatement(SQL_friends);
                friend_stmt.setInt(1, owner_ID);
                friend_stmt.setInt(2, owner_ID);
                friend_stmt.setInt(3, owner_ID);

                ResultSet friends_rs = friend_stmt.executeQuery();

                int invited_friends = 0;
                while (friends_rs.next()) {
                    String SQL_add = "SELECT add_invitee(?,?,?) AS 'RESULT';";
                    PreparedStatement add_stmt = MySQL_Connection.getInstance().getConnection().prepareStatement(SQL_add);
                    add_stmt.setInt(1, friends_rs.getInt("usr_ID"));
                    add_stmt.setString(2, activity_title);
                    add_stmt.setInt(3, owner_ID);

                    ResultSet friend_return = add_stmt.executeQuery();

                    if (friend_return.next()){
                        invited_friends++;
                    }
                }

                response = Response.ok(Json.createObjectBuilder().add("Friends_added", invited_friends).build()).build();
            }

        } catch (Exception e) {
            System.out.println("Error in Activity_Controller::invite_invitees: " + e.getMessage());
            e.printStackTrace();
        }

        return response;
    }

    public Response getActivites(int userID) {
        Response response = Response.status(Response.Status.BAD_REQUEST).build();

        try {
            String SQL = "CALL get_activities(?);";
            PreparedStatement stmt = MySQL_Connection.getInstance().getConnection().prepareStatement(SQL);
            stmt.setInt(1, userID);
            ResultSet rs = stmt.executeQuery();

            JsonArrayBuilder activities = Json.createArrayBuilder();

            while (rs.next()) {
               JsonObject JO = Json.createObjectBuilder()
                       .add("activity_ID", rs.getString("activity_ID"))
                       .add("activity_owner", rs.getString("activity_owner"))
                       .add("activity_desciption", rs.getString("activity_desciption"))
                       .add("activity_title", rs.getString("activity_title"))
                       .add("activity_start", rs.getString("activity_start"))
                       .add("activity_end", rs.getString("activity_end"))
                       .add("activity_created", rs.getString("activity_created"))
                       .add("activity_type", rs.getString("activity_type"))
                       .add("activity_permission", rs.getString("activity_permission"))
                       .build();

              activities.add(JO);
            }

            response = Response.ok(activities.build()).build();

        } catch (Exception e) {
            System.out.println("Error in getActivites: " + e.getMessage());
            e.printStackTrace();

            response = Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }

        return response;
    }
    /*
    * Soo this is a weird function, we have to find a way to properly check
    * if a procedure ended with a success. Casue it doenst do that now.
    */

    public Response respond_activity(JsonObject data_response) {
        Response response = Response.status(Response.Status.BAD_REQUEST).build();
        int userID = data_response.getInt("usr_ID");
        int activityID = data_response.getInt("activity_ID");
        int responseID = data_response.getInt("response");

        try {
            String SQL = "CALL activity_response(?, ?, ?);";
            PreparedStatement stmt = MySQL_Connection.getInstance().getConnection().prepareStatement(SQL);
            stmt.setInt(1, userID);
            stmt.setInt(2, activityID);
            stmt.setInt(3, responseID);

            ResultSet rs = stmt.executeQuery();

            response = Response.ok().build();

        } catch (Exception e) {
            System.out.println("Error in Activity_Controller::respond_activity: " + e.getMessage());
            e.printStackTrace();
        }

        return response;

    }

    public Response getMembers(int activity_id, boolean invitees) {
        Response response = Response.status(Response.Status.BAD_REQUEST).build();

        try {
            String SQL = "SELECT users.usr_ID, users.usr_firstname, users.usr_lastname, users.usr_email, users.usr_telnumber, AM.activity_member_type " +
                        "FROM activity_members as AM " +
                        "JOIN users " +
                        "WHERE activity_ID = ? AND users.usr_ID = AM.usr_ID;";
            if (invitees) {
                SQL = "SELECT users.usr_ID, users.usr_firstname, users.usr_lastname, users.usr_email, users.usr_telnumber " +
                        "FROM activity_invitees as AI " +
                        "JOIN users " +
                        "WHERE activity_ID = ? AND users.usr_ID = AI.usr_ID;";
            }

            PreparedStatement stmt = MySQL_Connection.getInstance().getConnection().prepareStatement(SQL);
            stmt.setInt(1, activity_id);

            ResultSet rs = stmt.executeQuery();
            JsonArrayBuilder result_data = Json.createArrayBuilder();

            while(rs.next()) {
                JsonObjectBuilder user = Json.createObjectBuilder()
                        .add("usr_ID", rs.getString("usr_ID"))
                        .add("usr_firstname", rs.getString("usr_firstname"))
                        .add("usr_lastname", rs.getString("usr_lastname"))
                        .add("usr_email", rs.getString("usr_email"))
                        .add("usr_telnumber", rs.getString("usr_telnumber"));

                if (!invitees) {
                    user.add("usr_member_type", rs.getString("activity_member_type"));
                }

                result_data.add(user);
            }

            response = Response.ok(result_data.build()).build();

        } catch(Exception e) {
            System.out.println("Error in Activity_Controller::getMembers: " + e.getMessage());
            e.printStackTrace();
        }

        return response;
    }
}
