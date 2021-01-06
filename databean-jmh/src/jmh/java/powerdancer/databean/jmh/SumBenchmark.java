package powerdancer.databean.jmh;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import powerdancer.asmproxy.utils.ByteCodeVersion;
import powerdancer.asmproxy.utils.MapClassLoader;
import powerdancer.databean.DataBeanConstructorRegistry;

public class SumBenchmark {

    @State(Scope.Benchmark)
    public static class MyState {
        public final DataBeanConstructorRegistry registry = new DataBeanConstructorRegistry(
                MapClassLoader.systemInstance(),
                ByteCodeVersion.VM_14
        );
        public MyBean asmProxy;
        public MyBean javaProxy;
        public MyBean pojo;

        @Setup(Level.Trial)
        public void doSetup() {
            asmProxy = registry.get(MyBean.class).apply().a(1).b(2);
            javaProxy = MyBeanJavaProxy.newProxy().a(1).b(2);
            pojo = new MyBeanImpl().a(1).b(2);
        }
    }

    @Benchmark
    public void databeanProxy(Blackhole bh, MyState s) {
        bh.consume(s.asmProxy.sum());
    }

    @Benchmark
    public void classicProxy(Blackhole bh, MyState s) {
        bh.consume(s.javaProxy.sum());
    }

    @Benchmark
    public void pojo(Blackhole bh, MyState s) {
        bh.consume(s.pojo.sum());
    }
}
