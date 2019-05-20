import MySQL_Database.Activity_Controller;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.StringReader;

@Path("/activity")
public class Activity_Service {

    private Activity_Controller AC;

    public Activity_Service() {
        this.AC = new Activity_Controller();
    }

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create_activity(String body){
        JsonReader JR = Json.createReader(new StringReader(body));
        JsonObject activity_info = JR.readObject();

        return this.AC.create_activity(activity_info);
    }

    @GET
    @Path("/activity_feed/{user_ID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get_activity(@PathParam("user_ID") int user_ID){

        return this.AC.getActivites(user_ID);
    }
}
