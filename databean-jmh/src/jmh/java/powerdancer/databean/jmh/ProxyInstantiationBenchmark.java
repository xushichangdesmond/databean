package powerdancer.databean.jmh;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import powerdancer.asmproxy.utils.ByteCodeVersion;
import powerdancer.asmproxy.utils.MapClassLoader;
import powerdancer.databean.DataBeanConstructor;
import powerdancer.databean.DataBeanConstructorRegistry;

public class ProxyInstantiationBenchmark {



    @State(Scope.Benchmark)
    public static class MyState {
        public final DataBeanConstructorRegistry registry = new DataBeanConstructorRegistry(
                MapClassLoader.systemInstance(),
                ByteCodeVersion.VM_14
        );
        public DataBeanConstructor<MyBean> constructor;

        @Setup(Level.Trial)
        public void doSetup() {
            constructor = registry.get(MyBean.class);
        }
    }

    @Benchmark
    public void databeanProxy(Blackhole bh, MyState s) {
        bh.consume(s.constructor.apply());
    }

    @Benchmark
    public void classicProxy(Blackhole bh, MyState s) {
        bh.consume(MyBeanJavaProxy.newProxy());
    }

    @Benchmark
    public void pojo(Blackhole bh, MyState s) {
        bh.consume(new MyBeanImpl());
    }
}
