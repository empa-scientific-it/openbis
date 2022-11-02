/*
 * Copyright 2022 ETH Zürich, SIS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.ethz.sis.shared.exception;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.List;

public abstract class RuntimeExceptionTemplate<REASON extends Serializable, TYPE extends Enum> {
    protected final Class clazz;
    protected final int componentCode;
    protected final int exceptionCode;
    protected final String messageTemplate;
    protected final List<TYPE> types;

    public RuntimeExceptionTemplate(int componentCode, Class clazz, List<TYPE> types, int exceptionCode, String messageTemplate) {
        this.componentCode = componentCode;
        this.clazz = clazz;
        this.types = types;
        this.exceptionCode = exceptionCode;
        this.messageTemplate = messageTemplate;
    }

    public abstract REASON getReason(Object... args);

    public ThrowableReason getThrowableReason(Object... args) {
        return new ThrowableReason(getReason(args));
    }

    public RuntimeException getInstance(Object... args) {
        RuntimeException exception;
        try {
            Constructor constructor = clazz.getConstructor(Throwable.class);
            exception = (RuntimeException) constructor.newInstance(getThrowableReason(args));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return exception;
    }

    public Exception getCheckedInstance(Object... args) {
        Exception exception;
        try {
            Constructor constructor = clazz.getConstructor(Throwable.class);
            exception = (Exception) constructor.newInstance(getThrowableReason(args));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return exception;
    }

}