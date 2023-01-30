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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.DisposableTabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.TabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * {@link TabContent} containing samples with specified child sample.
 * 
 * @author Piotr Buczek
 */
public class ParentSamplesSection extends DisposableTabContent
{
    private static final String PREFIX = "parent-samples-section_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    private final Sample child;

    public ParentSamplesSection(final IViewContext<?> viewContext, final Sample child)
    {
        super(viewContext.getMessage(Dict.PARENT_SAMPLES_HEADING), viewContext, child);
        this.child = child;
        setIds(DisplayTypeIDGenerator.PARENT_SAMPLES_SECTION);
    }

    // @Private
    static String createGridId(TechId childId)
    {
        return SampleBrowserGrid.createGridId(createBrowserId(childId));
    }

    private static String createBrowserId(TechId childId)
    {
        return ID_PREFIX + childId + "-browser";
    }

    @Override
    protected IDisposableComponent createDisposableContent()
    {
        TechId childId = TechId.create(child);
        return SampleBrowserGrid.createGridForParentSamples(viewContext.getCommonViewContext(),
                childId, createBrowserId(childId), child.getSampleType());
    }

}
