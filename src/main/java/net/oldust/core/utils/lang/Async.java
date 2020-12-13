package net.oldust.core.utils.lang;

import net.oldust.core.utils.CUtils;

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
