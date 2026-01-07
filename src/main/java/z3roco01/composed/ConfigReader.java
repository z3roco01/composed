package z3roco01.composed;

import net.minecraft.block.Block;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

        Object obj = fromString(field.get(configObject), field.getType(), strValue, line);
        if(obj == null) return false;

        boolean accessible = field.canAccess(configObject);
        field.setAccessible(true);

        field.set(configObject, obj);

        field.setAccessible(accessible);

        return true;
    }

    /**
     * Creates the correct object from a string
     * @param defaultValue the default value for this property
     * @param clazz the class of this object
     * @param str the string to convert
     * @param line the line which is being converted
     * @return the object that was created
     */
    private Object fromString(@Nullable Object defaultValue, Class<?> clazz, String str, String line) throws IllegalAccessException {
        if(clazz == boolean.class)
            return Boolean.parseBoolean(str);
        else if(clazz == Boolean.class)
            return Boolean.valueOf(str);
        else if(clazz == byte.class)
            return Byte.parseByte(str);
        else if(clazz == Byte.class)
            return Byte.valueOf(str);
        else if(clazz == short.class)
            return Short.parseShort(str);
        else if(clazz == Short.class)
            return Short.valueOf(str);
        else if(clazz == int.class)
            return Integer.parseInt(str);
        else if(clazz == Integer.class)
            return Integer.valueOf(str);
        else if(clazz == long.class)
            return Long.parseLong(str);
        else if(clazz == Long.class)
            return Long.valueOf(str);
            // decimal numbers
        else if(clazz == float.class)
            return Float.parseFloat(str);
        else if(clazz == Float.class)
            return Float.valueOf(str);
        else if(clazz == double.class)
            return Double.parseDouble(str);
        else if(clazz == Double.class)
            return Double.valueOf(str);
            // string
        else if(clazz == String.class)
            return removeQuotes(str);
        else if(clazz == Item.class)
            return getFromRegistry(Registries.ITEM, removeQuotes(str));
        else if(clazz == Block.class)
            return getFromRegistry(Registries.BLOCK, removeQuotes(str));
        else if(clazz == StatusEffect.class)
            return getFromRegistry(Registries.STATUS_EFFECT, removeQuotes(str));
        else if(ArrayList.class.isAssignableFrom(clazz)) {
            ArrayList<Object> list = new ArrayList<>();

            Class elementClass;
            if(!((ArrayList<Object>)defaultValue).isEmpty())
                elementClass = ((ArrayList<Object>)defaultValue).get(0).getClass();
            else  // must have at least one element by default
                return null;

            // clear out everything to start reading
            ((ArrayList<Object>)defaultValue).clear();
            // idx to start reading liens from
            int idx = lines.indexOf(line)+1;
            String curLine = lines.get(idx).trim();

            while(!curLine.startsWith("]")) {
                list.add(fromString(null, elementClass, curLine, curLine));

                idx++;
                curLine = lines.get(idx);
            }

            return list;
        }

        return null;
    }

    /**
     * Will lookup the string identifier in the passed registry
     */
    private <T> T getFromRegistry(Registry<T> registry, String strId) {
        Identifier id = Identifier.of(strId);

        return registry.get(id);
    }

    /**
     * Removes the first and last double quote from a string
     */
    private String removeQuotes(String str) {
        int firstQuote = str.indexOf("\"");

        int lastQuote = str.lastIndexOf("\"");
        if(firstQuote == -1 || lastQuote == -1 || firstQuote == lastQuote)
            return null;

        return str.substring(firstQuote+1, lastQuote);
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
