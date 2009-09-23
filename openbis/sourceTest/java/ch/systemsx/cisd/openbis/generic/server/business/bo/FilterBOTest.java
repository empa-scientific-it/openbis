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

import static ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool.EXAMPLE_SESSION;

import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Filter;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IFilterUpdates;
import ch.systemsx.cisd.openbis.generic.shared.dto.FilterPE;

/**
 * @author Piotr Buczek
 */
public class FilterBOTest extends AbstractBOTest
{

    private static final String NAME = "name";

    private static final String GRID_ID = "gridId";

    private static final String DESCRIPTION = "description";

    private static final String EXPRESSION = "expression";

    private static final boolean IS_PUBLIC = true;

    private static final TechId TECH_ID = new TechId(42l);

    @Test
    public void testUpdate()
    {
        final FilterPE filter = createFilter();
        final IFilterUpdates updates = createFilterUpdates(filter);
        context.checking(new Expectations()
            {
                {
                    one(filterDAO).getByTechId(TECH_ID);
                    will(returnValue(filter));

                    filter.setName(updates.getName());
                    filter.setDescription(updates.getDescription());
                    filter.setExpression(updates.getExpression());
                    filter.setPublic(updates.isPublic());

                    one(filterDAO).validateAndSaveUpdatedEntity(filter);
                }
            });

        IFilterBO filterBO = createFilterBO();
        filterBO.update(updates);

        context.assertIsSatisfied();
    }

    private FilterPE createFilter()
    {
        final FilterPE filter = new FilterPE();
        filter.setId(TECH_ID.getId());
        filter.setGridId(GRID_ID);
        filter.setName(NAME);
        filter.setPublic(IS_PUBLIC);
        filter.setDescription(DESCRIPTION);
        filter.setExpression(EXPRESSION);
        return filter;
    }

    private IFilterUpdates createFilterUpdates(final FilterPE filter)
    {
        final Filter updates = new Filter();
        updates.setId(filter.getId());
        updates.setName(filter.getName() + " modified");
        updates.setDescription(filter.getDescription() + " modified");
        updates.setExpression(filter.getExpression() + " modified");
        updates.setPublic(filter.isPublic() == false);
        return updates;
    }

    private final IFilterBO createFilterBO()
    {
        return new FilterBO(daoFactory, EXAMPLE_SESSION);
    }

}
