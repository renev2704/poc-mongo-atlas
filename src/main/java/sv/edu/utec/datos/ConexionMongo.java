package sv.edu.utec.datos;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public final class ConexionMongo {

    private static final String NOMBRE_BD = "distrisalva";
    private static MongoClient cliente;

    private ConexionMongo() { }

    public static synchronized MongoDatabase getBaseDeDatos() {
        if (cliente == null) {
            String uri = System.getenv("MONGODB_URI");
            if (uri == null || uri.isBlank()) {
                throw new IllegalArgumentException(
                        "Falta la variable de entorno MONGODB_URI " +
                                "(Run > Edit Configurations).");
            }

            MongoClientSettings ajustes = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(uri))
                    .serverApi(ServerApi.builder().version(ServerApiVersion.V1).build())
                    .build();

            cliente = MongoClients.create(ajustes);
        }
        return cliente.getDatabase(NOMBRE_BD);
    }

    public static synchronized void cerrar() {
        if (cliente != null) {
            cliente.close();
            cliente = null;
        }
    }
}