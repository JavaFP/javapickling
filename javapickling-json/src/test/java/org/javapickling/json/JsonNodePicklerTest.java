package org.javapickling.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.javapickling.common.*;
import org.junit.Assert;
import org.javapickling.core.*;
import org.junit.Test;

public class JsonNodePicklerTest {

    private static final JsonNodePicklerCore jsonPickler = JsonNodePicklerCore.create();

    static {
        jsonPickler.registerClassShortName(Colour.class);
        jsonPickler.registerClassShortName(ComplexClass.class);
        jsonPickler.registerClassShortName(Generic.class);
        jsonPickler.registerClassShortName(IdWrapper.class);
    }

    @Test
    public void testPickle() throws Exception {

        final ComplexClass simple = ComplexClass.createInstance(true);

        final RoundTrip jsonTimeMs = roundTripViaJson(simple);
        System.out.println(jsonTimeMs);

        Utils.roundTripViaJavaSer(simple);
        final RoundTrip javaSerTimeMs = Utils.roundTripViaJavaSer(simple);
        System.out.println(javaSerTimeMs);
    }

    private static RoundTrip roundTripViaJson(ComplexClass complex) throws Exception {

        final Pickler<ComplexClass, JsonNode> pickler = jsonPickler.object_p(ComplexClass.class);

        final long startTime1 = System.nanoTime();
        final JsonNode node = pickler.pickle(complex, null);
        final long endTime1 = System.nanoTime();

        final String jsonPretty = JsonNodePicklerCore.nodeToString(node, true);
        System.out.println("JSON=");
        System.out.println(jsonPretty);
        final String json = JsonNodePicklerCore.nodeToString(node, false);
        final int size = json.length();

        final long startTime2 = System.nanoTime();
        final ComplexClass complex2 = pickler.unpickle(node);
        final long endTime2 = System.nanoTime();

        Assert.assertEquals(complex, complex2);

        return new RoundTrip("JsonNodePickler", endTime1 - startTime1, endTime2 - startTime2, size);
    }

}
