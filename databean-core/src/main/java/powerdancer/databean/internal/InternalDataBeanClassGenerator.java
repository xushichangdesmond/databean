package powerdancer.databean.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import powerdancer.asmproxy.AsmProxyException;
import powerdancer.asmproxy.ProxyClassGenerator;
import powerdancer.asmproxy.ProxyConstructor;
import powerdancer.databean.DataBeanBuilder;
import powerdancer.databean.DataBeanConstructor;
import powerdancer.databean.DataBeanConstructorRegistry;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public interface InternalDataBeanClassGenerator {

    Logger logger = LoggerFactory.getLogger(InternalDataBeanClassGenerator.class);

    class DataState {
        final Object[] state;
        public DataState(int size) {
            state = new Object[size];
        }
    }

    class Property {
        public final String name;
        public final int index;
        public final MethodHandle vGetter;
        public final Type type;

        Property(int index, Method m) {
            this.index = index;
            name = m.getName();
            try {
                if (m.getParameterCount() == 0) {
                    type = m.getGenericReturnType();
                    vGetter = MethodHandles.publicLookup()
                            .findVirtual(m.getDeclaringClass(), name, MethodType.methodType(getRawClass(type)));
                }
                else {
                    type = m.getGenericParameterTypes()[0];
                    vGetter = MethodHandles.publicLookup()
                            .findVirtual(m.getDeclaringClass(), name, MethodType.methodType(getRawClass(type)));
                }
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new IllegalStateException("Unable to find getter for property " + name, e);
            }
        }
    }

    static Class getRawClass(Type t) {
        if (t instanceof Class) return (Class)t;
        if (t instanceof ParameterizedType) return getRawClass(((ParameterizedType)t).getRawType());
        throw new IllegalStateException("unable to find raw type for " + t);
    }

    static <T> DataBeanConstructor<T> generateImplClass(
            DataBeanConstructorRegistry registry,
            Class<T> dataInterfaceClass
    ) {
        // TODO - needs refactoring for readability and maintainability

        AtomicInteger i = new AtomicInteger(0);
        List<Property> properties = new ArrayList<>();
        String name = UUID.randomUUID().toString();

        AtomicReference<DataBeanConstructor> constructorRef = new AtomicReference<>(null);
        AtomicReference<DataBeanConstructor> generatedTargetDataBeanConstructor = new AtomicReference<>(null);
        Class c = ProxyClassGenerator.generateProxyClass(
                registry.classRepo,
                registry.byteCodeVersion,
                name,
                method -> {
                    if (method.isDefault()) return null;
                    if (method.getDeclaringClass() == ToString.class) {
                        return args-> dataInterfaceClass.getName() + toString(properties, (Object[]) args.state());
                    }
                    if (method.getDeclaringClass() == DataBeanBuilder.class) {
                        Class targetType = Arrays.stream(dataInterfaceClass.getGenericInterfaces())
                                .filter(t -> t instanceof ParameterizedType)
                                .map(t -> (ParameterizedType) t)
                                .filter(t -> t.getRawType() == DataBeanBuilder.class)
                                .map(t -> t.getActualTypeArguments()[0])
                                .filter(t -> t instanceof Class)
                                .map(t -> (Class) t)
                                .findFirst().get();
                        DataBeanConstructor<?> ctr = registry.get(targetType);
                        generatedTargetDataBeanConstructor.set(ctr);
                        return args->ctr.apply(args.instance());
                    }
                    if (method.getDeclaringClass() == DataBeanProxy.class) {
                        return args->constructorRef.get();
                    }

                    String propName = method.getName();

                    Property p = properties.stream()
                            .filter(prop->prop.name.equals(propName))
                            .findFirst()
                            .orElseGet(()-> {
                                Property newP = new Property(i.getAndIncrement(), method);
                                properties.add(newP);
                                return newP;
                            });

                    if (method.getParameterCount() == 0) {
                        return args-> args.state()[p.index];
                    } else if(method.getParameterCount() == 1) {
                        return args -> {
                            args.state()[p.index] = args.get(0);
                            return args.instance();
                        };
                    }
                    return null;
                },
                Object[].class,
                dataInterfaceClass,
                ToString.class,
                DataBeanProxy.class
        );

        Constructor constructor;
        try {
            constructor = c.getConstructor(Object[].class);
        } catch (Exception e) {
            throw new AsmProxyException("unexpected error", e);
        }

        int size = i.get();
        Optional<Class> optionalGeneratedTargetClass = Optional.ofNullable(generatedTargetDataBeanConstructor.get())
                .map(ProxyConstructor::proxyClass);

        DataBeanConstructor<T> dbc = new InternalDataBeanConstructor<T>() {
            @Override
            public List<Property> dataBeanProperties() {
                return properties;
            }

            @Override
            public Optional<Class> generatedTargetClass() {
                return optionalGeneratedTargetClass;
            }

            @Override
            public T copyOf(T t) {
                try {
                    Object stateCopy = properties.stream()
                            .map(p-> {
                                try {
                                    return p.vGetter.invoke(t);
                                } catch (Throwable throwable) {
                                    throw new AsmProxyException("error invoking getter for " + p.name, throwable);
                                }
                            })
                            .toArray();
                    return (T)constructor.newInstance(stateCopy);
                } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                    throw new AsmProxyException("error constructing instance", e);
                }
            }

            @Override
            public T apply(Object[] instanceState) {
                try {
                    Object o = Arrays.copyOf(instanceState, instanceState.length);
                    return (T)constructor.newInstance(o);
                } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                    throw new AsmProxyException("error constructing instance", e);
                }
            }

            @Override
            public T apply() {
                try {
                    return (T)constructor.newInstance(new Object[]{new Object[size]});
                } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                    throw new AsmProxyException("error constructing instance", e);
                }
            }

            @Override
            public Class proxyClass() {
                return c;
            }

            @Override
            public Class<Object[]> stateClass() {
                return Object[].class;
            }

            @Override
            public Class<T> primaryInterface() {
                return dataInterfaceClass;
            }

        };

        constructorRef.set(dbc);
        return dbc;
    }

    static String toString(List<Property> properties, Object[] instanceState) {
        return properties.stream().map(p->p.name + "=" +instanceState[p.index])
                .collect(Collectors.joining(",","[","]"));
    }

    interface ToString {
        String toString();
    }
}
