// Importa los paquetes necesarios
import java.util.List;
import java.util.ArrayList;

// Clase que representa un objeto Mapa
public class Mapa {

    // Propiedades del Mapa
    private List<String> keys; // Lista que almacena las llaves
    private List<String> values; // Lista que almacena los valores

    // Constructor que inicializa el Mapa
    public Mapa() {
        keys = new ArrayList<>(); // Inicializa la lista de llaves
        values = new ArrayList<>(); // Inicializa la lista de valores
    }

    // Método para agregar un par llave-valor al Mapa
    public void put(String key, String value) {
        keys.add(key); // Agrega la llave a la lista de llaves
        values.add(value); // Agrega el valor a la lista de valores
    }

    // Método para obtener un valor basado en su llave
    public String get(String key) {
        int index = keys.indexOf(key); // Encuentra el índice de la llave
        if (index >= 0) {
            return values.get(index); // Retorna el valor correspondiente
        }
        return null; // Retorna null si la llave no se encuentra
    }

    // Método para eliminar un par llave-valor del Mapa
    public void remove(String key) {
        int index = keys.indexOf(key); // Encuentra el índice de la llave
        if (index >= 0) {
            keys.remove(index); // Elimina la llave de la lista de llaves
            values.remove(index); // Elimina el valor correspondiente de la lista de valores
        }
    }

    // Método para obtener todas las llaves del Mapa
    public List<String> getKeys() {
        return keys; // Retorna la lista de llaves
    }

    // Método para obtener todos los valores del Mapa
    public List<String> getValues() {
        return values; // Retorna la lista de valores
    }
}