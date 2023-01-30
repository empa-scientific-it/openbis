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

package ch.systemsx.cisd.openbis.plugin.generic.server;

import org.springframework.stereotype.Component;

import ch.systemsx.cisd.openbis.generic.server.business.bo.IDataBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IDataSetTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExperimentBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExperimentTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IMaterialBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IMaterialTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IProjectBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ITrashBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.materiallister.IMaterialLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.materiallister.MaterialLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.SampleLister;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.AbstractPluginBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.generic.shared.ResourceNames;

/**
 * The unique {@link IGenericBusinessObjectFactory} implementation.
 * 
 * @author Christian Ribeaud
 */
@Component(ResourceNames.GENERIC_BUSINESS_OBJECT_FACTORY)
public final class GenericBusinessObjectFactory extends AbstractPluginBusinessObjectFactory
        implements IGenericBusinessObjectFactory
{

    //
    // IGenericBusinessObjectFactory
    //

    @Override
    public final ISampleBO createSampleBO(final Session session)
    {
        return getCommonBusinessObjectFactory().createSampleBO(session);
    }

    @Override
    public IExperimentBO createExperimentBO(final Session session)
    {
        return getCommonBusinessObjectFactory().createExperimentBO(session);
    }

    @Override
    public IExperimentTable createExperimentTable(final Session session)
    {
        return getCommonBusinessObjectFactory().createExperimentTable(session);
    }

    @Override
    public IMaterialBO createMaterialBO(final Session session)
    {
        return getCommonBusinessObjectFactory().createMaterialBO(session);
    }

    @Override
    public final ISampleTable createSampleTable(final Session session)
    {
        return getCommonBusinessObjectFactory().createSampleTable(session);
    }

    @Override
    public IMaterialTable createMaterialTable(Session session)
    {
        return getCommonBusinessObjectFactory().createMaterialTable(session);
    }

    @Override
    public IDataBO createDataBO(Session session)
    {
        return getCommonBusinessObjectFactory().createDataBO(session);
    }

    @Override
    public IDataSetTable createDataSetTable(Session session)
    {
        return getCommonBusinessObjectFactory().createDataSetTable(session);
    }

    @Override
    public IProjectBO createProjectBO(Session session)
    {
        return getCommonBusinessObjectFactory().createProjectBO(session);
    }

    @Override
    public ITrashBO createTrashBO(Session session)
    {
        return getCommonBusinessObjectFactory().createTrashBO(session);
    }

    @Override
    public ISampleLister createSampleLister(Session session)
    {
        return SampleLister.create(getDaoFactory(), session.getBaseIndexURL(), session
                .tryGetPerson().getId());
    }

    @Override
    public IMaterialLister createMaterialLister(Session session)
    {
        return MaterialLister.create(getDaoFactory(), session.getBaseIndexURL(), session
                .tryGetPerson().getId());
    }

}
