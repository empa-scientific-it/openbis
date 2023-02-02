/*
 * Copyright 2011 ETH Zuerich, CISD
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
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;

/**
 * @author Pawel Glyzewski
 */
public class ImageDataSetViewer extends GenericDataSetViewer
{
    public static DatabaseModificationAwareComponent create(final ScreeningViewContext viewContext,
            final IIdAndCodeHolder identifiable, WellLocation wellLocationOrNull)
    {
        ImageDataSetViewer viewer =
                new ImageDataSetViewer(viewContext, identifiable, wellLocationOrNull);
        viewer.reloadAllData();
        return new DatabaseModificationAwareComponent(viewer, viewer);
    }

    private final ScreeningViewContext screeningViewContext;

    private final WellLocation wellLocationOrNull;

    protected ImageDataSetViewer(ScreeningViewContext screeningViewContext,
            IIdAndCodeHolder identifiable, WellLocation wellLocationOrNull)
    {
        super(screeningViewContext, identifiable);

        this.screeningViewContext = screeningViewContext;
        this.wellLocationOrNull = wellLocationOrNull;
    }

    @Override
    protected List<TabContent> createAdditionalSectionPanels(AbstractExternalData dataset)
    {
        List<TabContent> sections = new ArrayList<TabContent>();

        sections.add(new ImageDataSetSection(screeningViewContext, dataset, wellLocationOrNull));
        return sections;
    }

    @Override
    protected void loadDatasetInfo(TechId datasetTechId, AsyncCallback<AbstractExternalData> asyncCallback)
    {
        screeningViewContext.getService().getDataSetInfo(datasetTechId, asyncCallback);
    }
}
