package net.englab.contextsearcher.json;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.google.common.collect.RangeMap;
import net.englab.contextsearcher.json.serializers.RangeMapSerializer;

/**
 * This class provides custom serializers required by the project.
 */
public class CustomSerializers extends Serializers.Base {
    @Override
    public JsonSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc) {
        Class<?> raw = type.getRawClass();
        if (RangeMap.class.isAssignableFrom(raw)) {
            return new RangeMapSerializer();
        }
        return null;
    }
}
