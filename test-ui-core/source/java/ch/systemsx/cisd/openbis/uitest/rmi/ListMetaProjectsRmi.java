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

package ch.systemsx.cisd.openbis.uitest.rmi;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.uitest.dsl.Command;
import ch.systemsx.cisd.openbis.uitest.dsl.Inject;
import ch.systemsx.cisd.openbis.uitest.rmi.eager.MetaProjectRmi;
import ch.systemsx.cisd.openbis.uitest.type.MetaProject;

/**
 * @author anttil
 */
public class ListMetaProjectsRmi implements Command<List<MetaProject>>
{
    @Inject
    private String session;

    @Inject
    private IGeneralInformationService generalInformationService;

    @Override
    public List<MetaProject> execute()
    {
        List<MetaProject> result = new ArrayList<MetaProject>();
        for (Metaproject m : generalInformationService.listMetaprojects(session))
        {
            result.add(new MetaProjectRmi(m));
        }
        return result;
    }
}
