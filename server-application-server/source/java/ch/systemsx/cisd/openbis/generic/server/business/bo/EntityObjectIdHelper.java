/*
 * Copyright 2012 ETH Zuerich, CISD
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

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.IObjectId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.dataset.IDataSetId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.experiment.IExperimentId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.material.IMaterialId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.sample.ISampleId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityInformationHolderDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * @author Jakub Straszewski
 */
public class EntityObjectIdHelper
{
    protected final ICommonBusinessObjectFactory businessObjectFactory;

    public EntityObjectIdHelper(ICommonBusinessObjectFactory businessObjectFactory)
    {
        this.businessObjectFactory = businessObjectFactory;
    }

    public IEntityInformationHolderDTO getEntityById(Session session, IObjectId id)
    {
        if (id instanceof IExperimentId)
        {
            return getExperimentById(session, (IExperimentId) id);
        }
        if (id instanceof ISampleId)
        {
            return getSampleById(session, (ISampleId) id);
        }
        if (id instanceof IDataSetId)
        {
            return getDataSetById(session, (IDataSetId) id);
        }
        if (id instanceof IMaterialId)
        {
            return getMaterialById(session, (IMaterialId) id);
        }
        throw new IllegalArgumentException("The " + id.getClass()
                + " is not recognized as correct argument");
    }

    private MaterialPE getMaterialById(Session session, IMaterialId id)
    {
        IMaterialBO bo = businessObjectFactory.createMaterialBO(session);
        return bo.tryFindByMaterialId(id);
    }

    private DataPE getDataSetById(Session session, IDataSetId id)
    {
        IDataBO bo = businessObjectFactory.createDataBO(session);
        return bo.tryFindByDataSetId(id);
    }

    private SamplePE getSampleById(Session session, ISampleId id)
    {
        ISampleBO bo = businessObjectFactory.createSampleBO(session);
        return bo.tryFindBySampleId(id);
    }

    public ExperimentPE getExperimentById(Session session, IExperimentId id)
    {
        IExperimentBO bo = businessObjectFactory.createExperimentBO(session);
        return bo.tryFindByExperimentId(id);
    }

}
