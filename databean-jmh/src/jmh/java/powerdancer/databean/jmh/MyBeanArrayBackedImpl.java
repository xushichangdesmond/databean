package powerdancer.databean.jmh;

public class MyBeanArrayBackedImpl implements MyBean{
    Object[] state = new Object[2];

    @Override
    public int a() {
        return (Integer)state[0];
    }

    @Override
    public MyBean a(int i) {
        state[0] = i;
        return this;
    }

    @Override
    public int b() {
        return (Integer)state[1];
    }

    @Override
    public MyBean b(int i) {
        state[1] = i;
        return this;
    }
}
