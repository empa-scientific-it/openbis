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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application;

import java.util.Collections;
import java.util.Set;

import com.extjs.gxt.ui.client.mvc.Dispatcher;

import ch.systemsx.cisd.openbis.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ISampleViewClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGeneration;

/**
 * {@link IClientPluginFactory} implementation for <i>Generic</i> technology.
 * 
 * @author Christian Ribeaud
 */
public final class ClientPluginFactory extends
        AbstractClientPluginFactory<IGenericClientServiceAsync>
{
    private ISampleViewClientPlugin sampleViewClientPlugin;

    public ClientPluginFactory(final IViewContext<IGenericClientServiceAsync> originalViewContext)
    {
        super(originalViewContext);
    }

    //
    // AbstractClientPluginFactory
    //

    @Override
    protected final IViewContext<IGenericClientServiceAsync> createViewContext(
            final IViewContext<IGenericClientServiceAsync> originalViewContext)
    {
        return originalViewContext;
    }

    //
    // IClientPluginFactory
    //

    public final ISampleViewClientPlugin createViewClientForSampleType(final String sampleTypeCode)
    {
        if (sampleViewClientPlugin == null)
        {
            sampleViewClientPlugin = new SampleViewClientPlugin();
        }
        return sampleViewClientPlugin;
    }

    public final Set<String> getSampleTypeCodes()
    {
        return Collections.singleton(SampleTypeCode.CONTROL_LAYOUT.getCode());
    }

    //
    // Helper classes
    //

    private final class SampleViewClientPlugin implements ISampleViewClientPlugin
    {

        SampleGenerationInfoCallback sampleInfoCallback;

        //
        // ISampleViewClientPlugin
        //

        public final void viewSample(final String sampleIdentifier)
        {
            final IViewContext<IGenericClientServiceAsync> viewContext = getViewContext();
            viewContext.getService().getSampleInfo(sampleIdentifier,
                    new SampleGenerationInfoCallback(viewContext));
        }
    }

    private final static class SampleGenerationInfoCallback extends
            AbstractAsyncCallback<SampleGeneration>
    {
        private SampleGenerationInfoCallback(
                final IViewContext<IGenericClientServiceAsync> viewContext)
        {
            super(viewContext);
        }

        //
        // AbstractAsyncCallback
        //

        @SuppressWarnings("unchecked")
        @Override
        protected final void process(final SampleGeneration result)
        {
            final String title = result.getGenerator().getCode();
            final GenericSampleViewer sampleViewer =
                    new GenericSampleViewer((IViewContext<IGenericClientServiceAsync>) viewContext,
                            result);
            Dispatcher.get().dispatch(
                    DispatcherHelper.createNaviEvent(new DefaultTabItem(title, sampleViewer)
                        {

                            //
                            // DefaultTabItem
                            //

                            @Override
                            public final void afterAddTabItem()
                            {
                                sampleViewer.loadStores();
                            }
                        }));
        }
    }
}
