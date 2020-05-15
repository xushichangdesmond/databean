package powerdancer.databean.examples;

import powerdancer.databean.DataBeanConstructor;
import powerdancer.databean.DataBeanConstructorRegistry;

public class BeanCopy_Example {
    public interface MyBean {
        int i();
        MyBean i(int i);

        String s();
        MyBean s(String s);
    }

    public static void main(String[] args) throws Throwable {
        DataBeanConstructor<MyBean> constructor = DataBeanConstructorRegistry.global.get(MyBean.class);
        MyBean original = constructor.apply()
                .i(5)
                .s("helloWorld");
        System.out.println("original - " + original);

        MyBean copy = constructor.copyOf(original);
        System.out.println("copy - " + copy);

        copy.i(6).s("bye");
        System.out.println("copy.i(6).s(\"bye\")");
        System.out.println("original - " + original);
        System.out.println("copy - " + copy);
    }
}
