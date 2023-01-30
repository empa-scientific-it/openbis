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

package ch.systemsx.cisd.openbis.generic.server.authorization.validator;

import java.util.Collection;

import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * A validator that allows to validate not only a single object but also a collection of objects at once. When validating a whole collection the
 * validator can make some optimizations, e.g. fetch the data needed by the validation only once.
 * 
 * @author pkupczyk
 */
public interface ICollectionValidator<T> extends IValidator<T>
{

    /**
     * Validates given <var>values</var> for a given <var>person</var>. Returns only values that the given person is allowed to see.
     */
    public Collection<T> getValid(final PersonPE person, final Collection<T> values);

}
