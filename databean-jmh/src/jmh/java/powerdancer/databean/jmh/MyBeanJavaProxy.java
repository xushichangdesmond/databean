package powerdancer.databean.jmh;


import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

public class MyBeanJavaProxy {

    public static MyBean newProxy() {
        ConcurrentMap<String, Object> state = new ConcurrentHashMap<>();
//        AtomicReference<MyBean> pRef = new AtomicReference<>(null);
        MethodHandles.Lookup l = MethodHandles.lookup();
        ConcurrentMap<Method, MethodHandle> defaultMethodHandles = new ConcurrentHashMap<>();
        MyBean b = (MyBean)Proxy.newProxyInstance(
                ClassLoader.getSystemClassLoader(),
                new Class[]{MyBean.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if (method.getDeclaringClass() == Object.class) return method.invoke(proxy, args);
                        if (method.isDefault()) {
                            return defaultMethodHandles.computeIfAbsent(method, m-> {
                                try {
                                    return l.findSpecial(MyBean.class, method.getName(), MethodType.methodType(method.getReturnType(), method.getParameterTypes()), method.getDeclaringClass());
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }).bindTo(proxy).invokeWithArguments(args);
                        }
                        if (method.getParameterCount() == 0) {
                            return state.get(method.getName());
                        }
                        if (method.getParameterCount() == 1) {
                            state.put(method.getName(), args[0]);
                            return proxy;
                        }
                        return null;
                    }
                }
        );
//        pRef.set(b);
        return b;
    }
}
