package com.docheck.flow.configuration;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;

@Configuration
public class JacksonConfiguration {

    @Bean
    public Module brazilianNumberModule() {
        SimpleModule module = new SimpleModule("BrazilianNumberModule");
        module.addDeserializer(Double.class, new BrazilianDoubleDeserializer());
        module.addDeserializer(double.class, new BrazilianDoubleDeserializer());
        return module;
    }

    public static class BrazilianDoubleDeserializer extends JsonDeserializer<Double> {
        private final DecimalFormat brazilianFormat;

        public BrazilianDoubleDeserializer() {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("pt-BR"));
            symbols.setDecimalSeparator(',');
            symbols.setGroupingSeparator('.');
            this.brazilianFormat = new DecimalFormat("#,##0.##", symbols);
            this.brazilianFormat.setParseBigDecimal(false);
        }

        @Override
        public Double deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String value = p.getValueAsString();
            if (value == null || value.trim().isEmpty()) {
                return null;
            }

            try {
                // Try standard JSON number format first
                if (!value.contains(",") && !value.contains(".") || 
                    (value.contains(".") && !value.contains(",") && value.indexOf('.') > value.length() - 4)) {
                    return Double.parseDouble(value.replace(".", "").replace(",", "."));
                }
                
                // Try Brazilian format (e.g., "2.555,55")
                if (value.contains(",")) {
                    return brazilianFormat.parse(value).doubleValue();
                }
                
                // Fallback to standard parsing
                return Double.parseDouble(value);
                
            } catch (ParseException | NumberFormatException e) {
                throw new IOException("Cannot deserialize value '" + value + "' as Double", e);
            }
        }
    }
}