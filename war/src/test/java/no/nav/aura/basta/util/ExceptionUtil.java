package no.nav.aura.basta.util;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

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

    public static List<String> getMessages(final Throwable startException) {
        List<String> messages = Lists.newArrayList();
        Set<Throwable> visited = Sets.newHashSet();
        Throwable exception = startException;
        while (exception != null && !visited.contains(exception)) {
            String message = exception.getMessage();
            messages.add(exception.getClass().getName() + ": " + Optional.fromNullable(message).or("(null)"));
            exception = exception.getCause();
        }
        return messages;
    }
}
