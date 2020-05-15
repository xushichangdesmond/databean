package powerdancer.databean.examples;

import powerdancer.databean.DataBeanBuilder;
import powerdancer.databean.DataBeanConstructorRegistry;

public class DataBeanBuilder_Example {
    public interface MyImmutableBean {
        int i();
        String s();

        interface Builder extends MyImmutableBean, DataBeanBuilder<MyImmutableBean> {
            Builder i(int i);
            Builder s(String s);
        }
    }

    public static void main(String[] args) {
        MyImmutableBean b = DataBeanConstructorRegistry.global.get(MyImmutableBean.Builder.class)
                .apply()
                .i(5)
                .s("hello")
                .build();

        System.out.println("b - " + b);
    }
}
