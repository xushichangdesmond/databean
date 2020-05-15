package powerdancer.databean;

import powerdancer.asmproxy.ClassRepo;
import powerdancer.asmproxy.utils.ByteCodeVersion;
import powerdancer.asmproxy.utils.MapClassLoader;
import powerdancer.databean.internal.InternalDataBeanClassGenerator;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class DataBeanConstructorRegistry {
    public static final DataBeanConstructorRegistry global = new DataBeanConstructorRegistry(MapClassLoader.systemInstance(), ByteCodeVersion.runtimeVersion());

    public final ClassRepo classRepo;
    public final int byteCodeVersion;

    final ConcurrentHashMap<Class, DataBeanConstructor> constructors = new ConcurrentHashMap<>();

    public DataBeanConstructorRegistry(ClassRepo classRepo, int byteCodeVersion) {
        this.classRepo = Objects.requireNonNull(classRepo);
        this.byteCodeVersion = byteCodeVersion;
    }

    public <T> DataBeanConstructor<T> get(Class<T> beanInterface) {
        return constructors.computeIfAbsent(beanInterface, k-> InternalDataBeanClassGenerator.generateImplClass(this, k));
    }

    public <T> DataBeanConstructor<T> getOrNull(Class<T> beanInterface) {
        return constructors.get(beanInterface);
    }

    public DataBeanConstructorRegistry init(Class... beanInterfaces) {
        for (Class c : beanInterfaces) {
            get(c);
        }
        return this;
    }

}
