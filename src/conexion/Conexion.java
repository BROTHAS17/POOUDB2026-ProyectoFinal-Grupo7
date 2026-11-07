package conexion;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Gestión de la conexión a MySQL usando db.properties.
 */
public class Conexion {

    public static Connection getConexion() throws SQLException {
        Properties properties = new Properties();
        try (InputStream input = Conexion.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                throw new SQLException("No se encontró el archivo db.properties en el classpath.");
            }
            properties.load(input);
        } catch (Exception e) {
            throw new SQLException("Error al leer db.properties: " + e.getMessage(), e);
        }

        String url = properties.getProperty("db.url");
        String user = properties.getProperty("db.user");
        String password = properties.getProperty("db.password");

        return DriverManager.getConnection(url, user, password);
    }
}
