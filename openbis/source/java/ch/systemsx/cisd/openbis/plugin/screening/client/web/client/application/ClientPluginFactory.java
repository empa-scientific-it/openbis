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

import java.util.Collections;
import java.util.Set;

import com.extjs.gxt.ui.client.widget.Component;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DummyComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ViewerTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.ClientPluginAdapter;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.SampleTypeCode;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;

/**
 * {@link IClientPluginFactory} implementation for <i>Screening</i> technology.
 * <p>
 * Currently, this implementation only runs for a sample of type {@link SampleTypeCode#CELL_PLATE}.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class ClientPluginFactory extends
        AbstractClientPluginFactory<IScreeningClientServiceAsync>
{

    public ClientPluginFactory(final IViewContext<ICommonClientServiceAsync> originalViewContext)
    {
        super(originalViewContext);
    }

    //
    // AbstractClientPluginFactory
    //

    @Override
    protected final IViewContext<IScreeningClientServiceAsync> createViewContext(
            final IViewContext<ICommonClientServiceAsync> originalViewContext)
    {
        return new ScreeningViewContext(originalViewContext);
    }

    //
    // IClientPluginFactory
    //

    public final Set<String> getEntityTypeCodes(final EntityKind entityKind)
    {
        if (entityKind == EntityKind.SAMPLE)
        {
            return Collections.singleton(SampleTypeCode.MASTER_PLATE.getCode());
        }
        return Collections.emptySet();
    }

    @SuppressWarnings("unchecked")
    public <T extends EntityType, I extends IIdentifierHolder> IClientPlugin<T, I> createClientPlugin(
            final EntityKind entityKind)
    {
        if (EntityKind.EXPERIMENT.equals(entityKind))
        {
            return (IClientPlugin<T, I>) new ExperimentClientPlugin();
        }
        if (EntityKind.SAMPLE.equals(entityKind))
        {
            return (IClientPlugin<T, I>) new SampleClientPlugin();
        }
        throw new UnsupportedOperationException("IClientPlugin for entity kind '" + entityKind
                + "' not implemented yet.");
    }

    //
    // Helper classes
    //

    private final class SampleClientPlugin implements IClientPlugin<SampleType, Sample>
    {
        //
        // IViewClientPlugin
        //

        public final ITabItemFactory createEntityViewer(final Sample sample)
        {
            final String sampleIdentifier = sample.getIdentifier();
            return new ITabItemFactory()
                {
                    public ITabItem create()
                    {
                        final ScreeningSampleViewer sampleViewer =
                                new ScreeningSampleViewer(getViewContext(), sampleIdentifier);
                        return new ViewerTabItem(sampleIdentifier, sampleViewer);
                    }

                    public String getId()
                    {
                        return ScreeningSampleViewer.createId(sampleIdentifier);
                    }

                    public boolean isCloseConfirmationNeeded()
                    {
                        return false;
                    }
                };
        }

        public final Widget createRegistrationForEntityType(final SampleType sampleTypeCode)
        {
            return new DummyComponent();
        }

        public final Widget createBatchRegistrationForEntityType(final SampleType sampleType)
        {
            return new DummyComponent();
        }
    }

    private final static class ExperimentClientPlugin extends
            ClientPluginAdapter<ExperimentType, Experiment>
    {

        //
        // IViewClientPlugin
        //

        @Override
        public final ITabItemFactory createEntityViewer(final Experiment identifier)
        {
            return new ITabItemFactory()
                {
                    public ITabItem create()
                    {
                        Component component = new DummyComponent();
                        return new DefaultTabItem(identifier.getIdentifier(), component);
                    }

                    public String getId()
                    {
                        return DummyComponent.ID;
                    }

                    public boolean isCloseConfirmationNeeded()
                    {
                        return false;
                    }
                };
        }
    }
}
