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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sample;

import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.AbstractListObjectById;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author Franz-Josef Elmer
 */
// TODO: neds unit tests
public class ListSampleByIdentifier extends AbstractListObjectById<SampleIdentifier, SamplePE>
{

    private ISampleDAO sampleDAO;

    private ListSampleTechIdByIdentifier techIdByIdentifier;

    public ListSampleByIdentifier(ISampleDAO sampleDAO, SpacePE homeSpaceOrNull)
    {
        this.sampleDAO = sampleDAO;
        String homeSpaceCodeOrNull = homeSpaceOrNull == null ? null : homeSpaceOrNull.getCode();
        techIdByIdentifier = new ListSampleTechIdByIdentifier(homeSpaceCodeOrNull);
    }

    @Override
    public Class<SampleIdentifier> getIdClass()
    {
        return SampleIdentifier.class;
    }

    @Override
    public SampleIdentifier createId(SamplePE sample)
    {
        return techIdByIdentifier.createId(sample.getId());
    }

    @Override
    public List<SamplePE> listByIds(IOperationContext context, List<SampleIdentifier> ids)
    {
        return sampleDAO.listByIDs(techIdByIdentifier.listByIds(context, ids));
    }

}
