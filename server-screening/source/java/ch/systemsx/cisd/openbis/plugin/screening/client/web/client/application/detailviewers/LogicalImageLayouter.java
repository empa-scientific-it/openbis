/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningViewContext;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.ImagingDatasetGuiUtils.IDatasetImagesReferenceUpdater;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.dto.LogicalImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetEnrichedReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.LogicalImageInfo;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;

/**
 * @author Tomasz Pylak
 */
class LogicalImageLayouter extends LayoutContainer implements IDatasetImagesReferenceUpdater
{
    private final ScreeningViewContext viewContext;

    private final WellLocation wellLocationOrNull;

    private final Map<ImageDatasetEnrichedReference, LogicalImageInfo> refsMap;

    public LogicalImageLayouter(ScreeningViewContext viewContext, WellLocation wellLocationOrNull,
            List<LogicalImageInfo> images)
    {
        this.viewContext = viewContext;
        this.wellLocationOrNull = wellLocationOrNull;
        this.refsMap = createRefsMap(images);
    }

    @Override
    public void changeDisplayedImageDataset(ImageDatasetEnrichedReference dataset)
    {
        LogicalImageInfo imageInfo = refsMap.get(dataset);
        assert imageInfo != null : "cannot find logical image for " + dataset;

        removeAll();
        Widget viewerWidget = createImageViewer(imageInfo);
        add(viewerWidget);
        layout();
    }

    private Widget createImageViewer(LogicalImageInfo imageInfo)
    {
        LogicalImageReference logicalImageReference =
                new LogicalImageReference(imageInfo.getImageDataset(), wellLocationOrNull);
        LogicalImageViewer viewer =
                new LogicalImageViewer(logicalImageReference, viewContext,
                        imageInfo.getExperimentIdentifier(), imageInfo.getExperimentPermId(), true);
        return viewer.getViewerWidget("LogicalImageLayouter");
    }

    public List<ImageDatasetEnrichedReference> getDatasetImagesReferences()
    {
        return new ArrayList<ImageDatasetEnrichedReference>(refsMap.keySet());
    }

    private static Map<ImageDatasetEnrichedReference, LogicalImageInfo> createRefsMap(
            List<LogicalImageInfo> images)
    {
        Map<ImageDatasetEnrichedReference, LogicalImageInfo> map =
                new HashMap<ImageDatasetEnrichedReference, LogicalImageInfo>();
        for (LogicalImageInfo imageInfo : images)
        {
            ImageDatasetEnrichedReference ref = imageInfo.getImageDataset();
            map.put(ref, imageInfo);
        }
        return map;
    }
}
