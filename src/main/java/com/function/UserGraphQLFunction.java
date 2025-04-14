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
            // Cargar el esquema
            TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(
                    new InputStreamReader(
                            UserGraphQLFunction.class.getResourceAsStream("/schema.graphqls"),
                            StandardCharsets.UTF_8));

            // Wiring
            RuntimeWiring wiring = RuntimeWiring.newRuntimeWiring()
                    .type("Mutation", builder -> builder
                            .dataFetcher("updateUser", env -> {
                                int userId = env.getArgument("userId");
                                String username = env.getArgument("username");
                                String password = env.getArgument("password");
                                String email = env.getArgument("email");
                                Integer roleId = env.getArgument("roleId");

                                User user = new User();
                                user.setUserId(userId);
                                user.setUsername(username);
                                user.setPassword(password);
                                user.setEmail(email);
                                user.setRoleId(roleId);

                                boolean result = UserService.updateUser(user);
                                return result ? user : null; // Si se actualiza, retornamos el usuario actualizado
                            })
                            .dataFetcher("deleteUser", env -> {
                                int userId = env.getArgument("userId");
                                boolean result = UserService.deleteUser(userId);
                                return result;
                            }))
                    .build();

            SchemaGenerator schemaGenerator = new SchemaGenerator();
            GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeRegistry, wiring);
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

            GraphQL graphQL = GraphQLProvider.getGraphQL();
            ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                    .query(query)
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