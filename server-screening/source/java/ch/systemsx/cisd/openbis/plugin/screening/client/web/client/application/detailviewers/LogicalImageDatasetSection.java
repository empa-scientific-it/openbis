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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.TabContent;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningViewContext;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.dto.LogicalImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.LayoutUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.LogicalImageInfo;

/**
 * A section of dataset detail view for datasets containing microscopy images to present these images in a viewer.
 * 
 * @author Tomasz Pylak
 */
public class LogicalImageDatasetSection extends TabContent
{
    private final AbstractExternalData dataset;

    public LogicalImageDatasetSection(final ScreeningViewContext viewContext,
            final AbstractExternalData dataset)
    {
        super("Images", viewContext, dataset);
        this.dataset = dataset;
        setIds(DisplayTypeIDGenerator.LOGICAL_IMAGE_DATASET_SECTION);
    }

    private ScreeningViewContext getViewContext()
    {
        return (ScreeningViewContext) viewContext;
    }

    @Override
    protected void showContent()
    {
        final ScreeningViewContext context = getViewContext();
        add(new Text(context.getMessage(ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict.LOAD_IN_PROGRESS)));

        context.getService().getImageDatasetInfo(dataset.getCode(),
                dataset.getDataStore().getCode(), null, createDisplayCallback());
    }

    private AsyncCallback<LogicalImageInfo> createDisplayCallback()
    {
        return new AbstractAsyncCallback<LogicalImageInfo>(getViewContext())
            {
                @Override
                protected void process(LogicalImageInfo imageInfo)
                {
                    removeAll();
                    setLayout(new RowLayout());
                    setScrollMode(Scroll.AUTO);

                    Widget viewerWidget = createImageViewerWidget(imageInfo);
                    add(viewerWidget, LayoutUtils.createRowLayoutSurroundingData());

                    layout();
                }
            };
    }

    private Widget createImageViewerWidget(LogicalImageInfo imageInfo)
    {
        Experiment experiment = dataset.getExperiment();
        String experimentPermId = experiment.getPermId();
        String identifier = experiment.getIdentifier();
        LogicalImageReference logicalImageReference =
                new LogicalImageReference(dataset.getCode(), dataset.getDataStore().getCode(),
                        dataset.getDataStore().getHostUrl(), imageInfo.getImageParameters());
        LogicalImageViewer viewer =
                new LogicalImageViewer(logicalImageReference,
                        LogicalImageDatasetSection.this.getViewContext(), identifier, experimentPermId,
                        true);
        Widget viewerWidget = viewer.getViewerWidget(imageInfo.getChannelStacks(), "LogicalImageDatasetSection");
        return viewerWidget;
    }
}
