package z3roco01.composed;

import net.minecraft.block.Block;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import z3roco01.composed.annotation.Comment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Handles the writing of a config file
 */
class ConfigWriter extends FileWriter {
    private final Object configObject;

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
        Class fieldClass = field.getType();
        Object fieldObject = field.get(configObject);
        writeObject(fieldObject, field);
    }

    /**
     * Writes one objects value to the file
     * @param field the field that underlies the object
     */
    private void writeObject(Object obj, @Nullable Field field) throws IllegalAccessException, IOException {
        Class<?> objClass;
        if(field == null)
            objClass = obj.getClass();
        else
            objClass = field.getType();

        // boolean
        if(objClass == boolean.class)
            this.writeBoolean(field.getBoolean(configObject));
        else if(objClass == Boolean.class)
            this.writeBoolean((Boolean)obj);
            // whole numbers
        else if(objClass == byte.class)
            this.writeLong(field.getByte(obj));
        else if(objClass == Byte.class)
            this.writeLong((Byte)obj);
        else if(objClass == short.class)
            this.writeLong(field.getShort(obj));
        else if(objClass == Short.class)
            this.writeLong((Short)obj);
        else if(objClass == int.class)
            this.writeLong(field.getInt(configObject));
        else if(objClass == Integer.class)
            this.writeLong((Integer)obj);
        else if(objClass == long.class)
            this.writeLong(field.getLong(configObject));
        else if(objClass == Long.class)
            this.writeLong((Long)obj);
            // decimal numbers
        else if(objClass == float.class)
            this.writeDouble(field.getFloat(configObject));
        else if(objClass == Float.class)
            this.writeDouble((Float)obj);
        else if(objClass == double.class)
            this.writeDouble(field.getDouble(configObject));
        else if(objClass == Double.class)
            this.writeDouble((Double) obj);
            // string
        else if(objClass == String.class)
            this.writeString((String)obj);
        else if(objClass == Item.class)
            this.writeId(Registries.ITEM, (Item)obj);
        else if(objClass == Block.class)
            this.writeId(Registries.BLOCK, (Block)obj);
        else if(objClass == StatusEffect.class)
            this.writeId(Registries.STATUS_EFFECT, (StatusEffect)obj);
        // lists
        else if(ArrayList.class.isAssignableFrom(objClass)) {
            ArrayList<?> list = (ArrayList<?>)obj;
            this.write("[\n");

            if(!list.isEmpty()) {
                Class elementClass = list.get(0).getClass();

                for(Object element : list) {
                    writeObject(element, null);
                }
            }
            this.write("]\n");

        }
    }

    /**
     * Performs lookup in the registry and gets the id of value, then writes it
     */
    private <T> void writeId(Registry<T> registry, T value) throws IOException {
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
    private void writeBoolean(boolean bool) throws IOException{
        if(bool)
            this.write("true\n");
        else
            this.write("false\n");
    }

    /**
     * Writes an integer, assuming field name is before
     * @param value integer to write
     */
    private void writeLong(long value) throws IOException {
        this.write(value + "\n");
    }

    /**
     * Writes a float, assuming field name is before
     * @param value float to write
     */
    private void writeDouble(double value) throws IOException {
        this.write(value + "\n");
    }

    /**
     * Writes a string, assuming field name is before
     * @param str string to write
     */
    private void writeString(String str) throws IOException {
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
