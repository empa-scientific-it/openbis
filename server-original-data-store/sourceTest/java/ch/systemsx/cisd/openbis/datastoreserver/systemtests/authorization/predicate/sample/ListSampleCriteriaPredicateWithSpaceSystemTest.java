/*
 * Copyright 2017 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.sample;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
public class ListSampleCriteriaPredicateWithSpaceSystemTest extends ListSampleCriteriaPredicateSystemTest
{

    @Override
    protected ListSampleCriteria createNonexistentObject(Object param)
    {
        ListSampleCriteria criteria = new ListSampleCriteria();
        criteria.setSpaceCode("IDONTEXIST");
        criteria.setIncludeSpace(true);
        return criteria;
    }

    @Override
    protected ListSampleCriteria createObject(SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        ListSampleCriteria criteria = new ListSampleCriteria();
        criteria.setSpaceCode(spacePE.getCode());
        criteria.setIncludeSpace(true);
        return criteria;
    }

}
