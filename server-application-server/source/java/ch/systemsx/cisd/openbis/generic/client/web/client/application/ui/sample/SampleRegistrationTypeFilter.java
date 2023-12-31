/*
 * Copyright ETH 2013 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample;

import java.util.Iterator;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * @author pkupczyk
 */
public class SampleRegistrationTypeFilter
{

    private String filterPattern;

    private boolean showParentsFlagIgnored;

    public SampleRegistrationTypeFilter(String pattern, boolean showParentsFlagIgnored)
    {
        filterPattern = pattern;
        this.showParentsFlagIgnored = showParentsFlagIgnored;

    }

    public void filter(List<SampleType> sampleTypes)
    {
        for (Iterator<SampleType> iterator = sampleTypes.iterator(); iterator.hasNext();)
        {
            SampleType type = iterator.next();
            if (pass(type) == false)
            {
                iterator.remove();
            }
        }
    }

    private boolean pass(SampleType sampleType)
    {
        if (filterPattern != null && sampleType.getCode().matches(filterPattern) == false)
        {
            return false;
        }
        return showParentsFlagIgnored || sampleType.isShowParents();
    }

}
