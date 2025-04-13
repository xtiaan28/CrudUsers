package com.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import java.util.Optional;
import java.util.logging.Logger;
import com.function.service.UserService;
import com.function.model.User;

public class FunctionUpdateUser {
    @FunctionName("UpdateUser")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.PUT},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "users/{id}") // Parámetro dinámico en la URL
            HttpRequestMessage<Optional<User>> request,
            @BindingName("id") int userId, // Obtiene el ID de la URL
            final ExecutionContext context) {
        
        Logger logger = context.getLogger();
        logger.info("Actualizando usuario con ID: " + userId);

        try {
            Optional<User> userRequest = request.getBody();

            if (!userRequest.isPresent()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("Cuerpo de solicitud vacío")
                        .build();
            }

            User userToUpdate = userRequest.get();
            userToUpdate.setUserId(userId); // Asegurar que se actualiza el usuario correcto

            try {
                boolean updated = UserService.updateUser(userToUpdate);
        
                if (updated) {
                    return request.createResponseBuilder(HttpStatus.OK)
                            .body("Usuario actualizado correctamente")
                            .build();
                } else {
                    return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                            .body("No se encontró el usuario con ID: " + userId)
                            .build();
                }
            } catch (Exception e) {
                logger.severe("Error al actualizar usuario: " + e.getMessage());
                return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error interno al actualizar usuario: " + e.getMessage())
                        .build();
            }
        } catch (Exception e) {
            logger.severe("Error al actualizar usuario: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar la solicitud")
                    .build();
        }
    }
}
