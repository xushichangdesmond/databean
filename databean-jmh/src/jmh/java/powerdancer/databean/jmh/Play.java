package powerdancer.databean.jmh;

public class Play {
    public static void main(String[] args) {
        System.out.println(MyBeanJavaProxy.newProxy().a(1).b(2).sum());
    }
}
