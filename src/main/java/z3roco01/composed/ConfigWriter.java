package z3roco01.composed;

import org.jetbrains.annotations.NotNull;
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

        Class fieldClass = field.getType();
        Object fieldObject = field.get(configObject);

        // boolean
        if(fieldClass == boolean.class)
            this.writeBoolean(field.getBoolean(configObject));
        else if(fieldClass == Boolean.class)
            this.writeBoolean((Boolean)fieldObject);
        // whole numbers
        else if(fieldClass == byte.class)
            this.writeLong(field.getByte(fieldObject));
        else if(fieldClass == Byte.class)
            this.writeLong((Byte)fieldObject);
        else if(fieldClass == short.class)
            this.writeLong(field.getShort(fieldObject));
        else if(fieldClass == Short.class)
            this.writeLong((Short)fieldObject);
        else if(fieldClass == int.class)
            this.writeLong(field.getInt(configObject));
        else if(fieldClass == Integer.class)
            this.writeLong((Integer)fieldObject);
        else if(fieldClass == long.class)
            this.writeLong(field.getLong(configObject));
        else if(fieldClass == Long.class)
            this.writeLong((Long)fieldObject);
        // decimal numbers
        else if(fieldClass == float.class)
            this.writeDouble(field.getFloat(configObject));
        else if(fieldClass == Float.class)
            this.writeDouble((Float)fieldObject);
        else if(fieldClass == double.class)
            this.writeDouble(field.getDouble(configObject));
        else if(fieldClass == Double.class)
            this.writeDouble((Double) fieldObject);
        // string
        else if(fieldClass == String.class)
            this.writeString((String)fieldObject);
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
