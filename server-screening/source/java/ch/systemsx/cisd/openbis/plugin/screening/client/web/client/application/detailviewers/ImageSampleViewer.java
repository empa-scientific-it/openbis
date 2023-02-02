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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.GenericSampleViewer;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningViewContext;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;

/**
 * The sample detail viewer plugin for <i>screening</i> well and microscopy sample.
 * 
 * @author Tomasz Pylak
 */
public final class ImageSampleViewer extends GenericSampleViewer
{
    public static DatabaseModificationAwareComponent create(final ScreeningViewContext viewContext,
            final IIdAndCodeHolder identifiable, WellLocation wellLocationOrNull,
            boolean isWellSample)
    {
        ImageSampleViewer viewer =
                new ImageSampleViewer(viewContext, identifiable, wellLocationOrNull, isWellSample);
        viewer.reloadAllData();
        return new DatabaseModificationAwareComponent(viewer, viewer);
    }

    private final ScreeningViewContext screeningViewContext;

    private final WellLocation wellLocationOrNull;

    private final boolean isWell;

    private ImageSampleViewer(final ScreeningViewContext viewContext,
            final IIdAndCodeHolder identifiable, WellLocation wellLocationOrNull,
            boolean isWellSample)
    {
        super(viewContext, identifiable);
        this.screeningViewContext = viewContext;
        this.isWell = isWellSample;

        if (isWellSample)
        {
            this.wellLocationOrNull =
                    isWellSample ? WellLocation.tryParseLocationStr(getWellCode(identifiable))
                            : null;
        } else
        {
            this.wellLocationOrNull = wellLocationOrNull;
        }
    }

    private static String getWellCode(final IIdAndCodeHolder identifiable)
    {
        String code = identifiable.getCode();
        int colon = code.indexOf(":");
        if (colon != -1)
        {
            return code.substring(colon + 1);
        } else
        {
            return code;
        }
    }

    @Override
    protected void loadSampleGenerationInfo(TechId sampleTechId,
            AsyncCallback<SampleParentWithDerived> asyncCallback)
    {
        screeningViewContext.getService().getSampleGenerationInfo(sampleTechId, asyncCallback);
    }

    @Override
    protected List<TabContent> createAdditionalSectionPanels()
    {
        List<TabContent> sections = new ArrayList<TabContent>();

        sections.add(new ImageSampleSection(screeningViewContext, sampleId, wellLocationOrNull,
                isWell));
        return sections;
    }

    @Override
    protected String getDeleteButtonLabel()
    {
        return viewContext.getMessage(Dict.BUTTON_DELETE_WELL);
    }

}
