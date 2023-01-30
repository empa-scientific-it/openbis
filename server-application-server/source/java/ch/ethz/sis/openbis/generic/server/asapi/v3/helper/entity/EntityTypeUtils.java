/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity;

import java.util.Collection;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyAssignmentCreation;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class EntityTypeUtils
{
    public static void checkPropertyAssignmentCreations(Collection<PropertyAssignmentCreation> propertyAssignments)
    {
        if (propertyAssignments != null)
        {
            for (PropertyAssignmentCreation assignmentCreation : propertyAssignments)
            {
                if (assignmentCreation.getPropertyTypeId() == null)
                {
                    throw new UserFailureException("PropertyTypeId cannot be null.");
                }
                if (assignmentCreation.getOrdinal() != null && assignmentCreation.getOrdinal() <= 0)
                {
                    throw new UserFailureException("Ordinal cannot be <= 0.");
                }
            }
        }
    }
}
