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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.logicalimage;

import com.extjs.gxt.ui.client.widget.LayoutContainer;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningDisplaySettingsManager;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningViewContext;

/**
 * @author pkupczyk
 */
public class LogicalImageSeriesControls extends LayoutContainer
{

    private IViewContext<IScreeningClientServiceAsync> viewContext;

    private String displayTypeId;

    private LogicalImageSeriesDownloader imageDownloader;

    private LogicalImageSeriesModel imageModel;

    public LogicalImageSeriesControls(IViewContext<IScreeningClientServiceAsync> viewContext,
            String displayTypeId, LogicalImageSeriesDownloader downloader,
            LogicalImageSeriesModel model)
    {
        this.viewContext = viewContext;
        this.displayTypeId = displayTypeId;
        this.imageDownloader = downloader;
        this.imageModel = model;
    }

    protected IViewContext<IScreeningClientServiceAsync> getViewContext()
    {
        return viewContext;
    }

    protected String getDisplayTypeId()
    {
        return displayTypeId;
    }

    protected LogicalImageSeriesDownloader getImageDownloader()
    {
        return imageDownloader;
    }

    protected LogicalImageSeriesModel getImageModel()
    {
        return imageModel;
    }

    protected ScreeningDisplaySettingsManager getSettingsManager()
    {
        return ScreeningViewContext.getTechnologySpecificDisplaySettingsManager(getViewContext());
    }

}
