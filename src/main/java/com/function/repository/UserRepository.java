package com.function.repository;

import com.function.OracleDBConnection;
import com.function.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    private static final String TABLE_NAME = "USERS_DC2";
    
    public static List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        try (Connection conn = OracleDBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + TABLE_NAME)) {
            
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }
        return users;
    }
    
    public static User getUserById(int userId) throws SQLException {
        try (Connection conn = OracleDBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT * FROM " + TABLE_NAME + " WHERE USER_ID = ?")) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
            return null;
        }
    }
    
    public static User createUser(User user) throws SQLException {
        try (Connection conn = OracleDBConnection.getConnection();
            
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO " + TABLE_NAME + " (USERNAME, PASSWORD, EMAIL, ROLE_ID) VALUES (?, ?, ?, ?)",
                 Statement.RETURN_GENERATED_KEYS)) {
                    System.out.println("Ejecutando SQL: INSERT INTO USERS_DC2 (USERNAME, PASSWORD, EMAIL, ROLE_ID) VALUES (" 
    + user.getUsername() + ", " + user.getPassword() + ", " + user.getEmail() + ", " + user.getRoleId() + ")");

                    System.out.println("Conectado a la base de datos");
                    System.out.println("Datos que se insertarán:");
                    System.out.println("Username: " + user.getUsername());
                    System.out.println("Password: " + user.getPassword());
                    System.out.println("Email: " + user.getEmail());
                    System.out.println("Role ID: " + user.getRoleId());
                                
            
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getEmail());
            stmt.setObject(4, user.getRoleId(), Types.INTEGER);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (PreparedStatement selectStmt = conn.prepareStatement(
                        "SELECT USER_ID FROM USERS_DC2 WHERE EMAIL = ? ORDER BY USER_ID DESC FETCH FIRST 1 ROW ONLY")) {
                    selectStmt.setString(1, user.getEmail());
                    try (ResultSet rs = selectStmt.executeQuery()) {
                        if (rs.next()) {
                            user.setUserId(rs.getInt("USER_ID"));
                        }
                    }
                }
                return user;
            }
            throw new SQLException("No se pudo crear el usuario");
        }
    }
    
    public static boolean updateUser(User user) throws SQLException {
        String sql = "UPDATE " + TABLE_NAME + " SET USERNAME = ?, PASSWORD = ?, EMAIL = ?, ROLE_ID = ? WHERE USER_ID = ?";

        try (Connection conn = OracleDBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            System.out.println("Ejecutando SQL: " + sql);
            System.out.println("Datos para actualizar:");
            System.out.println("Username: " + user.getUsername());
            System.out.println("Password: " + user.getPassword());
            System.out.println("Email: " + user.getEmail());
            System.out.println("Role ID: " + user.getRoleId());
            System.out.println("User ID: " + user.getUserId());

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getEmail());
            stmt.setObject(4, user.getRoleId(), Types.INTEGER);
            stmt.setInt(5, user.getUserId());

            int rowsAffected = stmt.executeUpdate();
            System.out.println("Filas actualizadas: " + rowsAffected);

            return rowsAffected > 0; // Devuelve true si se actualizó al menos 1 fila
        }
    }
    
    public static boolean deleteUser(int userId) throws SQLException {
        try (Connection conn = OracleDBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "DELETE FROM " + TABLE_NAME + " WHERE USER_ID = ?")) {
            
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        }
    }
    
    private static User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("USER_ID"));
        user.setUsername(rs.getString("USERNAME"));
        user.setPassword(rs.getString("PASSWORD"));
        user.setEmail(rs.getString("EMAIL"));
        user.setRoleId(rs.getObject("ROLE_ID") != null ? rs.getInt("ROLE_ID") : null);
        user.setCreatedAt(rs.getTimestamp("CREATED_AT"));
        return user;
    }
}