package org.javafp.javapickling.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Optional;
import org.javafp.javapickling.core.*;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * PicklerCore implementation which pickles objects to JsonNodes.
 */
public class JsonNodePicklerCore extends PicklerCoreBase<JsonNode> {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final JsonFactory factory = mapper.getFactory();

    public static JsonNodePicklerCore create() {
        final JsonNodePicklerCore core = new JsonNodePicklerCore();
        core.initialise();
        return core;
    }

    /**
     * Utility function to simplify converting a JsonNode into a String.
     * @param node
     * @param pretty
     * @return
     * @throws JsonProcessingException
     */
    public static String nodeToString(JsonNode node, boolean pretty) throws JsonProcessingException {
        final ObjectMapper om = new ObjectMapper();
        final ObjectWriter writer =
                pretty ?
                    om.writerWithDefaultPrettyPrinter() :
                    om.writer();

        return writer.writeValueAsString(node);
    }

    /**
     * Utility function to simplify converting a String into a JsonNode.
     * @param json
     * @return
     * @throws Exception
     */
    public static JsonNode stringToNode(String json) throws Exception {
        final JsonParser parser = factory.createParser(json);
        return mapper.readTree(parser);
    }

    protected void initialise() {
        super.initialise();
        registerGeneric(Optional.class, OptionalPickler.class);
    }

    protected final Pickler<Object, JsonNode> nullP = new Pickler<Object, JsonNode>() {

        @Override
        public JsonNode pickle(Object obj, JsonNode target) throws Exception {
            return nodeFactory.nullNode();
        }

        @Override
        public Object unpickle(JsonNode source) throws Exception {
            return null;
        }
    };

    protected final Pickler<Boolean, JsonNode> booleanP = new Pickler<Boolean, JsonNode>() {

        @Override
        public JsonNode pickle(Boolean b, JsonNode target) throws Exception {
            return nodeFactory.booleanNode(b);
        }

        @Override
        public Boolean unpickle(JsonNode source) throws Exception {
            return source.asBoolean();
        }
    };

    protected final Pickler<Byte, JsonNode> byteP = new Pickler<Byte, JsonNode>() {

        @Override
        public JsonNode pickle(Byte b, JsonNode target) throws Exception {
            return nodeFactory.numberNode(b);
        }

        @Override
        public Byte unpickle(JsonNode source) throws Exception {
            return (byte)source.asInt();
        }
    };

    protected final Pickler<Character, JsonNode> charP = new Pickler<Character, JsonNode>() {

        @Override
        public JsonNode pickle(Character c, JsonNode target) throws Exception {
            return nodeFactory.textNode(String.valueOf(c));
        }

        @Override
        public Character unpickle(JsonNode source) throws Exception {
            return source.asText().charAt(0);
        }
    };

    protected final Pickler<String, JsonNode> stringP = new Pickler<String, JsonNode>() {

        @Override
        public JsonNode pickle(String s, JsonNode target) throws Exception {
            return nodeFactory.textNode(s);
        }

        @Override
        public String unpickle(JsonNode source) throws Exception {
            return source.asText();
        }
    };

    protected final Pickler<Integer, JsonNode> integerP = new Pickler<Integer, JsonNode>() {

        @Override
        public JsonNode pickle(Integer i, JsonNode target) throws Exception {
            return nodeFactory.numberNode(i);
        }

        @Override
        public Integer unpickle(JsonNode source) throws Exception {
            return source.asInt();
        }
    };

    protected final Pickler<Short, JsonNode> shortP = new Pickler<Short, JsonNode>() {

        @Override
        public JsonNode pickle(Short s, JsonNode target) throws Exception {
            return nodeFactory.numberNode(s);
        }

        @Override
        public Short unpickle(JsonNode source) throws Exception {
            return source.shortValue();
        }
    };

    protected final Pickler<Long, JsonNode> longP = new Pickler<Long, JsonNode>() {

        @Override
        public JsonNode pickle(Long l, JsonNode target) throws Exception {
            return nodeFactory.numberNode(l);
        }

        @Override
        public Long unpickle(JsonNode source) throws Exception {
            return source.asLong();
        }
    };

    protected final Pickler<Float, JsonNode> floatP = new Pickler<Float, JsonNode>() {

        @Override
        public JsonNode pickle(Float f, JsonNode target) throws Exception {
            return nodeFactory.numberNode(f);
        }

        @Override
        public Float unpickle(JsonNode source) throws Exception {
            return source.floatValue();
        }
    };

    protected final Pickler<Double, JsonNode> doubleP = new Pickler<Double, JsonNode>() {

        @Override
        public JsonNode pickle(Double d, JsonNode target) throws Exception {
            return nodeFactory.numberNode(d);
        }

        @Override
        public Double unpickle(JsonNode source) throws Exception {
            return source.asDouble();
        }
    };

    protected final Pickler<boolean[], JsonNode> booleanArrayP = new Pickler<boolean[], JsonNode>() {

        final Pickler<Boolean, JsonNode> elemPickler = boolean_p();

        @Override
        public JsonNode pickle(boolean[] arr, JsonNode target) throws Exception {

            final ArrayNode result = nodeFactory.arrayNode();

            for (boolean elem : arr) {
                result.add(elemPickler.pickle(elem, result));
            }

            return result;
        }

        @Override
        public boolean[] unpickle(JsonNode source) throws Exception {

            if (!source.isArray())
                throw new PicklerException("Can not unpickle a " + source.getNodeType() + " into an array");

            final ArrayNode contNode = (ArrayNode)source;

            final boolean[] result = new boolean[contNode.size()];

            int i = 0;
            for (JsonNode elem : contNode) {
                result[i] = elemPickler.unpickle(elem);
                ++i;
            }

            return result;
        }
    };


    protected final Pickler<byte[], JsonNode> byteArrayP = new Pickler<byte[], JsonNode>() {

            final Pickler<Byte, JsonNode> elemPickler = byte_p();

            @Override
            public JsonNode pickle(byte[] arr, JsonNode target) throws Exception {

                final ArrayNode result = nodeFactory.arrayNode();

                for (byte elem : arr) {
                    result.add(elemPickler.pickle(elem, result));
                }

                return result;
            }

            @Override
            public byte[] unpickle(JsonNode source) throws Exception {

                if (!source.isArray())
                    throw new PicklerException("Can not unpickle a " + source.getNodeType() + " into an array");

                final ArrayNode contNode = (ArrayNode)source;

                final byte[] result = new byte[contNode.size()];

                int i = 0;
                for (JsonNode elem : contNode) {
                    result[i] = elemPickler.unpickle(elem);
                    ++i;
                }

                return result;
            }
        };


    protected final  Pickler<char[], JsonNode> charArrayP = new Pickler<char[], JsonNode>() {

        final Pickler<Character, JsonNode> elemPickler = char_p();

        @Override
        public JsonNode pickle(char[] arr, JsonNode target) throws Exception {

            final ArrayNode result = nodeFactory.arrayNode();

            for (char elem : arr) {
                result.add(elemPickler.pickle(elem, result));
            }

            return result;
        }

        @Override
        public char[] unpickle(JsonNode source) throws Exception {

            if (!source.isArray())
                throw new PicklerException("Can not unpickle a " + source.getNodeType() + " into an array");

            final ArrayNode contNode = (ArrayNode)source;

            final char[] result = new char[contNode.size()];

            int i = 0;
            for (JsonNode elem : contNode) {
                result[i] = elemPickler.unpickle(elem);
                ++i;
            }

            return result;
        }
    };


    protected final  Pickler<int[], JsonNode> integerArrayP = new Pickler<int[], JsonNode>() {

        final Pickler<Integer, JsonNode> elemPickler = integer_p();

        @Override
        public JsonNode pickle(int[] arr, JsonNode target) throws Exception {

            final ArrayNode result = nodeFactory.arrayNode();

            for (int elem : arr) {
                result.add(elemPickler.pickle(elem, result));
            }

            return result;
        }

        @Override
        public int[] unpickle(JsonNode source) throws Exception {

            if (!source.isArray())
                throw new PicklerException("Can not unpickle a " + source.getNodeType() + " into an array");

            final ArrayNode contNode = (ArrayNode)source;

            final int[] result = new int[contNode.size()];

            int i = 0;
            for (JsonNode elem : contNode) {
                result[i] = elemPickler.unpickle(elem);
                ++i;
            }

            return result;
        }
    };


    protected final  Pickler<short[], JsonNode> shortArrayP = new Pickler<short[], JsonNode>() {

        final Pickler<Short, JsonNode> elemPickler = short_p();

        @Override
        public JsonNode pickle(short[] arr, JsonNode target) throws Exception {

            final ArrayNode result = nodeFactory.arrayNode();

            for (short elem : arr) {
                result.add(elemPickler.pickle(elem, result));
            }

            return result;
        }

        @Override
        public short[] unpickle(JsonNode source) throws Exception {

            if (!source.isArray())
                throw new PicklerException("Can not unpickle a " + source.getNodeType() + " into an array");

            final ArrayNode contNode = (ArrayNode)source;

            final short[] result = new short[contNode.size()];

            int i = 0;
            for (JsonNode elem : contNode) {
                result[i] = elemPickler.unpickle(elem);
                ++i;
            }

            return result;
        }
    };

    protected final  Pickler<long[], JsonNode> longArrayP = new Pickler<long[], JsonNode>() {

        final Pickler<Long, JsonNode> elemPickler = long_p();

        @Override
        public JsonNode pickle(long[] arr, JsonNode target) throws Exception {

            final ArrayNode result = nodeFactory.arrayNode();

            for (long elem : arr) {
                result.add(elemPickler.pickle(elem, result));
            }

            return result;
        }

        @Override
        public long[] unpickle(JsonNode source) throws Exception {

            if (!source.isArray())
                throw new PicklerException("Can not unpickle a " + source.getNodeType() + " into an array");

            final ArrayNode contNode = (ArrayNode)source;

            final long[] result = new long[contNode.size()];

            int i = 0;
            for (JsonNode elem : contNode) {
                result[i] = elemPickler.unpickle(elem);
                ++i;
            }

            return result;
        }
    };

    protected final  Pickler<float[], JsonNode> floatArrayP = new Pickler<float[], JsonNode>() {

        final Pickler<Float, JsonNode> elemPickler = float_p();

        @Override
        public JsonNode pickle(float[] arr, JsonNode target) throws Exception {

            final ArrayNode result = nodeFactory.arrayNode();

            for (float elem : arr) {
                result.add(elemPickler.pickle(elem, result));
            }

            return result;
        }

        @Override
        public float[] unpickle(JsonNode source) throws Exception {

            if (!source.isArray())
                throw new PicklerException("Can not unpickle a " + source.getNodeType() + " into an array");

            final ArrayNode contNode = (ArrayNode)source;

            final float[] result = new float[contNode.size()];

            int i = 0;
            for (JsonNode elem : contNode) {
                result[i] = elemPickler.unpickle(elem);
                ++i;
            }

            return result;
        }
    };

    public Pickler<double[], JsonNode> doubleArrayP = new Pickler<double[], JsonNode>() {

        final Pickler<Double, JsonNode> elemPickler = double_p();

        @Override
        public JsonNode pickle(double[] arr, JsonNode target) throws Exception {

            final ArrayNode result = nodeFactory.arrayNode();

            for (double elem : arr) {
                result.add(elemPickler.pickle(elem, result));
            }

            return result;
        }

        @Override
        public double[] unpickle(JsonNode source) throws Exception {

            if (!source.isArray())
                throw new PicklerException("Can not unpickle a " + source.getNodeType() + " into an array");

            final ArrayNode contNode = (ArrayNode)source;

            final double[] result = new double[contNode.size()];

            int i = 0;
            for (JsonNode elem : contNode) {
                result[i] = elemPickler.unpickle(elem);
                ++i;
            }

            return result;
        }
    };

    protected final ObjectPickler<JsonNode> objectMapP = new ObjectPickler<JsonNode>() {

        @Override
        public FieldPickler<JsonNode> pickler(final JsonNode target) {

            return new AbstractFieldPickler(target) {

                private ObjectNode objectNode = nodeFactory.objectNode();

                @Override
                public <T> void field(String name, T value, Pickler<T, JsonNode> pickler) throws Exception {
                    objectNode.put(name, pickler.pickle(value, target));
                }

                @Override
                public JsonNode pickle(JsonNode source) {
                    return objectNode;
                }
            };
        }

        @Override
        public FieldUnpickler<JsonNode> unpickler(final JsonNode source) {

            return new AbstractFieldUnpickler(source) {

                @Override
                public <T> T field(String name, Pickler<T, JsonNode> pickler) throws Exception {
                    return pickler.unpickle(source.get(name));
                }
            };
        }
    };

    protected final JsonNodeFactory nodeFactory;

    private JsonNodePicklerCore() {
        this(JsonNodeFactory.instance);
    }

    private JsonNodePicklerCore(JsonNodeFactory nodeFactory) {
        this.nodeFactory = nodeFactory;
    }

    @Override
    public Pickler<Object, JsonNode> null_p() {
        return nullP;
    }

    @Override
    public Pickler<Boolean, JsonNode> boolean_p() {
        return booleanP;
    }

    @Override
    public Pickler<Byte, JsonNode> byte_p() {
        return byteP;
    }

    @Override
    public Pickler<Character, JsonNode> char_p() {
        return charP;
    }

    @Override
    public Pickler<String, JsonNode> string_p() {
        return stringP;
    }

    @Override
    public Pickler<Integer, JsonNode> integer_p() {
        return integerP;
    }

    @Override
    public Pickler<Short, JsonNode> short_p() {
        return shortP;
    }

    @Override
    public Pickler<Long, JsonNode> long_p() {
        return longP;
    }

    @Override
    public Pickler<Float, JsonNode> float_p() {
        return floatP;
    }

    @Override
    public Pickler<Double, JsonNode> double_p() {
        return doubleP;
    }

    @Override
    public Pickler<boolean[], JsonNode> boolean_array_p() {
        return booleanArrayP;
    }

    @Override
    public Pickler<byte[], JsonNode> byte_array_p() {
        return byteArrayP;
    }

    @Override
    public Pickler<char[], JsonNode> char_array_p() {
        return charArrayP;
    }

    @Override
    public Pickler<int[], JsonNode> integer_array_p() {
        return integerArrayP;
    }

    @Override
    public Pickler<short[], JsonNode> short_array_p() {
        return shortArrayP;
    }

    @Override
    public Pickler<long[], JsonNode> long_array_p() {
        return longArrayP;
    }

    @Override
    public Pickler<float[], JsonNode> float_array_p() {
        return floatArrayP;
    }

    @Override
    public Pickler<double[], JsonNode> double_array_p() {
        return doubleArrayP;
    }

    @Override
    public <T extends Enum<T>> Pickler<T, JsonNode> enum_p(final Class<T> enumClass) {

        return new Pickler<T, JsonNode>() {

            @Override
            public JsonNode pickle(T t, JsonNode target) throws Exception {
                return nodeFactory.textNode(t.name());
            }

            @Override
            public T unpickle(JsonNode source) throws Exception {
                return T.valueOf(enumClass, source.asText());
            }
        };
    }

    @Override
    public <T> Pickler<T[], JsonNode> array_p(
            final Pickler<T, JsonNode> elemPickler,
            final Class<T> elemClass) {

        return new Pickler<T[], JsonNode>() {

            @Override
            public JsonNode pickle(T[] arr, JsonNode target) throws Exception {

                final ArrayNode result = nodeFactory.arrayNode();

                for (T elem : arr) {
                    result.add(elemPickler.pickle(elem, result));
                }

                return result;
            }

            @Override
            public T[] unpickle(JsonNode source) throws Exception {

                if (!source.isArray())
                    throw new PicklerException("Can not unpickle a " + source.getNodeType() + " into an array");

                final ArrayNode contNode = (ArrayNode)source;

                final T[] result = (T[]) Array.newInstance(elemClass, contNode.size());

                int i = 0;
                for (JsonNode elem : contNode) {
                    result[i] = elemPickler.unpickle(elem);
                    ++i;
                }

                return result;
            }
        };
    }

    @Override
    public <T> Pickler<List<T>, JsonNode> list_p(
            final Pickler<T, JsonNode> elemPickler,
            final Class<? extends List> listClass) {

        return new Pickler<List<T>, JsonNode>() {

            @Override
            public JsonNode pickle(List<T> list, JsonNode target) throws Exception {

                final ArrayNode result = nodeFactory.arrayNode();

                for (T elem : list) {
                    result.add(elemPickler.pickle(elem, result));
                }

                return result;
            }

            @Override
            public List<T> unpickle(JsonNode source) throws Exception {

                if (!source.isArray())
                    throw new PicklerException("Can not unpickle a " + source.getNodeType() + " into a List");

                final ArrayNode contNode = (ArrayNode)source;

                final List<T> result = newInstance(listClass);

                for (JsonNode elem : contNode) {
                    result.add(elemPickler.unpickle(elem));
                }

                return result;
            }
        };
    }

    @Override
    public <T> Pickler<Map<String, T>, JsonNode> map_p(
            final Pickler<T, JsonNode> valuePickler,
            final Class<? extends Map> mapClass) {

        return new Pickler<Map<String, T>, JsonNode>() {

            @Override
            public JsonNode pickle(Map<String, T> map, JsonNode target) throws Exception {

                final ObjectNode result = nodeFactory.objectNode();

                for (Map.Entry<String, T> entry : map.entrySet()) {
                    result.put(entry.getKey(), valuePickler.pickle(entry.getValue(), result));
                }

                return result;
            }

            @Override
            public Map<String, T> unpickle(JsonNode source) throws Exception {

                if (!source.isObject())
                    throw new PicklerException("Can not unpickle a " + source.getNodeType() + " into a Map");

                final ObjectNode objectNode = (ObjectNode)source;

                final Map<String, T> result = newInstance(mapClass);

                for (Iterator<Map.Entry<String, JsonNode>> iter = objectNode.fields(); iter.hasNext();) {
                    final Map.Entry<String, JsonNode> entry = iter.next();
                    result.put(entry.getKey(), valuePickler.unpickle(entry.getValue()));
                }

                return result;
            }
        };
    }

    @Override
    public <K, V> Pickler<Map<K, V>, JsonNode> map_p(
            final Pickler<K, JsonNode> keyPickler,
            final Pickler<V, JsonNode> valuePickler,
            final Class<?  extends Map> mapClass) {

        return new Pickler<Map<K, V>, JsonNode>() {

            private static final String keyF = "@key";
            private static final String valueF = "@value";

            @Override
            public JsonNode pickle(Map<K, V> map, JsonNode target) throws Exception {

                final ArrayNode result = nodeFactory.arrayNode();

                for (Map.Entry<K, V> entry : map.entrySet()) {
                    final ObjectNode elem = nodeFactory.objectNode();
                    elem.put(keyF, keyPickler.pickle(entry.getKey(), result));
                    elem.put(valueF, valuePickler.pickle(entry.getValue(), result));
                    result.add(elem);
                }

                return result;
            }

            @Override
            public Map<K, V> unpickle(JsonNode source) throws Exception {

                if (!source.isArray())
                    throw new PicklerException("Can not unpickle a " + source.getNodeType() + " into a Map");

                final ArrayNode arrayNode = (ArrayNode)source;

                final Map<K, V> result;
                try {
                    result = mapClass.newInstance();
                } catch (InstantiationException ex) {
                    throw new PicklerException("Can not create map class", ex);
                } catch (IllegalAccessException ex) {
                    throw new PicklerException("Can not create map class", ex);
                }

                for (JsonNode child : arrayNode) {
                    final ObjectNode objectNode = (ObjectNode)child;
                    final K key = keyPickler.unpickle(objectNode.get(keyF));
                    final V value = valuePickler.unpickle(objectNode.get(valueF));
                    result.put(key, value);
                }

                return result;
            }
        };
    }

    @Override
    public <T> Pickler<Set<T>, JsonNode> set_p(
            final Pickler<T, JsonNode> elemPickler,
            final Class<? extends Set> setClass) {

        return new Pickler<Set<T>, JsonNode>() {

            @Override
            public JsonNode pickle(Set<T> set, JsonNode target) throws Exception {

                final ArrayNode result = nodeFactory.arrayNode();

                for (T elem : set) {
                    result.add(elemPickler.pickle(elem, result));
                }

                return result;
            }

            @Override
            public Set<T> unpickle(JsonNode source) throws Exception {

                if (!source.isArray())
                    throw new PicklerException("Can not unpickle a " + source.getNodeType() + " into a Set");

                final ArrayNode contNode = (ArrayNode)source;

                final Set<T> result = newInstance(setClass);

                for (JsonNode elem : contNode) {
                    result.add(elemPickler.unpickle(elem));
                }

                return result;
            }
        };
    }

    @Override
    public Pickler<Object, JsonNode> d_object_p() {
        return new DynamicObjectJsonNodePickler<Object>(this, Object.class);
    }

    @Override
    public <T, S extends T> Pickler<S, JsonNode> d_object_p(Class<T> clazz) {
        return new DynamicObjectJsonNodePickler<S>(this, clazz);
    }

    @Override
    public ObjectPickler<JsonNode> object_map() {
        return objectMapP;
    }

    @Override
    public <T> Pickler<T, JsonNode> nullable(final Pickler<T, JsonNode> pickler) {
        return new Pickler<T, JsonNode>() {

            @Override
            public JsonNode pickle(T t, JsonNode target) throws Exception {
                if (t == null) {
                    return nodeFactory.nullNode();
                } else {
                    return pickler.pickle(t, target);
                }
            }

            @Override
            public T unpickle(JsonNode source) throws Exception {
                if (source == null || source.isNull()) {
                    return null;
                } else {
                    return pickler.unpickle(source);
                }
            }
        };
    }
}
