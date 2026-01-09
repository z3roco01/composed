package z3roco01.composed.file;

import java.io.IOException;
import java.lang.reflect.Field;

@FunctionalInterface
interface ClassWriter {
    void write(Object obj, Field field, ConfigWriter writer) throws IOException;
}
