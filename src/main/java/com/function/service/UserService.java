package com.function.service;

import com.function.model.User;
import com.function.repository.UserRepository;
import java.sql.SQLException;
import java.util.List;

public class UserService {

    // Obtener todos los usuarios
    public static List<User> getAllUsers() throws SQLException {
        return UserRepository.getAllUsers();
    }

    // Obtener un usuario por ID
    public static User getUserById(int userId) throws SQLException {
        return UserRepository.getUserById(userId);
    }

    // Crear un nuevo usuario
    public static User createUser(User user) throws SQLException {
        return UserRepository.createUser(user);
    }

    // Actualizar un usuario existente
    public static User updateUser(User user) throws SQLException {
        boolean success = UserRepository.updateUser(user);
        return success ? user : null;
    }

    // Eliminar un usuario por ID
    public static boolean deleteUser(int userId) throws SQLException {
        return UserRepository.deleteUser(userId);
    }
}
