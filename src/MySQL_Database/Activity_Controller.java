package MySQL_Database;


import javax.json.Json;
import javax.json.JsonObject;
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
                        response = Response.ok().build();
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
}
