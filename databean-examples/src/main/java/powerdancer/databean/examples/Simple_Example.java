package powerdancer.databean.examples;

import powerdancer.databean.DataBeanConstructorRegistry;

public class Simple_Example {
    public interface MyBean {
        int i();
        MyBean i(int i);

        String s();
        MyBean s(String s);
    }

    public static void main(String[] args) throws Throwable {
        System.out.println(
                DataBeanConstructorRegistry.global.get(MyBean.class).apply()
                    .i(5)
                    .s("helloWorld")
        );
    }
}
