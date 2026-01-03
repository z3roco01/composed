package z3roco01.composed;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Handles reading of config files
 */
class ConfigReader extends FileReader {
    private final Object configObject;
    private final ArrayList<String> lines = new ArrayList<>();

    public ConfigReader(@NotNull File file, Object configObject) throws IOException {
        super(file);
        this.configObject = configObject;
        BufferedReader reader = new BufferedReader(this);
        for(String line; (line = reader.readLine()) != null;) {
            // ignore comments
            if(line.startsWith("#"))
                continue;

            lines.add(line);
        }
    }

    /**
     * Reads one field from the file
     * @param key the key of the field
     * @param field the field to set
     * @return could the field be read
     */
    public boolean readField(String key, Field field) throws IOException, IllegalAccessException {
        String line = findKey(key);
        if(line == null) return false;

        String[] splitLine = line.split("=", 2);
        if(splitLine.length != 2) return false;

        String strValue = splitLine[1];

        Class fieldClass = field.getType();
        Object fieldObject = field.get(configObject);
        Object newValue = new Object();

        // boolean
        if(fieldClass == boolean.class)
            newValue = Boolean.parseBoolean(strValue);
        else if(fieldClass == Boolean.class)
            newValue = Boolean.valueOf(strValue);
        // whole numbers
        else if(fieldClass == byte.class)
            newValue = Byte.parseByte(strValue);
        else if(fieldClass == Byte.class)
            newValue = Byte.valueOf(strValue);
        else if(fieldClass == short.class)
            newValue = Short.parseShort(strValue);
        else if(fieldClass == Short.class)
            newValue = Short.valueOf(strValue);
        else if(fieldClass == int.class)
            newValue = Integer.parseInt(strValue);
        else if(fieldClass == Integer.class)
            newValue = Integer.valueOf(strValue);
        else if(fieldClass == long.class)
            newValue = Long.parseLong(strValue);
        else if(fieldClass == Long.class)
            newValue = Long.valueOf(strValue);
            // decimal numbers
        else if(fieldClass == float.class)
            newValue = Float.parseFloat(strValue);
        else if(fieldClass == Float.class)
            newValue = Float.valueOf(strValue);
        else if(fieldClass == double.class)
            newValue = Double.parseDouble(strValue);
        else if(fieldClass == Double.class)
            newValue = Double.valueOf(strValue);
            // string
        else if(fieldClass == String.class) {
            String str = (String)fieldObject;

            if(!str.startsWith("\"") || !str.endsWith("\""))
                return false;

            newValue = str.substring(1, str.length()-2);
        }

        boolean accessible = field.isAccessible();
        field.setAccessible(true);

        field.set(configObject, newValue);

        field.setAccessible(accessible);

        return true;
    }

    /**
     * Finds the line that contains the key
     * @param key the key to search for
     * @return the whole line containing the key, including the key
     */
    private String findKey(String key) throws IOException {
        for(String line : lines) {
            if(line.startsWith(key))
                return line;
        }

        return null;
    }
}
