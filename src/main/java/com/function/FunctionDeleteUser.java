package com.function;

import com.function.service.UserService;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import java.util.logging.Logger;

public class FunctionDeleteUser {
    @FunctionName("DeleteUser")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.DELETE},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "users/{id}") HttpRequestMessage<Void> request,
            @BindingName("id") int userId,
            final ExecutionContext context) {
        
        Logger logger = context.getLogger();
        logger.info("Iniciando eliminación del usuario con ID: " + userId);

        try {
            boolean deleted = UserService.deleteUser(userId);

            if (deleted) {
                return request.createResponseBuilder(HttpStatus.OK)
                        .body("Usuario eliminado correctamente.")
                        .build();
            } else {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .body("No se encontró el usuario con ID: " + userId)
                        .build();
            }
        } catch (Exception e) {
            logger.severe("Error al eliminar usuario: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al eliminar usuario: " + e.getMessage())
                    .build();
        }
    }
}
