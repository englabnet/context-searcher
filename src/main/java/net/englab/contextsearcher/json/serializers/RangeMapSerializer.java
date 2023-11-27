package net.englab.contextsearcher.json.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.common.collect.RangeMap;

import java.io.IOException;

/**
 * A serializer for Guava's RangeMap class. It converts the map into the following JSON:
 * <pre>
 * {
 *     "[0..43)": 68,
 *     "[43..135)": 69,
 *     "[135..230)": 70,
 *     "[230..328)": 71
 * }
 * </pre>
 */
public class RangeMapSerializer extends StdSerializer<RangeMap<?, ?>> {

    public RangeMapSerializer() {
        this(null);
    }

    public RangeMapSerializer(Class<RangeMap<?, ?>> t) {
        super(t);
    }

    @Override
    public void serialize(RangeMap<?, ?> rangeMap, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeObject(rangeMap.asMapOfRanges());
    }
}
