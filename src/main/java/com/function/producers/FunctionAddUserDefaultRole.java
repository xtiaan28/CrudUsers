package com.function.producers;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;
import com.function.model.User;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public class FunctionAddUserDefaultRole {

    // Configuración de Event Grid (deberías mover esto a variables de entorno)
    private static final String TOPIC_ENDPOINT = "https://g14-eventgrid-asignarol.eastus2-1.eventgrid.azure.net/api/events";
    private static final String TOPIC_KEY = "loMm2mXG5B4TO24qkChHbA4Upuhb1uH0nUi67K4FE6FRx4FYcniMJQQJ99BEACHYHv6XJ3w3AAABAZEGgp2w";

    @FunctionName("CreateUserWithDefaultRole")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {
                    HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<User>> request,
            final ExecutionContext context) {

        Logger logger = context.getLogger();

        // Validación básica
        if (!request.getBody().isPresent()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Cuerpo de solicitud vacío")
                    .build();
        }

        User user = request.getBody().get();

        if (user.getUsername() == null || user.getUsername().trim().isEmpty() ||
                user.getPassword() == null || user.getPassword().trim().isEmpty() ||
                user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Username, password y email son requeridos")
                    .build();
        }

        try {
            // Publicar evento con todos los datos necesarios
            publishUserCreationEvent(user, logger);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Se enviaron los siguientes datos al Event Grid");
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            return request.createResponseBuilder(HttpStatus.ACCEPTED)
                    .header("Content-Type", "application/json")
                    .body(response)
                    .build();

        } catch (Exception e) {
            logger.severe("Error al publicar evento: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar solicitud: " + e.getMessage())
                    .build();
        }
    }

    private void publishUserCreationEvent(User user, Logger logger) {
        try {
            EventGridPublisherClient<EventGridEvent> client = new EventGridPublisherClientBuilder()
                    .endpoint(TOPIC_ENDPOINT)
                    .credential(new AzureKeyCredential(TOPIC_KEY))
                    .buildEventGridEventPublisherClient();

            JsonObject data = new JsonObject();
            data.addProperty("username", user.getUsername());
            data.addProperty("password", user.getPassword());
            data.addProperty("email", user.getEmail());

            EventGridEvent event = new EventGridEvent(
                    "Users/Create", // topic
                    "User.CreationRequested", // eventType
                    BinaryData.fromObject(data), // data
                    "1.0" // dataVersion
            );

            client.sendEvent(event);
            logger.info("Evento de creación de usuario publicado para: " + user.getUsername());
        } catch (Exception e) {
            logger.severe("Error al publicar evento de asignación de rol: " + e.getMessage());
            throw new RuntimeException("Error al publicar evento", e);
        }
    }
}
