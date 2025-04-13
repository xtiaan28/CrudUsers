package com.function;

import com.function.model.User;
import com.function.service.UserService;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

public class FunctionGetUsers {

    @FunctionName("GetUsers")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
                Logger logger = context.getLogger();
        logger.info("Iniciando función para obtener usuarios de USERS_DC2");

        try {
            // 1. Obtener usuarios a través del servicio
            List<User> users = UserService.getAllUsers();

            logger.info("Usuarios encontrados: " + users.size());

            // 2. Devolver respuesta JSON
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(users)
                    .build();
        } catch (SQLException e) {
            logger.severe("Error de base de datos: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener usuarios: " + e.getMessage())
                    .build();
        }
            }
}
