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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.LabeledItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.SimpleModelComboBox;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageResolution;

/**
 * @author pkupczyk
 */
public class ResolutionChooser extends LayoutContainer
{

    private SimpleModelComboBox<ImageResolution> comboBox;

    public ResolutionChooser(final IViewContext<IScreeningClientServiceAsync> viewContext,
            List<ImageResolution> incomingResolutions, final ImageResolution defaultResolution)
    {

        List<ImageResolution> resolutions = prepareResolutions(incomingResolutions);
        List<LabeledItem<ImageResolution>> items = new ArrayList<LabeledItem<ImageResolution>>();

        if (resolutions.isEmpty())
        {
            items.add(new LabeledItem<ImageResolution>(null, viewContext.getMessage(Dict.RESOLUTION_CHOOSER_DEFAULT)));
        } else
        {
            for (ImageResolution resolution : resolutions)
            {
                String resolutionText =
                        viewContext.getMessage(Dict.RESOLUTION_CHOOSER_RESOLUTION,
                                resolution.getWidth(), resolution.getHeight());
                items.add(new LabeledItem<ImageResolution>(resolution, resolutionText));
            }
        }

        comboBox = new SimpleModelComboBox<ImageResolution>(viewContext, items, null);
        LabeledItem<ImageResolution> defaultResolutionItem = comboBox.findModelForVal(defaultResolution);

        if (defaultResolutionItem != null)
        {
            comboBox.setSelection(defaultResolutionItem);
        } else if (false == items.isEmpty())
        {
            comboBox.setSelection(items.get(0));
        }

        add(comboBox);
    }

    private List<ImageResolution> prepareResolutions(List<ImageResolution> resolutions)
    {
        Set<ImageResolution> allResolutions = new TreeSet<ImageResolution>(resolutions);
        maybeAddThumbnailResolution(allResolutions);
        return new ArrayList<ImageResolution>(allResolutions);
    }

    private void maybeAddThumbnailResolution(Collection<ImageResolution> resolutions)
    {
        boolean hasThumbnails = false;

        for (ImageResolution resolution : resolutions)
        {
            if (false == resolution.isOriginal())
            {
                hasThumbnails = true;
                break;
            }
        }

        if (false == hasThumbnails)
        {
            ImageResolution originalResolution = null;

            for (ImageResolution resolution : resolutions)
            {
                if (resolution.isOriginal())
                {
                    originalResolution = resolution;
                    break;
                }
            }

            if (originalResolution != null)
            {
                resolutions.add(new ImageResolution(Math.round(originalResolution.getWidth() / 4), Math.round(originalResolution.getHeight() / 4),
                        false));
            }
        }
    }

    public ImageResolution getResolution()
    {
        return comboBox.tryGetChosenItem();
    }

    public void addResolutionChangedListener(Listener<BaseEvent> listener)
    {
        comboBox.addListener(Events.SelectionChange, listener);
    }

}
