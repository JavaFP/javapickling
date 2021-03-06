package org.javafp.javapickling.core;

import com.google.common.collect.Maps;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A class which describes types.
 */
public class MetaType {

    private static final String ARRAY_SUFFIX = "[]";

    public static <PT> Class<?> primToObjClass(Class<PT> clazz) {
        if (clazz.equals(boolean.class)) {
            return Boolean.class;
        } else if (clazz.equals(byte.class)) {
            return Byte.class;
        } else if (clazz.equals(char.class)) {
            return Character.class;
        } else if (clazz.equals(double.class)) {
            return Double.class;
        } else if (clazz.equals(float.class)) {
            return Float.class;
        } else if (clazz.equals(int.class)) {
            return Integer.class;
        } else if (clazz.equals(long.class)) {
            return Long.class;
        } else if (clazz.equals(short.class)) {
            return Short.class;
        } else {
            return null;
        }
    }

    public static <OT> Class<?> objToPrimClass(Class<OT> clazz) {
        if (clazz.equals(Boolean.class)) {
            return boolean.class;
        } else if (clazz.equals(Byte.class)) {
            return byte.class;
        } else if (clazz.equals(Character.class)) {
            return char.class;
        } else if (clazz.equals(Double.class)) {
            return double.class;
        } else if (clazz.equals(Float.class)) {
            return float.class;
        } else if (clazz.equals(Integer.class)) {
            return int.class;
        } else if (clazz.equals(Long.class)) {
            return long.class;
        } else if (clazz.equals(Short.class)) {
            return short.class;
        } else {
            return null;
        }
    }

    /**
     * An enum for what we consider to be the base types.
     */
    public enum TypeKind {
        NULL(Object.class),
        ARRAY(Array.class),
        BOOLEAN(Boolean.class),
        BYTE(Byte.class),
        CHAR(Character.class),
        CLASS(Class.class),
        DOUBLE(Double.class),
        ENUM(String.class),
        FLOAT(Float.class),
        INT(Integer.class),
        LIST(List.class),
        LONG(Long.class),
        MAP(Map.class),
        OBJECT(Object.class),
        SET(Set.class),
        SHORT(Short.class),
        STRING(String.class);

        public final Class<?> clazz;

        TypeKind(Class<?> clazz) {
            this.clazz = clazz;
        }

        /**
         * Construct a pickler for the type described by this TypeKind.
         * @param core PicklerCore implementation.
         * @param clazz optional Class object.
         * @param <PF>
         * @return
         */
        public <PF> Pickler<?, PF> pickler(PicklerCore<PF> core, Class<?> clazz) {
            switch (this) {
                case NULL:      return core.null_p();
                case BOOLEAN:   return core.boolean_p();
                case BYTE:      return core.byte_p();
                case CHAR:      return core.char_p();
                case CLASS:     return core.class_p();
                case DOUBLE:    return core.double_p();
                case ENUM:
                    return core.enum_p(MetaType.<TypeKind>castEnumClass(clazz));
                case FLOAT:     return core.float_p();
                case INT:       return core.integer_p();
                case LIST:      return core.list_p(core.d_object_p(), (Class<List<Object>>)clazz);
                case LONG:      return core.long_p();
                case MAP:       return core.map_p(core.d_object_p(), core.d_object_p(), (Class<Map<Object, Object>>)clazz);
                case OBJECT:    return core.object_p(clazz);
                case SET:       return core.set_p(core.d_object_p(), (Class<Set<Object>>)clazz);
                case SHORT:     return core.short_p();
                case STRING:    return core.string_p();
                default:        throw new PicklerException("Unexpected TypeKind value - " + name());
            }
        }
    }

    private static <T extends Enum<T>> Class<T> castEnumClass(Class<?> clazz) {
        return (Class<T>)clazz;
    }

    private static final Map<String, TypeKind> classTypeMap = Maps.newTreeMap();

    static {
        for (TypeKind typeKind : TypeKind.values()) {
            register(typeKind);
        }
    }

    private static void register(TypeKind typeKind) {
        classTypeMap.put(typeKind.clazz.getName(), typeKind);
        final Class<?> primClass = objToPrimClass(typeKind.clazz);
        if (primClass != null) {
            classTypeMap.put(primClass.getName(), typeKind);
        }
    }

    /**
     * Determine the TypeKind for the supplied class object.
     * @param clazz
     * @return
     */
    public static TypeKind typeKindOf(Class<?> clazz) {

        // Is it one of the simple cases?
        if (clazz == null) {
            return TypeKind.NULL;
        } else if (clazz.isEnum()) {
            return TypeKind.ENUM;
        } else if (clazz.isArray()) {
            return TypeKind.ARRAY;
        }

        // Is it one of the interface-based kinds?
        final Class<?>[] interfaces = clazz.getInterfaces();
        final Class<List> listInter = List.class;
        final Class<Map> mapInter = Map.class;
        final Class<Set> setInter = Set.class;
        for (Class<?> interfaze : interfaces) {
            if (listInter.isAssignableFrom(interfaze)) {
                return TypeKind.LIST;
            } else if (mapInter.isAssignableFrom(interfaze)) {
                return TypeKind.MAP;
            } else if (setInter.isAssignableFrom(interfaze)) {
                return TypeKind.SET;
            }
        }

        // Is it registered in the classTypeMap?
        final TypeKind typeKind = classTypeMap.get(clazz.getName());
        if (typeKind != null) {
            return typeKind;
        } else {
            return TypeKind.OBJECT;
        }
    }

    /**
     * Determine the MetaType for an object.
     * @param obj
     * @return
     */
    public static MetaType ofObject(Object obj) {

        // Handle nulls.
        if (obj == null)
            return new MetaType((TypeKind.NULL));

        // Unwrap the arrays.
        Class<?> clazz = obj.getClass();
        int arrayDepth = 0;
        while (clazz.isArray()) {
            ++arrayDepth;
            clazz = clazz.getComponentType();
        }

        // Determine the TypeKind.
        final TypeKind typeKind = typeKindOf(clazz);

        if (typeKind == TypeKind.ENUM || typeKind == TypeKind.OBJECT) {
            // Enums and Objects require the class object.
            return new MetaType(typeKind, clazz, arrayDepth);
        } else {
            return new MetaType(typeKind, arrayDepth);
        }
    }

    /**
     * Construct a MetaType from its name
     * @param name the name, typically generated using the name() method.
     * @return
     */
    public static MetaType ofName(String name) {

        // Unwrap array prefixes.
        int arrayDepth = 0;
        while (name.endsWith(ARRAY_SUFFIX)) {
            ++arrayDepth;
            name = name.substring(0, name.length() - 2);
        }

        final TypeKind typeKind = TypeKind.valueOf(name);
        return new MetaType(typeKind, arrayDepth);
    }

    public final TypeKind typeKind;
    public final Class<?> clazz;
    public final int arrayDepth;

    public MetaType(TypeKind typeKind, Class<?> clazz, int arrayDepth) {
        this.typeKind = typeKind;
        this.clazz = clazz;
        this.arrayDepth = arrayDepth;
    }

    public MetaType(TypeKind typeKind, Class<?> clazz) {
        this(typeKind, clazz, 0);
    }

    public MetaType(TypeKind typeKind, int arrayDepth) {
        this(typeKind, null, arrayDepth);
    }

    public MetaType(TypeKind typeKind) {
        this(typeKind, null, 0);
    }

    /**
     * Request a pickler for the type described by this MetaType.
     * @param core PicklerCore implementation.
     * @param <PF>
     * @return
     */
    public <PF> Pickler<Object, PF> pickler(PicklerCore<PF> core) {

        // Get the pickler for the leaf type.
        Pickler<?, PF> pickler = typeKind.pickler(core, clazz);

        // Wrap the pickler for each enclosing array.
        Class<?> arrClazz = clazz == null ? typeKind.clazz : clazz;

        for (int i = 0; i < arrayDepth; ++i) {
            pickler = core.array_p((Pickler<Object, PF>)pickler, (Class<Object>)arrClazz);
            arrClazz = Array.newInstance(arrClazz, 0).getClass();
        }

        return (Pickler<Object, PF>)pickler;
    }

    /**
     * Generate a unique name for this MetaType.
     * @return readable name
     */
    public String name() {
        final StringBuilder sb = new StringBuilder(typeKind.name());
        for (int i = 0; i < arrayDepth; ++i) {
            sb.append(ARRAY_SUFFIX);
        }
        return sb.toString();
    }
}
