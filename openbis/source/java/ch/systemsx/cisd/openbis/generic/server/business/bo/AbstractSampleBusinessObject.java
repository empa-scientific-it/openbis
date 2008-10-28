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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import ch.systemsx.cisd.openbis.generic.server.business.bo.util.SampleOwner;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.SampleOwnerFinder;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * An <i>abstract</i> {@link AbstractBusinessObject} extension for {@link SamplePE}.
 * 
 * @author Christian Ribeaud
 */
abstract class AbstractSampleBusinessObject extends AbstractBusinessObject
{
    private final SampleOwnerFinder sampleOwnerFinder;

    AbstractSampleBusinessObject(final IDAOFactory daoFactory, final Session session)
    {
        super(daoFactory, session);
        sampleOwnerFinder = new SampleOwnerFinder(daoFactory, findRegistrator());

    }

    final SampleOwnerFinder getSampleOwnerFinder()
    {
        return sampleOwnerFinder;
    }

    final SamplePE getSampleByIdentifier(final SampleIdentifier identifier)
    {
        assert identifier != null : "Sample identifier unspecified.";
        final SampleOwner sampleOwner = sampleOwnerFinder.figureSampleOwner(identifier);
        final String sampleCode = identifier.getSampleCode();
        final ISampleDAO sampleDAO = getSampleDAO();
        if (sampleOwner.isDatabaseInstanceLevel())
        {
            return sampleDAO.tryFindByCodeAndDatabaseInstance(sampleCode, sampleOwner
                    .tryGetDatabaseInstance());
        } else
        {
            assert sampleOwner.isGroupLevel() : "Must be of group level.";
            return sampleDAO.tryFindByCodeAndGroup(sampleCode, sampleOwner.tryGetGroup());
        }
    }

}