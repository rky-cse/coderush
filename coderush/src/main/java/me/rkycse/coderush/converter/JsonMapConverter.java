package me.rkycse.coderush.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.HashMap;
import java.util.Map;
import me.rkycse.coderush.util.JsonConverter;

@Converter
public class JsonMapConverter implements AttributeConverter<Map<String, Map<String, Integer>>, String> {

    @Override
    public String convertToDatabaseColumn(Map<String, Map<String, Integer>> attribute) {
        if (attribute == null) {
            return null;
        }
        return JsonConverter.toJson(attribute);
    }

    @Override
    public Map<String, Map<String, Integer>> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return new HashMap<>();
        }
        return JsonConverter.fromJson(dbData, new TypeReference<Map<String, Map<String, Integer>>>() {});
    }
}
