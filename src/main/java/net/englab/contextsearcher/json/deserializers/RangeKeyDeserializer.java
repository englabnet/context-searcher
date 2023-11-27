package net.englab.contextsearcher.json.deserializers;

import com.fasterxml.jackson.databind.*;
import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

/**
 * A deserializer for Guava's Range class when it is used as a key in a Map.
 * Currently, it designed to handle only Integer ranges. For example:
 * <pre>
 * [0..43)
 * </pre>
 * The deserializer can process all types of range bounds provided by Guava's Range class,
 * including closed, open, closed-open, and open-closed bounds.
 */
public class RangeKeyDeserializer extends KeyDeserializer {
    private final JavaType containedType;

    public RangeKeyDeserializer(JavaType containedType) {
        this.containedType = containedType;
    }

    @Override
    public Range<?> deserializeKey(String key, DeserializationContext context) {
        String strRange = key.substring(1, key.length() - 1);
        String[] range = strRange.split("\\.\\.");

        BoundType lowerType = key.charAt(0) == '[' ? BoundType.CLOSED : BoundType.OPEN;
        BoundType upperType = key.charAt(0) == ']' ? BoundType.CLOSED : BoundType.OPEN;

        // For now, we support only Integer
        if (containedType.getRawClass() == Integer.class) {
            return Range.range(Integer.valueOf(range[0]), lowerType, Integer.valueOf(range[1]), upperType);
        }

        return null;
    }
}
