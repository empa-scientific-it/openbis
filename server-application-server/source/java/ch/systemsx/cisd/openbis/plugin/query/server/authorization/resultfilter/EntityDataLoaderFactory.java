/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.query.server.authorization.resultfilter;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * Default implementation of {@link IEntityDataLoaderFactory}.
 * 
 * @author Izabela Adamczyk
 */
class EntityDataLoaderFactory implements IEntityDataLoaderFactory
{
    private final IDAOFactory factory;

    public EntityDataLoaderFactory(IDAOFactory factory)
    {
        this.factory = factory;
    }

    @Override
    public IEntityDataLoader create(EntityKind kind)
    {
        switch (kind)
        {
            case EXPERIMENT:
                return new ExperimentDataLoader(factory.getExperimentDAO());
            case SAMPLE:
                return new SampleDataLoader(factory.getSampleDAO());
            case DATA_SET:
                return new DataSetDataLoader(factory.getDataDAO());
            case MATERIAL:
                throw new UnsupportedOperationException();
        }
        throw new IllegalArgumentException();
    }
}