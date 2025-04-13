package com.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import java.util.Optional;
import java.util.logging.Logger;
import com.function.service.UserService;
import com.function.model.User;

public class FunctionGetUserById {
    @FunctionName("GetUserById")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "users/{id}") // Parámetro dinámico en la URL
            HttpRequestMessage<Optional<String>> request,
            @BindingName("id") int userId, // Obtiene el ID de la URL
            final ExecutionContext context) {
        
        Logger logger = context.getLogger();
        logger.info("Buscando usuario con ID: " + userId);
        
        try {
            User user = UserService.getUserById(userId);
            if (user != null) {
                return request.createResponseBuilder(HttpStatus.OK)
                        .header("Content-Type", "application/json")
                        .body(user)
                        .build();
            } else {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .body("Usuario no encontrado con ID: " + userId)
                        .build();
            }
        } catch (Exception e) {
            logger.severe("Error al obtener usuario: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar la solicitud")
                    .build();
        }
    }
}
