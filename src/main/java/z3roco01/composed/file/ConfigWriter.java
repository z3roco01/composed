package z3roco01.composed.file;

import net.minecraft.block.Block;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import z3roco01.composed.Composed;
import z3roco01.composed.annotation.Comment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Handles the writing of a config file
 */
class ConfigWriter extends FileWriter {
    private final Object configObject;
    /**
     * Maps classes to a writer to simplify it
     */
    private static final HashMap<Class<?>, ClassWriter> classWriterMap = new HashMap<>();

    static {
        classWriterMap.put(boolean.class, (obj, field, writer)-> writer.writeBoolean((boolean)obj));
        classWriterMap.put(Boolean.class, (obj, field, writer)-> writer.writeBoolean((Boolean)obj));

        classWriterMap.put(byte.class, (obj, field, writer)-> writer.writeNumber((byte)obj));
        classWriterMap.put(Byte.class, (obj, field, writer)-> writer.writeNumber((Byte)obj));
        classWriterMap.put(short.class, (obj, field, writer)-> writer.writeNumber((short)obj));
        classWriterMap.put(Short.class, (obj, field, writer)-> writer.writeNumber((Short)obj));
        classWriterMap.put(int.class, (obj, field, writer)-> writer.writeNumber((int)obj));
        classWriterMap.put(Integer.class, (obj, field, writer)-> writer.writeNumber((Integer)obj));
        classWriterMap.put(long.class, (obj, field, writer)-> writer.writeNumber((long)obj));
        classWriterMap.put(Long.class, (obj, field, writer)-> writer.writeNumber((Long)obj));

        classWriterMap.put(float.class, (obj, field, writer)-> writer.writeDecimal((float)obj));
        classWriterMap.put(Float.class, (obj, field, writer)-> writer.writeDecimal((Float)obj));
        classWriterMap.put(double.class, (obj, field, writer)-> writer.writeDecimal((double)obj));
        classWriterMap.put(Double.class, (obj, field, writer)-> writer.writeDecimal((Double)obj));

        classWriterMap.put(String.class, (obj, field, writer)-> writer.writeString((String)obj));

        classWriterMap.put(Item.class, (obj, field, writer) -> writer.writeId(Registries.ITEM, (Item)obj));
        classWriterMap.put(Block.class, (obj, field, writer) -> writer.writeId(Registries.BLOCK, (Block)obj));
        classWriterMap.put(StatusEffect.class, (obj, field, writer) -> writer.writeId(Registries.STATUS_EFFECT, (StatusEffect)obj));

        classWriterMap.put(ArrayList.class, (obj, field, writer) -> {
            ArrayList<?> list = (ArrayList<?>)obj;
            writer.write("[\n");
            Type type = field.getGenericType();

            Class<?> elementClass;
            if(type instanceof ParameterizedType)
                elementClass = (Class<?>)((ParameterizedType)type).getActualTypeArguments()[0];
            else
                elementClass = list.getFirst().getClass();

            ClassWriter classWriter = classWriterMap.get(elementClass);

            if(!list.isEmpty()) {
                for(Object element : list)
                    classWriter.write(element, null, writer);
            }
            writer.write("]\n");
        });
    }

    public ConfigWriter(@NotNull File file, Object configObject) throws IOException {
        super(file);
        this.configObject = configObject;
    }

    /**
     * Append one field to the file
     * @param key the name of it, from the annotation
     * @param field the field to write its value
     */
    public void writeField(String key, Field field) throws IOException, IllegalAccessException {
        writeComment(field);
        writeKey(key);
        writeValue(field);
    }

    /**
     * Writes the actual value of a field
     */
    private void writeValue(Field field) throws IllegalAccessException, IOException {
        Object fieldObject = field.get(configObject);
        writeObject(fieldObject, field);
    }

    /**
     * Writes one objects value to the file
     * @param obj the object being written ( not the config object )
     * @param field the field that underlies the object
     */
    public void writeObject(Object obj, @Nullable Field field) throws IllegalAccessException, IOException {
        Class<?> objClass;
        if(field == null)
            objClass = obj.getClass();
        else
            objClass = field.getType();

        if(classWriterMap.containsKey(objClass))
            classWriterMap.get(objClass).write(obj, field, this);
    }

    /**
     * Performs lookup in the registry and gets the id of value, then writes it
     */
    public <T> void writeId(Registry<T> registry, T value) throws IOException {
        String id = registry.getId(value).toString();

        this.writeString(id);
    }

    /**
     * Writes a fields comment
     * @param field the field to write its comment
     */
    private void writeComment(Field field) throws IOException {
        String comment = getComment(field);
        // dont bother writing nothing
        if(comment.isBlank()) return;

        this.write("# " + getComment(field) + "\n");
    }

    /**
     * Writes the key of a field and its = sign
     * @param key the name to write
     */
    private void writeKey(String key) throws IOException {
        this.write(key + "=");
    }

    /**
     * Writes a boolean, assumes the field name is before
     * @param bool boolean to write
     */
    public void writeBoolean(boolean bool) throws IOException{
        if(bool)
            this.write("true\n");
        else
            this.write("false\n");
    }

    /**
     * Writes an integer, assuming field name is before
     * @param value integer to write
     */
    public void writeNumber(long value) throws IOException {
        this.write(value + "\n");
    }

    /**
     * Writes a float, assuming field name is before
     * @param value float to write
     */
    public void writeDecimal(double value) throws IOException {
        this.write(value + "\n");
    }

    /**
     * Writes a string, assuming field name is before
     * @param str string to write
     */
    public void writeString(String str) throws IOException {
        this.write("\"" + str + "\"\n");
    }

    /**
     * Gets the comment of the field if present
     * @param field the field to check
     * @return a comment if present, otherwise an empty string
     */
    private static String getComment(Field field) {
        if(!field.isAnnotationPresent(Comment.class)) return "";
        return field.getAnnotation(Comment.class).comment();
    }
}
