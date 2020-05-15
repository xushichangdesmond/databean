package powerdancer.databean;

import powerdancer.asmproxy.ProxyConstructor;

import java.util.Optional;

public interface DataBeanConstructor<T> extends ProxyConstructor<T,Object[]> {
    /**
     * If the databean interface implements DataBeanBuilder, then the
     * return value will be non-empty and will be the class of the object
     * instance obtained after calling build()
     */
    Optional<Class> generatedTargetClass();

    T copyOf(T t);
}