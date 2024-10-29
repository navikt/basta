package no.nav.aura.basta.util;

import java.lang.reflect.InvocationTargetException;

public abstract class ExceptionUtil {

    private ExceptionUtil() {
    }

    public static RuntimeException unpackInvocationException(InvocationTargetException e) {
        Throwable cause = e.getCause();
        if (cause instanceof RuntimeException) {
            return (RuntimeException) cause;
        }
        return new RuntimeException(cause);
    }
}
