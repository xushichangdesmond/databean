package powerdancer.databean.jmh;

public class MyBeanImpl implements MyBean{
    int a;
    int b;

    @Override
    public int a() {
        return a;
    }

    @Override
    public MyBean a(int i) {
        a = i;
        return this;
    }

    @Override
    public int b() {
        return b;
    }

    @Override
    public MyBean b(int i) {
        b = i;
        return this;
    }
}
