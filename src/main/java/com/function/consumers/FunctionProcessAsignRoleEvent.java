package com.function.consumers;

import com.google.gson.*;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import com.function.model.User;
import com.function.service.UserService;

import java.sql.SQLException;
import java.util.logging.Logger;

public class FunctionProcessAsignRoleEvent {

    private static final int DEFAULT_ROLE = 2;

    @FunctionName("ProcessUserCreationAndAsignRole")
    public void run(
            @EventGridTrigger(name = "eventGridEvent") String content,
            final ExecutionContext context) {

        Logger logger = context.getLogger();
        logger.info("########## INICIO DE PROCESAMIENTO DE EVENTO ##########");
        logger.info("Evento recibido (raw content):\n" + content);

        try {
            // 1. Parseo del evento
            logger.info("Iniciando parseo del JSON del evento...");
            JsonObject eventGridEvent = JsonParser.parseString(content).getAsJsonObject();

            if (!eventGridEvent.has("data")) {
                logger.warning(
                        "El evento no contiene el campo 'data'. Estructura completa recibida:\n" + eventGridEvent);
                throw new RuntimeException("Campo 'data' no encontrado en el evento");
            }

            JsonObject data = eventGridEvent.getAsJsonObject("data");
            logger.info("Datos del evento parseados correctamente");

            // 2. Validar que existe el objeto 'members'
            if (!data.has("members")) {
                logger.severe("El campo 'members' no existe en los datos del evento");
                throw new RuntimeException("Estructura del evento inválida: falta campo 'members'");
            }

            JsonObject members = data.getAsJsonObject("members");
            logger.info("Extrayendo datos del usuario...");
            String username = members.getAsJsonObject("username").get("value").getAsString();
            String password = members.getAsJsonObject("password").get("value").getAsString();
            String email = members.getAsJsonObject("email").get("value").getAsString();

            logger.info("Datos del usuario extraídos:");
            logger.info("- Username: " + username);
            logger.info("- Email: " + email);
            logger.info("- Password: [PROTEGIDO]");
            logger.info("- Rol a asignar: " + DEFAULT_ROLE);

            // 3. Creación del objeto User
            logger.info("Creando objeto User...");
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
            user.setEmail(email);
            user.setRoleId(DEFAULT_ROLE);

            // 4. Inserción en base de datos
            logger.info("Iniciando inserción en base de datos...");
            User createdUser = UserService.createUser(user);

            logger.info("########## OPERACIÓN EXITOSA ##########");
            logger.info("Usuario creado exitosamente:");
            logger.info("- ID asignado: " + createdUser.getUserId());
            logger.info("- Username: " + createdUser.getUsername());
            logger.info("- Email: " + createdUser.getEmail());
            logger.info("- Rol asignado: " + createdUser.getRoleId());

        } catch (JsonSyntaxException e) {
            logger.severe("ERROR DE PARSEO JSON: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            logger.severe("ERROR DE BASE DE DATOS: " + e.getMessage());
            logger.severe("Código de error SQL: " + e.getErrorCode());
            logger.severe("Estado SQL: " + e.getSQLState());
            e.printStackTrace();
        } catch (Exception e) {
            logger.severe("ERROR INESPERADO: " + e.getMessage());
            e.printStackTrace();
        } finally {
            logger.info("########## FIN DE PROCESAMIENTO ##########");
        }
    }   
}