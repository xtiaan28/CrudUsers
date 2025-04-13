package com.function;

import com.function.service.UserService;
import com.function.model.User;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Logger;

public class FunctionAddUser {

    @FunctionName("CreateUser")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<User>> request,
            final ExecutionContext context) {
        
        Logger logger = context.getLogger();
        logger.info("Iniciando función para crear usuario");

        // 1. Validar que el body contiene un usuario
        Optional<User> optionalUser = request.getBody();
        if (!optionalUser.isPresent()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("El cuerpo de la solicitud está vacío o no es válido")
                    .build();
        }

        User user = optionalUser.get();

        try {
            // 2. Insertar usuario en la base de datos
            User createdUser = UserService.createUser(user);

            logger.info("Usuario creado con ID: " + createdUser.getUserId());

            // 3. Retornar respuesta con el usuario creado
            return request.createResponseBuilder(HttpStatus.CREATED)
                    .header("Content-Type", "application/json")
                    .body(createdUser)
                    .build();
        } catch (SQLException e) {
            logger.severe("Error de base de datos: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear usuario: " + e.getMessage())
                    .build();
        }
    }
}
