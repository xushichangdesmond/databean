package powerdancer.databean.jackson;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import powerdancer.databean.DataBeanConstructor;
import powerdancer.databean.internal.DataBeanProxy;
import powerdancer.databean.internal.InternalDataBeanClassGenerator;
import powerdancer.databean.internal.InternalDataBeanConstructor;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DataBeanProxyJacksonDeserializer extends JsonDeserializer {
    final Map<String, InternalDataBeanClassGenerator.Property> properties;
    final InternalDataBeanConstructor ctr;

    DataBeanProxyJacksonDeserializer(DataBeanConstructor ctr) {
        this.ctr = (InternalDataBeanConstructor)ctr;
        properties = ((List<InternalDataBeanClassGenerator.Property>)this.ctr.dataBeanProperties()).stream()
                .collect(Collectors.toMap(p->p.name, Function.identity()));
    }

    DataBeanProxyJacksonDeserializer(Class<?> rawClass) {
        try {
            ctr = ((DataBeanProxy)rawClass.getConstructor(Object[].class).newInstance((Object)null)).$$$dataBeanProxyConstructor();
        } catch (Exception e) {
            throw new IllegalStateException("Unexpected exception trying to handle " + rawClass, e);
        }
        properties = ((List<InternalDataBeanClassGenerator.Property>)this.ctr.dataBeanProperties()).stream()
                .collect(Collectors.toMap(p->p.name, Function.identity()));
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        if (p.currentToken() == JsonToken.START_OBJECT) {
            p.nextToken();
        }

        Object[] state = new Object[properties.size()];

        while (p.currentToken() != JsonToken.END_OBJECT) {
            if (p.currentToken() == JsonToken.FIELD_NAME) {
                String propName = p.getValueAsString();
                InternalDataBeanClassGenerator.Property prop = properties.get(propName);
                if (prop != null) {
                    if (p.nextToken() != JsonToken.VALUE_NULL)
                        state[prop.index] = ctxt.readValue(p, TypeFactory.defaultInstance().constructType(prop.type));
                }
            }

            p.nextToken();
        }

        try {
            return ctr.apply((Object)state);
        } catch (Exception e) {
            throw new JsonParseException(p, "Error constructing instance", e);
        }
    }
}
