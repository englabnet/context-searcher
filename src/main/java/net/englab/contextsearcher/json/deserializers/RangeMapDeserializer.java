package net.englab.contextsearcher.json.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;

import java.io.IOException;
import java.util.Map;

/**
 * A deserializer for Guava's RangeMap class. Here's an example of JSON it can parse:
 * <pre>
 * {
 *     "[0..43)": 68,
 *     "[43..135)": 69,
 *     "[135..230)": 70,
 *     "[230..328)": 71
 * }
 * </pre>
 */
public class RangeMapDeserializer extends StdDeserializer<RangeMap<?, ?>> {

    public RangeMapDeserializer(JavaType type) {
        super(type);
    }

    @Override
    public RangeMap<?, ?> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        RangeMap<Comparable<?>, Object> rangeMap = TreeRangeMap.create();

        TypeFactory typeFactory = TypeFactory.defaultInstance();
        JavaType keyType = typeFactory.constructParametricType(Range.class, getValueType().containedType(0));
        JavaType mapType = typeFactory.constructMapLikeType(Map.class, keyType, getValueType().containedType(1));

        Map<Range<Comparable<?>>, ?> map = deserializationContext.readValue(jsonParser, mapType);

        map.forEach(rangeMap::put);

        return rangeMap;
    }
}
