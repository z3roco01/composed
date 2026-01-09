package z3roco01.composed.file;

import z3roco01.composed.ProcessedConfig;
import z3roco01.composed.annotation.ConfigProperty;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

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
        }

        writer.close();
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

            // keeps track if any defaults need to be saved
            boolean configUpdated = true;
            ConfigReader reader = new ConfigReader(file, object);
            for(Field field : object.getClass().getDeclaredFields()) {
                // if it is a record then every field will be serialised
                if(!shouldSerialise(field))
                    continue;

                String key = getKey(field);

                configUpdated &= reader.readField(key, field);
            }

            // if the config class has fields that dont exist, add them up
            if(!configUpdated)
                store(file, object);

            // do processing if needed
            if(object instanceof ProcessedConfig)
                ((ProcessedConfig) object).process();
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
}