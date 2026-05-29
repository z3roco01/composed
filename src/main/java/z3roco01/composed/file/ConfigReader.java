package z3roco01.composed.file;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
        classReaderMap.put(boolean.class, (str, defaultValue, reader, line, field) -> Boolean.parseBoolean(str));
        classReaderMap.put(Boolean.class, (str, defaultValue, reader, line, field) -> Boolean.valueOf(str));

        classReaderMap.put(byte.class, (str, defaultValue, reader, line, field) -> Byte.parseByte(str));
        classReaderMap.put(Byte.class, (str, defaultValue, reader, line, field) -> Byte.valueOf(str));
        classReaderMap.put(short.class, (str, defaultValue, reader, line, field) -> Short.parseShort(str));
        classReaderMap.put(Short.class, (str, defaultValue, reader, line, field) -> Short.valueOf(str));
        classReaderMap.put(int.class, (str, defaultValue, reader, line, field) -> Integer.parseInt(str));
        classReaderMap.put(Integer.class, (str, defaultValue, reader, line, field) -> Integer.valueOf(str));
        classReaderMap.put(long.class, (str, defaultValue, reader, line, field) -> Long.parseLong(str));
        classReaderMap.put(Long.class, (str, defaultValue, reader, line, field) -> Long.valueOf(str));

        classReaderMap.put(float.class, (str, defaultValue, reader, line, field) -> Float.parseFloat(str));
        classReaderMap.put(Float.class, (str, defaultValue, reader, line, field) -> Float.valueOf(str));
        classReaderMap.put(double.class, (str, defaultValue, reader, line, field) -> Double.parseDouble(str));
        classReaderMap.put(Double.class, (str, defaultValue, reader, line, field) -> Double.valueOf(str));

        classReaderMap.put(String.class, (str, defaultValue, reader, line, field) -> ConfigReader.removeQuotes(str));

        classReaderMap.put(Item.class, (str, defaultValue, reader, line, field) -> ConfigReader.getFromRegistry(BuiltInRegistries.ITEM, removeQuotes(str)));
        classReaderMap.put(Block.class, (str, defaultValue, reader, line, field) -> ConfigReader.getFromRegistry(BuiltInRegistries.BLOCK, removeQuotes(str)));
        classReaderMap.put(MobEffect.class, (str, defaultValue, reader, line, field) -> ConfigReader.getFromRegistry(BuiltInRegistries.MOB_EFFECT, removeQuotes(str)));

        classReaderMap.put(ArrayList.class, (str, defaultValue, reader, line, field) -> {

            ArrayList<Object> list = new ArrayList<>();

            Class<?> elementClass;
            ArrayList<Object> arrList = (ArrayList<Object>)defaultValue;
            Type type = field.getGenericType();
            if(type instanceof ParameterizedType)
                elementClass = (Class<?>)((ParameterizedType)type).getActualTypeArguments()[0];
            else
                elementClass = list.getFirst().getClass();

            // clear out everything to start reading
            arrList.clear();
            // idx to start reading liens from
            int idx = reader.lines.indexOf(line)+1;
            String curLine = reader.lines.get(idx).trim();

            while(!curLine.startsWith("]")) {
                list.add(reader.fromString(null, elementClass, curLine, curLine, field));

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

        Object obj = fromString(field.get(configObject), field.getType(), strValue, line, field);
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
    private Object fromString(@Nullable Object defaultValue, Class<?> clazz, String str, String line, Field field) {
        if(classReaderMap.containsKey(clazz))
            return classReaderMap.get(clazz).read(str, defaultValue, this, line, field);
        else
            return null;
    }

    /**
     * Will lookup the string identifier in the passed registry
     */
    public static <T> T getFromRegistry(Registry<T> registry, String strId) {
        Identifier id = Identifier.parse(strId);

        return registry.getValue(id);
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
