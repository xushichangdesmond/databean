package powerdancer.databean.internal;

import powerdancer.databean.DataBeanConstructor;

import java.util.List;

public interface InternalDataBeanConstructor<T> extends DataBeanConstructor<T> {
    List<InternalDataBeanClassGenerator.Property> dataBeanProperties();
}
