/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Operations on {@link EntityTypePE} extensions. Note that you can use only one <em>define</em> method at a time.
 * 
 * @author Tomasz Pylak
 */
public interface IEntityTypeBO extends IBusinessObject
{
    /** defines a new sample type, it can be saved later in the database */
    public void define(final SampleType entityType);

    /** defines a new material type, it can be saved later in the database */
    public void define(final MaterialType entityType);

    /** defines a new experiment type, it can be saved later in the database */
    public void define(final ExperimentType entityType);

    /** defines a new data set type, it can be saved later in the database */
    public void define(final DataSetType entityType);

    /** Loads entity type of given kind and code */
    public void load(EntityKind entityKind, String code);

    /** Deletes previously loaded entity type from database */
    public void delete();
}
