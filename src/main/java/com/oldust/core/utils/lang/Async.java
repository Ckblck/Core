package com.oldust.core.utils.lang;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Anotación que simboliza
 * que el método debe ejecutarse
 * asíncronamente.
 * <p>
 * Normalmente, cualquier método que posea
 * esta anotación debe invocar {@link com.oldust.core.utils.CUtils#warnSyncCall();}
 */

@Retention(RetentionPolicy.SOURCE)
public @interface Async {
}
