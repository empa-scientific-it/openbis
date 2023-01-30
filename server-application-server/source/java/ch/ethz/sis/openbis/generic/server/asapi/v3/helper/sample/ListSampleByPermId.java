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

import java.util.LinkedList;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.AbstractListObjectById;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
public class ListSampleByPermId extends AbstractListObjectById<SamplePermId, SamplePE>
{

    private ISampleDAO sampleDAO;

    public ListSampleByPermId(ISampleDAO sampleDAO)
    {
        this.sampleDAO = sampleDAO;
    }

    @Override
    public Class<SamplePermId> getIdClass()
    {
        return SamplePermId.class;
    }

    @Override
    public SamplePermId createId(SamplePE sample)
    {
        return new SamplePermId(sample.getPermId());
    }

    @Override
    public List<SamplePE> listByIds(IOperationContext context, List<SamplePermId> ids)
    {
        List<String> permIds = new LinkedList<String>();

        for (SamplePermId id : ids)
        {
            permIds.add(id.getPermId());
        }

        return sampleDAO.listByPermID(permIds);
    }

}
