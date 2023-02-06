/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.generic.asapi.v3.exceptions;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * @author pkupczyk
 */
public class ObjectNotFoundException extends UserFailureException
{

    private static final long serialVersionUID = 1L;

    private IObjectId objectId;

    public ObjectNotFoundException(IObjectId id)
    {
        super("Object with " + id.getClass().getSimpleName() + " = [" + id + "] has not been found.");
        this.objectId = id;
    }

    public IObjectId getObjectId()
    {
        return objectId;
    }

}
