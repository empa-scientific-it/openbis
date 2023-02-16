/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * {@link IEntityDataLoader} for samples.
 * 
 * @author Izabela Adamczyk
 */
class SampleDataLoader implements IEntityDataLoader
{

    private final ISampleDAO dao;

    public SampleDataLoader(ISampleDAO dao)
    {
        this.dao = dao;
    }

    @Override
    public Map<String, SpacePE> loadGroups(Set<String> keys)
    {
        Map<String, SpacePE> map = new HashMap<String, SpacePE>();
        List<SamplePE> samples = dao.listByPermID(keys);
        for (SamplePE s : samples)
        {
            map.put(s.getPermId(), s.getSpace());
        }
        return map;
    }

    @Override
    public Map<String, ProjectPE> loadProjects(Set<String> keys)
    {
        Map<String, ProjectPE> map = new HashMap<String, ProjectPE>();
        List<SamplePE> samples = dao.listByPermID(keys);
        for (SamplePE s : samples)
        {
            ProjectPE project = null;

            if (s.getExperiment() != null)
            {
                project = s.getExperiment().getProject();
            } else if (s.getProject() != null)
            {
                project = s.getProject();
            }

            map.put(s.getPermId(), project);
        }
        return map;
    }

}