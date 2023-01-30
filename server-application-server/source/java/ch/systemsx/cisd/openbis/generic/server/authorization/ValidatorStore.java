/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.authorization;

import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.common.reflection.ClassUtils;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.IValidator;

/**
 * A <code>static</code> store of {@link IValidator}.
 * <p>
 * This store ensures that one and only one instance of each {@link IValidator} is instantiated.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class ValidatorStore
{

    private final static Map<Class<? extends IValidator<?>>, IValidator<?>> store =
            new HashMap<Class<? extends IValidator<?>>, IValidator<?>>();

    private ValidatorStore()
    {
        // Can not be instantiated.
    }

    /**
     * For given {@link IValidator} class returns an instance of it.
     */
    public final static <V extends IValidator<T>, T> IValidator<T> getValidatorForClass(
            final Class<V> validatorClass, IAuthorizationDataProvider authorizationDataProvider)
    {
        synchronized (store)
        {
            IValidator<T> validator = cast(store.get(validatorClass));
            if (validator == null)
            {
                validator = ClassUtils.createInstance(validatorClass);
                validator.init(authorizationDataProvider);
                store.put(validatorClass, validator);
            }
            return validator;
        }
    }

    @SuppressWarnings("unchecked")
    private final static <T> IValidator<T> cast(final IValidator<?> validator)
    {
        return (IValidator<T>) validator;
    }
}
