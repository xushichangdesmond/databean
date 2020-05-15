package powerdancer.databean.jackson.examples;

import com.fasterxml.jackson.databind.ObjectMapper;
import powerdancer.databean.DataBeanConstructor;
import powerdancer.databean.DataBeanConstructorRegistry;
import powerdancer.databean.jackson.DataBeanJacksonModule;

import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class NestedBeans_Example {

    public interface MyBean {
        AnotherBean another();
        MyBean another(AnotherBean another);

        AnotherBean[] anotherArray();
        MyBean anotherArray(AnotherBean[] anotherArray);

        List<AnotherBean> anotherList();
        MyBean anotherList(List<AnotherBean> anotherList);
    }

    public interface AnotherBean {
        int n();
        AnotherBean n(int n);
    }

    public static void main(String[] args) throws Throwable {

        ObjectMapper m = new ObjectMapper()
                .registerModule(
                        new DataBeanJacksonModule(DataBeanConstructorRegistry.global)
                );

        DataBeanConstructor<MyBean> ctr = DataBeanConstructorRegistry.global.get(MyBean.class);
        DataBeanConstructor<AnotherBean> anotherCtr = DataBeanConstructorRegistry.global.get(AnotherBean.class);

        MyBean b = ctr.apply()
                .another(anotherCtr.apply().n(1))
                .anotherArray(IntStream.range(3,7).mapToObj(i->anotherCtr.apply().n(i)).toArray(AnotherBean[]::new))
                .anotherList(IntStream.range(11,15).mapToObj(i->anotherCtr.apply().n(i)).collect(Collectors.toList()));
        
        StringWriter w = new StringWriter();
        m.writeValue(w, b);
        System.out.println(w.toString());
        System.out.println("deserialize-" + m.readValue("{\"another\":{\"n\":1},\"anotherArray\":[{\"n\":3},{\"n\":4},{\"n\":5},{\"n\":6}],\"anotherList\":[{\"n\":11},{\"n\":12},{\"n\":13},{\"n\":14}]}", MyBean.class));
    }
}
