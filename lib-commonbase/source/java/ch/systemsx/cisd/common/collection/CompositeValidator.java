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

package ch.systemsx.cisd.common.collection;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.common.collection.IValidator;

/**
 * An <code>IValidator</code> implementation which validates using a list of individual <code>IValidator</code>.
 * <p>
 * This is an <code>OR</code> operation: the tested object should only be validated by at least one <code>IValidator</code> to be valid.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class CompositeValidator<T> implements IValidator<T>
{
    private final List<IValidator<T>> validators;

    public CompositeValidator()
    {
        this.validators = new ArrayList<IValidator<T>>();
    }

    public final void addValidator(final IValidator<T> validator)
    {
        assert validator != null : "Unspecified validator.";
        validators.add(validator);
    }

    public final void removeValidator(final IValidator<T> validator)
    {
        assert validator != null : "Unspecified validator.";
        validators.remove(validator);
    }

    //
    // IValidator
    //

    @Override
    public final boolean isValid(final T t)
    {
        for (final IValidator<T> validator : validators)
        {
            if (validator.isValid(t))
            {
                return true;
            }
        }
        return false;
    }

}
