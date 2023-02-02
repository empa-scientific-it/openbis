/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithProperties;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * Utility methods to support the building of material-related UI components.
 * 
 * @author Tomasz Pylak
 */
public class MaterialComponentUtils
{
    private static final String MATERIAL_ID_DICT_MSG = "Id";

    /** @return the best short description of the material together with its type. */
    public static String getMaterialFullName(Material material, boolean captalizeFirstLetter)
    {
        return MaterialComponentUtils.getMaterialTypeAsTitle(material, captalizeFirstLetter) + " "
                + MaterialComponentUtils.getMaterialName(material);
    }

    /** @return the best short description of the material. */
    public static String getMaterialName(Material material)
    {
        if (material.getEntityType().getCode()
                .equalsIgnoreCase(ScreeningConstants.GENE_PLUGIN_TYPE_CODE))
        {
            String geneSymbol =
                    PropertiesUtil.tryFindProperty(material, ScreeningConstants.GENE_SYMBOLS);
            if (geneSymbol != null)
            {
                return geneSymbol;
            }
        }
        return material.getCode();
    }

    /**
     * @return the material code as title
     */
    public static String getMaterialTypeAsTitle(Material material, boolean captalizeFirstLetter)
    {
        String materialTypeCode = material.getMaterialType().getCode();

        return EntityTypeLabelUtils.formatAsTitle(materialTypeCode, captalizeFirstLetter);
    }

    /** Creates a grid with some header on top containing the specified title and entity properties. */
    public static IDisposableComponent createExperimentViewer(
            IViewContext<IScreeningClientServiceAsync> viewContext,
            IEntityInformationHolderWithProperties experiment, String headingText,
            IDisposableComponent gridComponent)
    {
        return createViewer(viewContext, experiment, headingText, gridComponent, null);
    }

    /** Creates a grid with some header on top containing the specified title and entity properties. */
    public static IDisposableComponent createMaterialViewer(
            IViewContext<IScreeningClientServiceAsync> viewContext,
            IEntityInformationHolderWithProperties material, String headingText,
            IDisposableComponent gridComponent)
    {
        Map<String, String> additionalProperties = createAdditionalMaterialProperties(material);
        return createViewer(viewContext, material, headingText, gridComponent,
                additionalProperties, getExcludedMatrialProperties());
    }

    public static String[] getExcludedMatrialProperties()
    {
        return new String[]
        { ScreeningConstants.GENE_SYMBOLS };
    }

    public static Map<String, String> createAdditionalMaterialProperties(
            IEntityInformationHolderWithProperties material)
    {
        Map<String, String> additionalProperties = new HashMap<String, String>();
        additionalProperties.put(MATERIAL_ID_DICT_MSG, material.getCode());
        return additionalProperties;
    }

    /**
     * Creates a grid with optional header on top containing the specified title and entity properties.
     */
    private static IDisposableComponent createViewer(
            IViewContext<IScreeningClientServiceAsync> viewContext,
            IEntityInformationHolderWithProperties entity, String headingTextOrNull,
            final IDisposableComponent gridComponent,
            Map<String/* label */, String/* value */> additionalPropertiesOrNull,
            String... excludedPropertyCodes)
    {
        final LayoutContainer panel = new LayoutContainer();
        panel.setLayout(new BorderLayout());

        if (headingTextOrNull != null)
        {
            addHeader(panel, viewContext, headingTextOrNull, entity, additionalPropertiesOrNull,
                    excludedPropertyCodes);
        }

        panel.add(gridComponent.getComponent(), new BorderLayoutData(LayoutRegion.CENTER));

        return new IDisposableComponent()
            {

                @Override
                public void update(Set<DatabaseModificationKind> observedModifications)
                {
                }

                @Override
                public DatabaseModificationKind[] getRelevantModifications()
                {
                    return new DatabaseModificationKind[0];
                }

                @Override
                public Component getComponent()
                {
                    return panel;
                }

                @Override
                public void dispose()
                {
                    gridComponent.dispose();
                }
            };
    }

    private static void addHeader(LayoutContainer parentPanel,
            IViewContext<IScreeningClientServiceAsync> viewContext, String headingText,
            IEntityInformationHolderWithProperties entity,
            Map<String/* label */, String/* value */> additionalPropertiesOrNull,
            String... excludedPropertyCodes)
    {
        LayoutContainer panel = new LayoutContainer();
        panel.setLayout(new RowLayout());
        panel.setScrollMode(Scroll.AUTOY);

        Widget headingWidget = PropertiesUtil.createHeaderTitle(headingText);
        panel.add(headingWidget, PropertiesUtil.createHeaderTitleLayoutData());

        LayoutContainer propertiesPanel = new LayoutContainer();
        propertiesPanel.setLayout(new RowLayout());
        int propsHeight =
                PropertiesUtil.addProperties(entity, propertiesPanel, additionalPropertiesOrNull,
                        excludedPropertyCodes);
        panel.add(propertiesPanel, new RowData(1, propsHeight));

        int headersHeight = 25;
        int totalHeight = propsHeight + headersHeight;
        BorderLayoutData layoutData = new BorderLayoutData(LayoutRegion.NORTH, totalHeight);
        layoutData.setMargins(PropertiesUtil.createHeaderInfoMargin());
        parentPanel.add(panel, layoutData);
    }
}
