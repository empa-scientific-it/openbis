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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.material;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.TabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractDatabaseModificationObserverWithCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.CompositeDatabaseModificationObserverWithMainObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractViewerWithVerticalSplit;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.PropertyValueRenderers;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.EntityHistoryGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.PropertyGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.SectionsPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.WebAppContext;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.PropertiesPanelUtils;

/**
 * The <i>generic</i> material viewer.
 * 
 * @author Piotr Buczek
 */
abstract public class GenericMaterialViewer extends AbstractViewerWithVerticalSplit<Material>
        implements IDatabaseModificationObserver
{
    public static final String PROPERTIES_ID_PREFIX = GenericConstants.ID_PREFIX
            + "material-properties-section_";

    private static final String GENERIC_MATERIAL_VIEWER = "generic-material-viewer";

    private static final String PREFIX = GENERIC_MATERIAL_VIEWER + "_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    private PropertyGrid propertyGrid;

    protected final TechId materialId;

    public static DatabaseModificationAwareComponent create(
            final IViewContext<IGenericClientServiceAsync> viewContext, final TechId materialId)
    {

        final GenericMaterialViewer viewer = new GenericMaterialViewer(viewContext, materialId)
            {
                @Override
                protected void loadMaterialInfo(TechId materialTechId,
                        AsyncCallback<Material> material)
                {
                    viewContext.getCommonService().getMaterialInfo(materialTechId, material);
                }
            };
        viewer.reloadAllData();
        return new DatabaseModificationAwareComponent(viewer, viewer);
    }

    protected abstract void loadMaterialInfo(TechId materialTechId, AsyncCallback<Material> material);

    protected GenericMaterialViewer(final IViewContext<?> viewContext, final TechId materialId)
    {
        super(viewContext, createId(materialId));
        this.materialId = materialId;
        setLayout(new BorderLayout());
    }

    public static String createId(final TechId materialId)
    {
        return ID_PREFIX + materialId;
    }

    @Override
    public void updateOriginalData(Material newData)
    {
        super.updateOriginalData(newData);
    }

    private final Component createRightPanel(Material material)
    {
        final SectionsPanel container =
                new SectionsPanel(viewContext.getCommonViewContext(), ID_PREFIX + material.getId());
        container.setDisplayID(DisplayTypeIDGenerator.GENERIC_MATERIAL_VIEWER, displayIdSuffix);

        container.addSection(EntityHistoryGrid.createPropertiesHistorySection(viewContext,
                EntityKind.MATERIAL, new TechId(material.getId())));

        List<TabContent> additionalPanels = createAdditionalSectionPanels(material);
        for (TabContent panel : additionalPanels)
        {
            container.addSection(panel);
        }
        container.layout();
        attachManagedPropertiesSections(container, material);
        moduleSectionManager.initialize(container, material);
        attachWebAppsSections(container, material, WebAppContext.MATERIAL_DETAILS_VIEW);
        return container;
    }

    @Override
    protected void reloadAllData()
    {
        reloadMaterialData(new MaterialInfoCallback(viewContext, this));
    }

    /**
     * Load the {@link Material} information.
     */
    protected void reloadMaterialData(AbstractAsyncCallback<Material> material)
    {
        loadMaterialInfo(materialId, material);
    }

    /**
     * To be subclassed. Creates additional panels of the viewer in the right side section besides components, datasets and attachments
     */
    protected List<TabContent> createAdditionalSectionPanels(Material material)
    {
        return new ArrayList<TabContent>();
    }

    private final Component createLeftPanel(final Material material)
    {
        final ContentPanel panel = new ContentPanel();
        panel.setScrollMode(Scroll.AUTOY);
        panel.setHeading(viewContext.getMessage(Dict.MATERIAL_PROPERTIES_HEADING));
        propertyGrid = createPropertyGrid(material, viewContext);
        panel.add(propertyGrid);
        return panel;
    }

    public PropertyGrid createPropertyGrid(final Material material)
    {
        return createPropertyGrid(material, viewContext);
    }

    private static final PropertyGrid createPropertyGrid(Material material,
            final IViewContext<?> viewContext)
    {
        final Map<String, Object> properties = createProperties(viewContext, material);
        final PropertyGrid propertyGrid = new PropertyGrid(viewContext, properties.size());
        propertyGrid.registerPropertyValueRenderer(MaterialType.class,
                PropertyValueRenderers.createMaterialTypePropertyValueRenderer(viewContext));
        propertyGrid.setProperties(properties);
        propertyGrid.getElement().setId(PROPERTIES_ID_PREFIX + material.getIdentifier());
        return propertyGrid;
    }

    private static final Map<String, Object> createProperties(final IViewContext<?> viewContext,
            Material material)
    {
        final Map<String, Object> properties = new LinkedHashMap<String, Object>();
        final MaterialType materialType = material.getMaterialType();

        properties.put(viewContext.getMessage(Dict.MATERIAL), material.getCode());
        properties.put(viewContext.getMessage(Dict.MATERIAL_TYPE), materialType);
        properties.put(viewContext.getMessage(Dict.REGISTRATOR), material.getRegistrator());
        properties.put(viewContext.getMessage(Dict.REGISTRATION_DATE),
                material.getRegistrationDate());

        PropertiesPanelUtils.addMetaprojects(viewContext, properties, material.getMetaprojects());
        PropertiesPanelUtils.addEntityProperties(viewContext, properties, material.getProperties());

        return properties;
    }

    public final void updateProperties(final Material material)
    {
        final Map<String, Object> properties = createProperties(viewContext, material);
        propertyGrid.resizeRows(properties.size());
        propertyGrid.setProperties(properties);
    }

    private static final class MaterialInfoCallback extends AbstractAsyncCallback<Material>
    {
        private final GenericMaterialViewer viewer;

        private MaterialInfoCallback(final IViewContext<?> viewContext,
                final GenericMaterialViewer viewer)
        {
            super(viewContext);
            this.viewer = viewer;
        }

        @Override
        protected final void process(final Material result)
        {
            viewer.updateOriginalData(result);
            viewer.removeAll();
            // Left panel
            final Component leftPanel = viewer.createLeftPanel(result);
            viewer.add(leftPanel, viewer.createLeftBorderLayoutData());
            viewer.configureLeftPanel(leftPanel);
            // Right panel
            final Component rightPanel = viewer.createRightPanel(result);
            viewer.add(rightPanel, createRightBorderLayoutData());

            viewer.layout();
        }

    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return createDatabaseModificationObserver().getRelevantModifications();
    }

    @Override
    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        createDatabaseModificationObserver().update(observedModifications);
    }

    private IDatabaseModificationObserver createDatabaseModificationObserver()
    {
        return new CompositeDatabaseModificationObserverWithMainObserver(
                new PropertyGridDatabaseModificationObserver());
    }

    private class PropertyGridDatabaseModificationObserver extends
            AbstractDatabaseModificationObserverWithCallback
    {

        @Override
        public DatabaseModificationKind[] getRelevantModifications()
        {
            return new DatabaseModificationKind[]
            {
                    DatabaseModificationKind.edit(ObjectKind.MATERIAL),
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
            reloadMaterialData(new ReloadPropertyGridCallback(viewContext,
                    GenericMaterialViewer.this));
        }

        private final class ReloadPropertyGridCallback extends AbstractAsyncCallback<Material>
        {
            private final GenericMaterialViewer viewer;

            private ReloadPropertyGridCallback(final IViewContext<?> viewContext,
                    final GenericMaterialViewer viewer)
            {
                super(viewContext);
                this.viewer = viewer;
            }

            //
            // AbstractAsyncCallback
            //

            /** This method triggers reloading of the {@link PropertyGrid} data. */
            @Override
            protected final void process(final Material result)
            {
                viewer.updateOriginalData(result);
                viewer.updateProperties(result);
                executeSuccessfulUpdateCallback();
            }

            @Override
            public void finishOnFailure(Throwable caught)
            {
                viewer.setupRemovedEntityView();
            }
        }
    }

    @Override
    protected String getDeleteButtonLabel()
    {
        return viewContext.getMessage(Dict.BUTTON_DELETE_MATERIAL);
    }
}
