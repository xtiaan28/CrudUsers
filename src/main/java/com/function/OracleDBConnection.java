package com.function;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;
public class OracleDBConnection {
    private static final Logger logger = Logger.getLogger(OracleDBConnection.class.getName());

    public static boolean testConnection(){
        try(Connection conn = getConnection()){
            boolean isValid = conn.isValid(5);
            logger.info("Conexión a Oracle probada: " +(isValid ? "Valida" : "Invalida"));
            return isValid;
        }catch(SQLException e){
            logger.severe("Error al probar la conexión: "+e.getMessage());
            return false;
        }
    }

    public static Connection getConnection() throws SQLException{
        String tnsName = System.getenv("ORACLE_TNS_NAME");
        String user = System.getenv("ORACLE_USER");
        String password = System.getenv("ORACLE_PASSWORD");
        String walletPath = System.getenv("ORACLE_WALLET_PATH");
        System.out.println("ORACLE_TNS_NAME: " + System.getenv("ORACLE_TNS_NAME"));
        System.out.println("ORACLE_USER: " + System.getenv("ORACLE_USER"));
        System.out.println("ORACLE_PASSWORD: " + (System.getenv("ORACLE_PASSWORD") != null ? "********" : "null"));
        System.out.println("ORACLE_WALLET_PATH: " + System.getenv("ORACLE_WALLET_PATH"));

        String url = "jdbc:oracle:thin:@" + tnsName + "?TNS_ADMIN=" + walletPath;

        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", password);
        props.setProperty("oracle.net.ssl_version", "1.2");
        props.setProperty("oracle.net.wallet_location", "(SOURCE=(METHOD=file)(METHOD_DATA=(DIRECTORY="+walletPath+")))");
        
        logger.info("Intentando conectar a Oracle");
        logger.info("TNS: "+tnsName);
        logger.info("Usuario: "+user);
        logger.info("Ruta Wallet: "+ walletPath);

        try{
            Connection conn = DriverManager.getConnection(url,props);
            logger.info("Conexión exitosa a Oracle");
            return conn;
        }catch(SQLException e){
            logger.severe("Error al conectar "+e.getMessage());
            throw e;
        }
    }
}