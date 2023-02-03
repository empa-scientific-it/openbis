package ch.ethz.sis.shared.reflect;

import java.lang.reflect.Method;
import java.util.Set;

public class Reflect {

    public static <T> Set<String> getMethodNames(Class<T> clazz) {
        Method[] methods = clazz.getMethods();
        String[] methodNames = new String[methods.length];
        for (int i = 0; methods.length > i; i++) {
            methodNames[i] = methods[i].getName();
        }

        return Set.of(methodNames);
    }

}
