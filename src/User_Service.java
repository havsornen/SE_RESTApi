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

    private User_Controller UC;

    public User_Service(){
        UC = new User_Controller();
    }

    @POST
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(String body){
        JsonReader JR = Json.createReader(new StringReader(body));
        JsonObject credentials = JR.readObject();

        return UC.login_user(credentials);
    }

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(String body){
        JsonReader JR = Json.createReader(new StringReader(body));
        JsonObject user_values = JR.readObject();

        return UC.create_user(user_values);
    }
}
