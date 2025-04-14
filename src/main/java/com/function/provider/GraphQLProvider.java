package com.function.provider;

import com.function.model.User;
import com.function.service.UserService;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.*;

import java.io.InputStreamReader;
import java.util.Map;

public class GraphQLProvider {
    private static GraphQL graphQL;

    static {
        try {
            TypeDefinitionRegistry typeRegistry = new SchemaParser()
                    .parse(new InputStreamReader(
                            GraphQLProvider.class.getResourceAsStream("/schema.graphqls")
                    ));

            RuntimeWiring wiring = buildWiring();
            GraphQLSchema schema = new SchemaGenerator().makeExecutableSchema(typeRegistry, wiring);
            graphQL = GraphQL.newGraphQL(schema).build();
        } catch (Exception e) {
            throw new RuntimeException("Error inicializando GraphQL", e);
        }
    }

    private static RuntimeWiring buildWiring() {
        return RuntimeWiring.newRuntimeWiring()
                .type("Mutation", typeWiring -> typeWiring
                        .dataFetcher("updateUser", env -> {
                            int userId = env.getArgument("userId");
                            User user = UserService.getUserById(userId);
                            if (user == null) return false;

                            Map<String, Object> args = env.getArguments();
                            if (args.containsKey("username")) user.setUsername((String) args.get("username"));
                            if (args.containsKey("password")) user.setPassword((String) args.get("password"));
                            if (args.containsKey("email")) user.setEmail((String) args.get("email"));
                            if (args.containsKey("roleId")) user.setRoleId((Integer) args.get("roleId"));

                            return UserService.updateUser(user);
                        })
                        .dataFetcher("deleteUser", env -> {
                            int userId = env.getArgument("userId");
                            return UserService.deleteUser(userId);
                        })
                )
                .build();
    }

    public static GraphQL getGraphQL() {
        return graphQL;
    }
}