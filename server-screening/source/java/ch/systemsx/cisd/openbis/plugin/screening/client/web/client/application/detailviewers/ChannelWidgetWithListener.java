/*
 * Copyright 2010 ETH Zuerich, CISD
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

import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.utils.GuiUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.IntensityRange;

/**
 * Allows to create a {@link Widget} ({@link #asWidget()}) containing channel view and allows to manually update visible channel via the (
 * {@link #selectionChanged(List, String, IntensityRange)}) method.
 * 
 * @author Izabela Adamczyk
 */
public class ChannelWidgetWithListener implements ChannelChooserPanel.ChannelSelectionListener
{
    final private LayoutContainer container;

    final private ISimpleChanneledViewerFactory viewerFactory;

    interface ISimpleChanneledViewerFactory
    {
        Widget create(List<String> channelCodes, String imageTransformationCodeOrNull,
                Map<String, IntensityRange> rangesOrNull);
    }

    public ChannelWidgetWithListener(final ISimpleChanneledViewerFactory viewerFactory)
    {
        this.container = new LayoutContainer();
        this.viewerFactory = viewerFactory;
    }

    public Widget asWidget()
    {
        return container;
    }

    @Override
    public void selectionChanged(List<String> channelNames, String imageTransformationCodeOrNull,
            Map<String, IntensityRange> rangesOrNull)
    {
        if (channelNames != null)
        {
            GuiUtils.replaceLastItem(container,
                    viewerFactory.create(channelNames, imageTransformationCodeOrNull, rangesOrNull));
        }
    }
}