package powerdancer.databean.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.fasterxml.jackson.databind.type.TypeFactory;
import powerdancer.databean.DataBeanConstructor;
import powerdancer.databean.DataBeanConstructorRegistry;
import powerdancer.databean.internal.DataBeanProxy;
import powerdancer.databean.internal.InternalDataBeanClassGenerator;

import java.util.Objects;
import java.util.Optional;

public class DataBeanJacksonModule extends Module {

    private final DataBeanConstructorRegistry registry;

    public DataBeanJacksonModule(DataBeanConstructorRegistry registry) {
        this.registry = Objects.requireNonNull(registry);
    }

    @Override
    public String getModuleName() {
        return "DataBeanJacksonModule";
    }

    @Override
    public Version version() {
        return Version.unknownVersion();
    }

    @Override
    public void setupModule(SetupContext context) {
        context.addSerializers(new Serializers.Base(){
            @Override
            public JsonSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc) {
                DataBeanConstructor ctr = registry.getOrNull(type.getRawClass());
                if (ctr != null) {
                    return new DataBeanProxyJacksonSerializer(ctr);
                }
                if (DataBeanProxy.class.isAssignableFrom(type.getRawClass())) {
                    return new DataBeanProxyJacksonSerializer(type.getRawClass());
                }
                return null;
            }
        });
        context.addDeserializers(new Deserializers.Base() {
            @Override
            public JsonDeserializer<?> findBeanDeserializer(JavaType type, DeserializationConfig config, BeanDescription beanDesc) throws JsonMappingException {
                DataBeanConstructor ctr = registry.getOrNull(type.getRawClass());
                if (ctr != null) {
                    return new DataBeanProxyJacksonDeserializer(ctr);
                }
                if (DataBeanProxy.class.isAssignableFrom(type.getRawClass())) {
                    return new DataBeanProxyJacksonDeserializer(type.getRawClass());
                }
                return null;
            }
        });
        context.addAbstractTypeResolver(new AbstractTypeResolver() {
            @Override
            public JavaType findTypeMapping(DeserializationConfig config, JavaType type) {
                return Optional.ofNullable(registry.getOrNull(type.getRawClass()))
                        .map(constructor-> TypeFactory.defaultInstance().constructType(constructor.proxyClass()))
                        .orElse(null);
            }
        });
    }
}
