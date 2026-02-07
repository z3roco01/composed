package z3roco01.composed.file;

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
import java.util.HashMap;

/**
 * Handles reading of config files
 */
class ConfigReader extends FileReader {
    private final Object configObject;
    public final ArrayList<String> lines = new ArrayList<>();

    private static final HashMap<Class<?>, ClassReader> classReaderMap = new HashMap<>();

    static {
        classReaderMap.put(boolean.class, (str, defaultValue, reader, line) -> Boolean.parseBoolean(str));
        classReaderMap.put(Boolean.class, (str, defaultValue, reader, line) -> Boolean.valueOf(str));

        classReaderMap.put(byte.class, (str, defaultValue, reader, line) -> Byte.parseByte(str));
        classReaderMap.put(Byte.class, (str, defaultValue, reader, line) -> Byte.valueOf(str));
        classReaderMap.put(short.class, (str, defaultValue, reader, line) -> Short.parseShort(str));
        classReaderMap.put(Short.class, (str, defaultValue, reader, line) -> Short.valueOf(str));
        classReaderMap.put(int.class, (str, defaultValue, reader, line) -> Integer.parseInt(str));
        classReaderMap.put(Integer.class, (str, defaultValue, reader, line) -> Integer.valueOf(str));
        classReaderMap.put(long.class, (str, defaultValue, reader, line) -> Long.parseLong(str));
        classReaderMap.put(Long.class, (str, defaultValue, reader, line) -> Long.valueOf(str));

        classReaderMap.put(float.class, (str, defaultValue, reader, line) -> Float.parseFloat(str));
        classReaderMap.put(Float.class, (str, defaultValue, reader, line) -> Float.valueOf(str));
        classReaderMap.put(double.class, (str, defaultValue, reader, line) -> Double.parseDouble(str));
        classReaderMap.put(Double.class, (str, defaultValue, reader, line) -> Double.valueOf(str));

        classReaderMap.put(String.class, (str, defaultValue, reader, line) -> ConfigReader.removeQuotes(str));

        classReaderMap.put(Item.class, (str, defaultValue, reader, line) -> ConfigReader.getFromRegistry(Registries.ITEM, removeQuotes(str)));
        classReaderMap.put(Block.class, (str, defaultValue, reader, line) -> ConfigReader.getFromRegistry(Registries.BLOCK, removeQuotes(str)));
        classReaderMap.put(StatusEffect.class, (str, defaultValue, reader, line) -> ConfigReader.getFromRegistry(Registries.STATUS_EFFECT, removeQuotes(str)));

        classReaderMap.put(ArrayList.class, (str, defaultValue, reader, line) -> {

            ArrayList<Object> list = new ArrayList<>();

            Class<?> elementClass;
            ArrayList<Object> arrList = (ArrayList<Object>)defaultValue;
            if(!arrList.isEmpty())
                elementClass = arrList.getFirst().getClass();
            else  // must have at least one element by default
                return null;

            // clear out everything to start reading
            arrList.clear();
            // idx to start reading liens from
            int idx = reader.lines.indexOf(line)+1;
            String curLine = reader.lines.get(idx).trim();

            while(!curLine.startsWith("]")) {
                list.add(reader.fromString(null, elementClass, curLine, curLine));

                idx++;
                curLine = reader.lines.get(idx);
            }

            return list;
        });
    }

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
    public boolean readField(String key, Field field) throws IllegalAccessException {
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
    private Object fromString(@Nullable Object defaultValue, Class<?> clazz, String str, String line) {
        if(classReaderMap.containsKey(clazz))
            return classReaderMap.get(clazz).read(str, defaultValue, this, line);
        else
            return null;
    }

    /**
     * Will lookup the string identifier in the passed registry
     */
    public static <T> T getFromRegistry(Registry<T> registry, String strId) {
        Identifier id = Identifier.of(strId);

        return registry.get(id);
    }

    /**
     * Removes the first and last double quote from a string
     */
    public static String removeQuotes(String str) {
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
    private String findKey(String key) {
        for(String line : lines) {
            if(line.startsWith(key))
                return line;
        }

        return null;
    }
}
