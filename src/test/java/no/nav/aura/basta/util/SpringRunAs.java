package no.nav.aura.basta.util;

import static no.nav.aura.basta.util.ExceptionUtil.unpackInvocationException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.google.common.base.Function;

public class SpringRunAs {

    public static <O> O runAs(AuthenticationManager authenticationManager, String userName, String password, Function<Void, O> function) {
        return SpringRunAs.runAs(authenticationManager, userName, password, null, function);
    }

    public static <I, O> O runAs(AuthenticationManager authenticationManager, String userName, String password, I i, Function<I, O> function) {
        Authentication token = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userName, password));
        Authentication original = SecurityContextHolder.getContext().getAuthentication();
        SecurityContextHolder.getContext().setAuthentication(token);
        try {
            return function.apply(i);
        } finally {
            SecurityContextHolder.getContext().setAuthentication(original);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T wrapWithAuthentication(final AuthenticationManager authenticationManager, Class<T> tClass, final T t, final String userName, final String password) {
        return (T) Proxy.newProxyInstance(tClass.getClassLoader(), ArrayHelper.of(tClass), new InvocationHandler() {
            @SuppressWarnings("serial")
            public Object invoke(final Object proxy, final Method method, final Object[] args) {
                return runAs(authenticationManager, userName, password, new SerializableFunction<Void, T>() {
                    public T process(Void input) {
                        try {
                            return (T) method.invoke(t, args);
                        } catch (IllegalAccessException | IllegalArgumentException e) {
                            throw new RuntimeException(e);
                        } catch (InvocationTargetException e) {
                            throw unpackInvocationException(e);
                        }
                    }
                });
            }
        });
    }

}
