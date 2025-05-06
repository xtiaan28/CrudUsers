package com.function;

import com.function.model.User;
import com.function.provider.GraphQLProvider;
import com.function.service.UserService;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

public class UserGraphQLFunction {

    private static GraphQL graphQL;

    static {
        try {
            TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(
                    new InputStreamReader(
                            UserGraphQLFunction.class.getResourceAsStream("/schema.graphqls"),
                            StandardCharsets.UTF_8));

            RuntimeWiring wiring = RuntimeWiring.newRuntimeWiring()
                    .type("Mutation", builder -> builder
                            .dataFetcher("updateUser", env -> {
                                Integer userId = env.getArgument("userId");
                                String username = env.containsArgument("username") ? env.getArgument("username") : null;
                                String password = env.containsArgument("password") ? env.getArgument("password") : null;
                                String email = env.containsArgument("email") ? env.getArgument("email") : null;
                                Integer roleId = env.containsArgument("roleId") ? env.getArgument("roleId") : null;

                                User user = new User();
                                user.setUserId(userId);
                                user.setUsername(username);
                                user.setPassword(password);
                                user.setEmail(email);
                                user.setRoleId(roleId);

                                ExecutionContext context = env.getGraphQlContext().get("context");

                                context.getLogger()
                                        .info("Enviando usuario al Event Grid para ser actualizado: " + user);
                                // Publicar evento en Event Grid
                                EventGridPublisher.publishEvent(
                                        "User.Updated",
                                        "CrudUsers/users",
                                        user, // Publicar el usuario actualizado
                                        context);
                                return user; // Retornar el usuario actualizado

                            })
                            .dataFetcher("deleteUser", env -> {
                                int userId = env.getArgument("userId");
                                ExecutionContext context = env.getGraphQlContext().get("context");
                                context.getLogger().info("Usuario con ID " + userId
                                        + " marcado para eliminación. Enviando evento de eliminación a Event Grid.");
                                EventGridPublisher.publishEvent(
                                        "User.Deleted",
                                        "CrudUsers/users",
                                        Map.of("userId", userId),
                                        context);

                                return true;
                            }))
                    .build();

            GraphQLSchema graphQLSchema = new SchemaGenerator()
                    .makeExecutableSchema(typeRegistry, wiring);
            graphQL = GraphQL.newGraphQL(graphQLSchema).build();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FunctionName("GraphQLUsers")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {
                    HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS, route = "graphql-users") HttpRequestMessage<Optional<Map<String, Object>>> request,
            final ExecutionContext context) {

        try {
            Map<String, Object> body = request.getBody().orElse(null);
            if (body == null || !body.containsKey("query")) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("Se esperaba un campo 'query'")
                        .build();
            }

            String query = (String) body.get("query");

            ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                    .query(query)
                    .graphQLContext(Map.of("context", context)) // Pasamos ExecutionContext
                    .build();

            ExecutionResult result = graphQL.execute(executionInput);

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(result.toSpecification())
                    .build();

        } catch (Exception e) {
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error ejecutando GraphQL: " + e.getMessage())
                    .build();
        }
    }

}