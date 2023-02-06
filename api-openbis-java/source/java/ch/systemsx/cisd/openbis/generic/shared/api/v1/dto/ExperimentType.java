/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Immutable value object representing an experiment type.
 * 
 * @author Franz-Josef Elmer
 */
@SuppressWarnings("unused")
@JsonObject("ExperimentType")
public class ExperimentType extends EntityType
{
    private static final long serialVersionUID = 1L;

    /**
     * Class used to initialize a new {@link ExperimentType} instance. Necessary since all the fields of a {@link ExperimentType} are final.
     * 
     * @author Franz-Josef Elmer
     */
    public static final class ExperimentTypeInitializer extends EntityTypeInitializer
    {
        public ExperimentTypeInitializer()
        {
            super();
        }
    }

    /**
     * Creates a new instance with the provided initializer
     * 
     * @throws IllegalArgumentException if some of the required information is not provided.
     */
    public ExperimentType(ExperimentTypeInitializer initializer)
    {
        super(initializer);
    }

    @Override
    public String toString()
    {
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        builder.append(getCode());
        builder.append(getDescription());
        builder.append(getPropertyTypeGroups());
        return builder.toString();
    }

    //
    // JSON-RPC
    //

    private ExperimentType()
    {
    }

}
