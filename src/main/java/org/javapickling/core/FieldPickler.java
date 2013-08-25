package org.javapickling.core;

import java.io.IOException;

public interface FieldPickler<PF> {

    <T> void field(String name, T value, Pickler<T, PF> pickler) throws IOException;
    <T> void field(Field<T, PF> field, T value) throws IOException;

    void boolean_f(String name, Boolean value) throws IOException;
    void byte_f(String name, Byte value) throws IOException;
    void char_f(String name, Character value) throws IOException;
    void short_f(String name, Short value) throws IOException;
    void long_f(String name, Long value) throws IOException;
    void integer_f(String name, Integer value) throws IOException;
    void float_f(String name, Float value) throws IOException;
    void double_f(String name, Double value) throws IOException;
    <T extends Enum<T>> void enum_f(String name, T value, Class<T> clazz) throws IOException;
    void string_f(String name, String value) throws IOException;

    PF pickle(PF pf) throws IOException;
}
