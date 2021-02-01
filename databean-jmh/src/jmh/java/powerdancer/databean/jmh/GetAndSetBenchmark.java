package powerdancer.databean.jmh;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import powerdancer.asmproxy.utils.ByteCodeVersion;
import powerdancer.asmproxy.utils.MapClassLoader;
import powerdancer.databean.DataBeanConstructor;
import powerdancer.databean.DataBeanConstructorRegistry;

public class GetAndSetBenchmark {

    @State(Scope.Benchmark)
    public static class MyState {
        public final DataBeanConstructorRegistry registry = new DataBeanConstructorRegistry(
                MapClassLoader.systemInstance(),
                ByteCodeVersion.VM_14
        );
        public MyBean asmProxy;
        public MyBean javaProxy;
        public MyBean pojo;
        public MyBean arrayBacked;

        @Setup(Level.Trial)
        public void doSetup() {
            asmProxy = registry.get(MyBean.class).apply();
            javaProxy = MyBeanJavaProxy.newProxy();
            pojo = new MyBeanImpl();
            arrayBacked = new MyBeanArrayBackedImpl();
        }
    }

    @Benchmark
    public void databeanProxy(Blackhole bh, MyState s) {
        bh.consume(s.asmProxy.a(1).a());
    }

    @Benchmark
    public void arrayBackedPojo(Blackhole bh, MyState s) {
        bh.consume(s.arrayBacked.a(1).a());
    }

    @Benchmark
    public void classicProxy(Blackhole bh, MyState s) {
        bh.consume(s.javaProxy.a(1).a());
    }

    @Benchmark
    public void pojo(Blackhole bh, MyState s) {
        bh.consume(s.pojo.a(1).a());
    }
}
