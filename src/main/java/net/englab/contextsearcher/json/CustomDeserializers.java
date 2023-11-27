package net.englab.contextsearcher.json;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.google.common.collect.RangeMap;
import net.englab.contextsearcher.json.deserializers.RangeMapDeserializer;

/**
 * This class provides custom deserializers required by the project.
 */
public class CustomDeserializers extends Deserializers.Base {
    @Override
    public JsonDeserializer<?> findBeanDeserializer(JavaType type, DeserializationConfig config, BeanDescription beanDesc) {
        if (RangeMap.class.isAssignableFrom(type.getRawClass())) {
            return new RangeMapDeserializer(type);
        }
        return null;
    }

    @Override
    public boolean hasDeserializerFor(DeserializationConfig config, Class<?> valueType) {
        return RangeMap.class.isAssignableFrom(valueType);
    }
}
