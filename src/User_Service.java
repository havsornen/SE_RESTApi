import MySQL_Database.User_Controller;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.StringReader;

@Path("/user")
public class User_Service {

    @POST
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(String body){
        JsonReader JR = Json.createReader(new StringReader(body));
        JsonObject credentials = JR.readObject();

        User_Controller user_controller = new User_Controller();


        return user_controller.login_user(credentials);
    }
}
