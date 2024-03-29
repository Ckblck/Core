package net.oldust.core.utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Anotación que simboliza
 * que el método debe ejecutarse
 * asíncronamente.
 * <p>
 * Normalmente, cualquier método que posea
 * esta anotación debe invocar {@link CUtils#warnSyncCall();}
 */

@Retention(RetentionPolicy.SOURCE)
public @interface Async {
}
