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

package ch.systemsx.cisd.openbis.systemtest.base.matcher;

import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;

public class ExternalDataHasContainerMatcher extends TypeSafeMatcher<AbstractExternalData>
{

    private AbstractExternalData expectedContainer;

    private Integer expectedOrder;

    public ExternalDataHasContainerMatcher(AbstractExternalData expected)
    {
        this.expectedContainer = expected;
    }

    public ExternalDataHasContainerMatcher orderInContainer(int order)
    {
        this.expectedOrder = order;
        return this;
    }

    @Override
    public void describeTo(Description description)
    {
        description.appendText("A dataset with container " + expectedContainer);
    }

    @Override
    public boolean matchesSafely(AbstractExternalData actual)
    {
        List<ContainerDataSet> containerDataSets = actual.getContainerDataSets();
        for (ContainerDataSet container : containerDataSets)
        {
            String containerCode = container.getCode();
            if (expectedContainer.getCode().equals(containerCode))
            {
                Integer order = actual.getOrderInContainer(containerCode);
                return expectedOrder == null || expectedOrder.equals(order);
            }
        }
        return false;
    }
}