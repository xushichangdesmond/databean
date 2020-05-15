package powerdancer.databean.jackson.examples;

import com.fasterxml.jackson.databind.ObjectMapper;
import powerdancer.databean.DataBeanConstructor;
import powerdancer.databean.DataBeanConstructorRegistry;
import powerdancer.databean.jackson.DataBeanJacksonModule;

import java.io.StringWriter;

public class Simple_Example {

    public interface MyBean {
        int i();
        MyBean i(int i);

        String s();
        MyBean s(String s);
    }

    public static void main(String[] args) throws Throwable {

        ObjectMapper m = new ObjectMapper()
                .registerModule(
                        new DataBeanJacksonModule(DataBeanConstructorRegistry.global)
                );

        DataBeanConstructor<MyBean> ctr = DataBeanConstructorRegistry.global.get(MyBean.class);
        MyBean b = ctr.apply()
                .i(1)
                .s("one");

        StringWriter w = new StringWriter();
        m.writeValue(w, b);
        System.out.println(w.toString());
        System.out.println("deserialize-" + m.readValue("{\"i\":1,\"s\":\"one\",\"s2\":null,\"arrayChildren\":[{\"n\":88},{\"n\":89}]}", MyBean.class));
    }
}
