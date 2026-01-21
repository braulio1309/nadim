// Import necessary packages
import java.util.List;

// Class representing a Map object
public class Mapa {

    // Properties of the Map
    private List<String> keys; // List to hold keys
    private List<String> values; // List to hold values

    // Constructor initializing the Map
    public Mapa() {
        keys = new ArrayList<>(); // Initialize the keys list
        values = new ArrayList<>(); // Initialize the values list
    }

    // Method to add a key-value pair to the Map
    public void put(String key, String value) {
        keys.add(key); // Add key to the keys list
        values.add(value); // Add value to the values list
    }

    // Method to get a value by its key
    public String get(String key) {
        int index = keys.indexOf(key); // Find the index of the key
        if (index >= 0) {
            return values.get(index); // Return the corresponding value
        }
        return null; // Return null if key is not found
    }

    // Method to remove a key-value pair from the Map
    public void remove(String key) {
        int index = keys.indexOf(key); // Find the index of the key
        if (index >= 0) {
            keys.remove(index); // Remove key from the keys list
            values.remove(index); // Remove corresponding value from the values list
        }
    }

    // Method to get all the keys in the Map
    public List<String> getKeys() {
        return keys; // Return the list of keys
    }

    // Method to get all the values in the Map
    public List<String> getValues() {
        return values; // Return the list of values
    }
}