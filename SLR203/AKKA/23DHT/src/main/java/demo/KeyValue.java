package demo;

public class KeyValue {
    private final String key;
    private final String value;

    // Constructor
    public KeyValue(String key, String value) {
        this.key = key;
        this.value = value;
    }

    // Getter for key
    public String getKey() {
        return key;
    }

    // Getter for value
    public String getValue() {
        return value;
    }

    // Override equals and hashCode for proper comparison and usage in collections
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KeyValue keyValue = (KeyValue) o;

        if (!key.equals(keyValue.key)) return false;
        return value.equals(keyValue.value);
    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }

    // Optional: toString() method for debugging
    @Override
    public String toString() {
        return "KeyValue{" +
               "key='" + key + '\'' +
               ", value='" + value + '\'' +
               '}';
    }
}
