import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;

public class Slimclient {
    private static final String TARGET_URL = "http://154.48.185.206:5000/deploy/a6442450-deb6-4b61-8c95-446bd095b8f0";

    public Slimclient() {
        Client client = ClientBuilder.newBuilder()
                .register(MultiPartFeature.class).build();
        WebTarget webTarget = client.target(TARGET_URL);
        FormDataMultiPart multipart =
                new FormDataMultiPart()
                        .field("timestamp", "2020-01-24T10:43:20.993831")
                        .field("version_id", String.valueOf(1));

        FileDataBodyPart fileDataBodyPart = new FileDataBodyPart("inputs_file",
                new File("C:\\Postdoc\\projects\\sodalite-eu\\refactoring-ml\\rule-based\\src\\test\\resources\\vehicle-iot-v1\\input.yaml"),
                MediaType.APPLICATION_OCTET_STREAM_TYPE);

        multipart.bodyPart(fileDataBodyPart);

        Response response = webTarget
                .request(MediaType.APPLICATION_JSON)
                .build("DELETE", Entity.entity(multipart, multipart.getMediaType()))
                .invoke();
        String message = response.readEntity(String.class);
        response.close();
        System.out.println(message);
        System.out.println(response.getStatus() + " "
                + response.getStatusInfo() + " " + response);
    }

    public static void main(String[] args) {
        new Slimclient();
    }
}
