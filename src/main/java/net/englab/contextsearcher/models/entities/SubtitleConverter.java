package net.englab.contextsearcher.models.entities;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.SneakyThrows;
import net.englab.contextsearcher.models.subtitles.SubtitleEntry;

import java.util.List;

/**
 * A JPA attribute converter that can convert any entity attribute to JSON and back.
 */
@Converter
public class SubtitleConverter implements AttributeConverter<List<SubtitleEntry>, String> {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @SneakyThrows
    @Override
    public String convertToDatabaseColumn(List<SubtitleEntry> o) {
        return OBJECT_MAPPER.writeValueAsString(o);
    }

    @SneakyThrows
    @Override
    public List<SubtitleEntry> convertToEntityAttribute(String s) {
        return OBJECT_MAPPER.readValue(s, new TypeReference<>() {});
    }
}
