package z3roco01.composed;

import z3roco01.composed.annotation.Comment;
import z3roco01.composed.annotation.ConfigProperty;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Properties;

/**
 * Holds methods which handle reading and writing of ConfigProperties
 */
public class ConfigFile {
    /**
     * Saves an object's marked config properties to a passed path
     * @param path the path of the file to save to, creates file if needed
     * @param object the object to save
     * @param <T> the type of the object
     */
    public static <T> void store(String path, T object) throws IOException, IllegalAccessException {
        File file = new File(path);
        store(file, object);
    }

    /**
     * Saves an object's marked config properties to a passed path
     * @param file the file to save to, creates file if needed
     * @param object the object to save
     * @param <T> the type of the object
     */
    public static <T> void store(File file, T object) throws IOException, IllegalAccessException {
        createIfNotPresent(file);

        // TODO: MAKE BETTER CONFIG SYSTEM THAT DOESNT RELY ON PROPERTIES
        // underlying properties objects, mostly for easily text handling
        //Properties properties = new Properties();

        ConfigWriter writer = new ConfigWriter(file, object);

        // first loop over all properties and filter for ones annotated as config
        for(Field field : object.getClass().getDeclaredFields()) {
            if(!shouldSerialise(field))
                continue;

            String key = getKey(field);

            boolean accessible = field.canAccess(object);
            // make it accessible temporarily
            field.setAccessible(true);
            writer.writeField(key, field);
            // return accessibility
            field.setAccessible(accessible);
            //properties.setProperty(key, value);
        }

        writer.close();

        // use the properties object to write
        //properties.store(new FileWriter(file), null);
    }

    /**
     * loads an object's marked config properties from a file
     * @param path the path of the file to save to, creates file and stores defaults if needed
     * @param object the object to save
     * @param <T> the type of the object
     */
    public static <T> void load(String path, T object) throws IOException, IllegalAccessException {
        File file = new File(path);
        load(file, object);
    }

    /**
     * loads an object's marked config properties from a file
     * @param file the file to save to, creates file and stores defaults if needed
     * @param object the object to save
     * @param <T> the type of the object
     */
    public static <T> void load(File file, T object) throws IOException, IllegalAccessException {
        // if the file did not need creation
        if(!createIfNotPresent(file)) {
            //Properties properties = new Properties();
            //properties.load(new FileReader(file));
            // if the file is missing at least one property, this will be true meaning a save is needed
            //boolean propertiesUpdated = false;

            // keeps track if any defaults need to be saved
            boolean configUpdated = false;
            ConfigReader reader = new ConfigReader(file, object);
            for(Field field : object.getClass().getDeclaredFields()) {
                // if it is a record then every field will be serialised
                if(!shouldSerialise(field))
                    continue;

                String key = getKey(field);

                configUpdated |= reader.readField(key, field);
            }

            // if the config class has fields that dont exist, add them up
            if(configUpdated)
                store(file, object);
        }else {
            // if it did, save defaults and thats it
            store(file, object);
        }
    }

    /**
     * Creates a file if it does not exist
     * @param file the file to maybe create
     */
    private static boolean createIfNotPresent(File file) throws IOException {
        // create any parent directories if needed
        file.toPath().getParent().toFile().mkdirs();

        if(!file.exists())
            return file.createNewFile();
        return false;
    }

    /**
     * Gets the key from a field annotated with the ConfigProperty annotation
     * @param field the field, must be annotated
     * @return the key if specified, or the field's name
     */
    private static String getKey(Field field) {
        if(isConfigProperty(field)) {
            String key = field.getAnnotation(ConfigProperty.class).key();
            // if no key was specified ( or it is intentionally blank ) then set it to the fields name
            if(key.isBlank())
                key = field.getName();

            return key;
        }else {
            // its apart of a record and does not have an annotation, so return its name
            return field.getName();
        }
    }

    /**
     * Returns if a field should be serialised, based on if it is annotated, or in a record
     * @param field the field to check
     * @return true if it should be, false otherwise
     */
    private static boolean shouldSerialise(Field field) {
        return isConfigProperty(field)/* || field.getDeclaringClass().isRecord()*/;
    }

    /**
     * Returns if the field has the ConfigProperty annotation
     * @param field the field to check
     * @return true when it is present
     */
    private static boolean isConfigProperty(Field field) {
        return field.isAnnotationPresent(ConfigProperty.class);
    }

    private static String getComment(Field field) {
        if(!field.isAnnotationPresent(Comment.class)) return "";
        return field.getAnnotation(Comment.class).comment();
    }
}