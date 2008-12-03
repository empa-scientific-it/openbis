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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGeneration;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.GenericSampleViewer;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;

/**
 * The <i>screening</i> sample viewer.
 * 
 * @author Christian Ribeaud
 */
public final class ScreeningSampleViewer extends LayoutContainer
{
    private static final String PREFIX = "screening-sample-viewer_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    private final IViewContext<IScreeningClientServiceAsync> viewContext;

    private final String sampleIdentifier;

    public ScreeningSampleViewer(final IViewContext<IScreeningClientServiceAsync> viewContext,
            final String sampleIdentifier)
    {
        setId(ID_PREFIX + sampleIdentifier);
        this.viewContext = viewContext;
        this.sampleIdentifier = sampleIdentifier;
    }

    private final Widget createUI(final SampleGeneration sampleGeneration)
    {
        final IMessageProvider messageProvider = viewContext.getMessageProvider();
        return GenericSampleViewer.createPropertyGrid(sampleIdentifier, sampleGeneration, messageProvider);
    }

    /**
     * Load the sample information.
     */
    public final void loadSampleInfo()
    {
        SampleInfoCallback callback = new SampleInfoCallback(viewContext, this);
        viewContext.getService().getSampleInfo(sampleIdentifier, callback);
    }

    //
    // Helper classes
    //

    public final static class SampleInfoCallback extends AbstractAsyncCallback<SampleGeneration>
    {
        private final ScreeningSampleViewer screeningSampleViewer;

        private SampleInfoCallback(final IViewContext<IScreeningClientServiceAsync> viewContext,
                ScreeningSampleViewer screeningSampleViewer)
        {
            super(viewContext);
            this.screeningSampleViewer = screeningSampleViewer;
        }

        //
        // AbstractAsyncCallback
        //

        /**
         * Sets the {@link SampleGeneration} for this <var>generic</var> sample viewer.
         * <p>
         * This method triggers the whole <i>GUI</i> construction.
         * </p>
         */
        @Override
        protected final void process(final SampleGeneration result)
        {
            screeningSampleViewer.removeAll();
            screeningSampleViewer.add(screeningSampleViewer.createUI(result));
            screeningSampleViewer.layout();
        }
    }
}