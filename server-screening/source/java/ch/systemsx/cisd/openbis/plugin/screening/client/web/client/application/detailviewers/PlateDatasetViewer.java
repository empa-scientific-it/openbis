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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.TabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.dataset.GenericDataSetViewer;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningViewContext;

/**
 * The <i>screening</i> plate dataset viewer.
 * 
 * @author Tomasz Pylak
 */
public final class PlateDatasetViewer extends GenericDataSetViewer
{
    public static DatabaseModificationAwareComponent create(final ScreeningViewContext viewContext,
            final IIdAndCodeHolder identifiable)
    {
        PlateDatasetViewer viewer = new PlateDatasetViewer(viewContext, identifiable);
        viewer.reloadAllData();
        return new DatabaseModificationAwareComponent(viewer, viewer);
    }

    private final ScreeningViewContext screeningViewContext;

    public PlateDatasetViewer(final ScreeningViewContext viewContext,
            final IIdAndCodeHolder identifiable)
    {
        super(viewContext, identifiable);
        this.screeningViewContext = viewContext;
    }

    @Override
    protected void loadDatasetInfo(TechId datasetTechId, AsyncCallback<AbstractExternalData> asyncCallback)
    {
        screeningViewContext.getService().getDataSetInfo(datasetTechId, asyncCallback);
    }

    @Override
    protected List<TabContent> createAdditionalSectionPanels(AbstractExternalData dataset)
    {
        List<TabContent> sections = new ArrayList<TabContent>();

        if (dataset.tryGetContainer() == null)
            sections.add(new PlateLayoutDatasetSection(screeningViewContext, datasetId));
        return sections;
    }
}
