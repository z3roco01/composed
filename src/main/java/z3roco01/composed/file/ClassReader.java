package z3roco01.composed.file;

import org.jspecify.annotations.Nullable;

@FunctionalInterface
interface ClassReader {
    /**
     * @param str the string representing the value
     * @param defaultValue the default value of the field
     * @param reader the ConfigReader
     * @param line the whole line being currently parsed
     */
    Object read(String str, @Nullable Object defaultValue, ConfigReader reader, String line);
}
