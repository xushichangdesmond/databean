package powerdancer.databean.jackson;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.type.TypeFactory;
import powerdancer.databean.DataBeanConstructor;
import powerdancer.databean.internal.DataBeanProxy;
import powerdancer.databean.internal.InternalDataBeanClassGenerator;
import powerdancer.databean.internal.InternalDataBeanConstructor;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

class DataBeanProxyJacksonSerializer extends JsonSerializer<DataBeanProxy> {

    final InternalDataBeanConstructor ctr;

    DataBeanProxyJacksonSerializer(DataBeanConstructor ctr) {
        this.ctr = (InternalDataBeanConstructor)ctr;
    }

    DataBeanProxyJacksonSerializer(Class<?> rawClass) {
        try {
            ctr = ((DataBeanProxy)rawClass.getConstructor(Object[].class).newInstance((Object)null)).$$$dataBeanProxyConstructor();
        } catch (Exception e) {
            throw new IllegalStateException("Unexpected exception trying to handle " + rawClass, e);
        }
    }

    @Override
    public void serialize(DataBeanProxy value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        List<InternalDataBeanClassGenerator.Property> properties = ctr.dataBeanProperties();
        for (InternalDataBeanClassGenerator.Property p: properties) {
            gen.writeFieldName(p.name);
            Object fV;
            try {
                fV = p.vGetter.invoke(value);
            } catch (Throwable t) {
                throw new JsonGenerationException("Error getting value of field " + p.name, t, gen);
            }
            serializers.findValueSerializer(
                    TypeFactory.defaultInstance().constructType(p.type)
            ).serialize(fV, gen, serializers);
        }
        gen.writeEndObject();
    }

}
