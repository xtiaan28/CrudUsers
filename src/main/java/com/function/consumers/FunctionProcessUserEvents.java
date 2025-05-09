package com.function.consumers;

import com.azure.messaging.eventgrid.EventGridEvent;
import com.function.model.User;
import com.function.service.UserService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import java.util.Map;
import java.util.logging.Logger;

public class FunctionProcessUserEvents {
    @FunctionName("ProcessUserEvents")
    public void run(
            @EventGridTrigger(name = "eventGridEvent") String content,
            final ExecutionContext context) {

        Logger logger = context.getLogger();
        logger.info("Evento de actualización de usuario recibido.");
        logger.info("Contenido recibido: " + content);

        try {
            Gson gson = new Gson();
            JsonObject eventGridEvent = gson.fromJson(content, JsonObject.class);

            String eventType = eventGridEvent.get("eventType").getAsString();
            JsonObject data = eventGridEvent.getAsJsonObject("data");

            logger.info("Tipo de evento: " + eventType);

            switch (eventType) {
                case "User.Updated":
                    int userId = data.get("userId").getAsInt();
                    String username = data.get("username").getAsString();
                    String email = data.get("email").getAsString();
                    String password = data.get("password").getAsString();
                    int roleId = data.get("roleId").getAsInt();

                    logger.info("Actualizando usuario: ID=" + userId);

                    User user = new User();
                    user.setUserId(userId);
                    user.setUsername(username);
                    user.setEmail(email);
                    user.setPassword(password);
                    user.setRoleId(roleId);

                    User updatedUser = UserService.updateUser(user);
                    if (updatedUser != null) {
                        logger.info("Usuario actualizado correctamente: " + updatedUser);
                    } else {
                        logger.warning("No se pudo actualizar el usuario con ID: " + userId);
                    }
                    break;

                case "User.Deleted":
                    int deletedUserId = data.get("userId").getAsInt();
                    logger.info("Eliminando usuario con ID: " + deletedUserId);

                    boolean deleted = UserService.deleteUser(deletedUserId);
                    if (deleted) {
                        logger.info("Usuario eliminado correctamente.");
                    } else {
                        logger.warning("No se encontró el usuario con ID: " + deletedUserId);
                    }
                    break;

                default:
                    logger.warning("Tipo de evento no reconocido: " + eventType);
                    break;
            }

        } catch (Exception e) {
            logger.severe("Error procesando evento: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
