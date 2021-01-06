package powerdancer.databean.jmh;

public interface MyBean {
    int a();
    MyBean a(int i);

    int b();
    MyBean b(int i);

    default int sum() {
        return a() + b();
    }
}