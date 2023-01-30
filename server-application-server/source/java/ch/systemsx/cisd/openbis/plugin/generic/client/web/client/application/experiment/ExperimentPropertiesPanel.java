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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractDatabaseModificationObserverWithCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserverWithCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.PropertyValueRenderers;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.PropertyGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.ExternalHyperlink;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.PropertiesPanelUtils;

/**
 * {@link ContentPanel} containing experiment properties.
 * 
 * @author Izabela Adamczyk
 */
public class ExperimentPropertiesPanel extends ContentPanel
{
    public static final String PROPERTIES_ID_PREFIX = GenericConstants.ID_PREFIX
            + "experiment-properties-section_";

    private final TechId experimentId;

    private final PropertyGrid grid;

    private final IViewContext<IGenericClientServiceAsync> viewContext;

    private final GenericExperimentViewer viewer;

    public ExperimentPropertiesPanel(final Experiment experiment,
            final IViewContext<IGenericClientServiceAsync> viewContext,
            final GenericExperimentViewer viewer)
    {
        super(new BorderLayout());
        setHeading(viewContext.getMessage(Dict.EXPERIMENT) + " Properties");
        this.experimentId = new TechId(experiment);
        this.viewContext = viewContext;
        this.viewer = viewer;
        this.grid = createPropertyGrid(experiment);
        ContentPanel pp = new ContentPanel();
        pp.setHeaderVisible(false);
        pp.setScrollMode(Scroll.AUTOY);
        pp.add(grid);
        add(pp, new BorderLayoutData(LayoutRegion.CENTER));
    }

    public void addSouthComponent(Component component)
    {
        add(component, new BorderLayoutData(LayoutRegion.SOUTH));
        layout();
    }

    private PropertyGrid createPropertyGrid(Experiment experiment)
    {
        return createPropertyGrid(createProperties(experiment, viewContext));
    }

    private final PropertyGrid createPropertyGrid(Map<String, Object> properties)
    {
        final IMessageProvider messageProvider = viewContext;
        final PropertyGrid propertyGrid = new PropertyGrid(viewContext, properties.size());
        propertyGrid.getElement().setId(PROPERTIES_ID_PREFIX + experimentId);
        propertyGrid.registerPropertyValueRenderer(ExperimentType.class,
                PropertyValueRenderers.createExperimentTypePropertyValueRenderer(messageProvider));
        propertyGrid.setProperties(properties);
        return propertyGrid;
    }

    private static Map<String, Object> createProperties(Experiment experiment,
            IViewContext<?> viewContext)
    {
        final Map<String, Object> properties = new LinkedHashMap<String, Object>();
        final ExperimentType experimentType = experiment.getExperimentType();
        final Deletion deletion = experiment.getDeletion();
        properties.put(viewContext.getMessage(Dict.EXPERIMENT), experiment.getIdentifier());
        properties.put(viewContext.getMessage(Dict.PERM_ID),
                new ExternalHyperlink(experiment.getPermId(), experiment.getPermlink()));
        properties.put(viewContext.getMessage(Dict.EXPERIMENT_TYPE), experimentType);
        properties.put(viewContext.getMessage(Dict.REGISTRATOR), experiment.getRegistrator());
        properties.put(viewContext.getMessage(Dict.REGISTRATION_DATE),
                experiment.getRegistrationDate());
        if (deletion != null)
        {
            properties.put(viewContext.getMessage(Dict.DELETION), deletion);
        }
        properties.put(viewContext.getMessage(Dict.PROJECT), experiment.getProject());

        PropertiesPanelUtils.addMetaprojects(viewContext, properties, experiment.getMetaprojects());
        PropertiesPanelUtils.addEntityProperties(viewContext, properties,
                experiment.getProperties());

        return properties;
    }

    //
    // auto-refresh
    //

    private final void updateProperties(Experiment experiment)
    {
        final Map<String, Object> properties = createProperties(experiment, viewContext);
        grid.resizeRows(properties.size());
        grid.setProperties(properties);
    }

    private void reloadData(AbstractAsyncCallback<Experiment> callback)
    {
        viewContext.getCommonService().getExperimentInfo(experimentId, callback);
    }

    public IDatabaseModificationObserverWithCallback getDatabaseModificationObserver()
    {
        return new PropertyGridDatabaseModificationObserver();
    }

    private class PropertyGridDatabaseModificationObserver extends
            AbstractDatabaseModificationObserverWithCallback
    {
        @Override
        public DatabaseModificationKind[] getRelevantModifications()
        {
            return new DatabaseModificationKind[]
            {
                    DatabaseModificationKind.edit(ObjectKind.EXPERIMENT),
                    DatabaseModificationKind.createOrDelete(ObjectKind.EXPERIMENT),
                    DatabaseModificationKind
                            .createOrDelete(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                    DatabaseModificationKind.edit(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                    DatabaseModificationKind.createOrDelete(ObjectKind.VOCABULARY_TERM),
                    DatabaseModificationKind.edit(ObjectKind.VOCABULARY_TERM),
                    DatabaseModificationKind.createOrDelete(ObjectKind.METAPROJECT),
                    DatabaseModificationKind.edit(ObjectKind.METAPROJECT) };
        }

        @Override
        public void update(Set<DatabaseModificationKind> observedModifications)
        {
            reloadData(new ExperimentInfoCallback(viewContext, ExperimentPropertiesPanel.this));
        }

        private final class ExperimentInfoCallback extends AbstractAsyncCallback<Experiment>
        {
            private final ExperimentPropertiesPanel section;

            private ExperimentInfoCallback(final IViewContext<?> viewContext,
                    final ExperimentPropertiesPanel section)
            {
                super(viewContext);
                this.section = section;
            }

            //
            // AbstractAsyncCallback
            //

            /** This method triggers reloading of the {@link ExperimentPropertiesPanel} data. */
            @Override
            protected final void process(final Experiment result)
            {
                viewer.updateOriginalData(result);
                section.updateProperties(result);
                executeSuccessfulUpdateCallback();
            }

            @Override
            public void finishOnFailure(Throwable caught)
            {
                viewer.setupRemovedEntityView();
            }
        }
    }

}
