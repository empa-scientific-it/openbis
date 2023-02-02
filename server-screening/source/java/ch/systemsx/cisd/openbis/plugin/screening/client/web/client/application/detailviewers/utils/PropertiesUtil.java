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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithProperties;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.LayoutUtils;

/**
 * Utility methods to display properties and header in the publication views.
 * 
 * @author Tomasz Pylak
 */
public class PropertiesUtil
{
    public static String tryFindProperty(IEntityPropertiesHolder propertiesHolder,
            String propertyCode)
    {
        for (IEntityProperty property : propertiesHolder.getProperties())
        {
            if (property.getPropertyType().getCode().equalsIgnoreCase(propertyCode))
            {
                return property.tryGetAsString();
            }
        }
        return null;
    }

    /** @return estimated height */
    public static int addProperties(IEntityInformationHolderWithProperties propertiesHolder,
            LayoutContainer panel,
            Map<String/* label */, String/* value */> additionalPropertiesOrNull,
            String... excludedPropertyCodes)
    {
        LayoutContainer propertiesPanel = new LayoutContainer();
        propertiesPanel.setLayout(new TableLayout(3));

        Map<String, String> propertiesMap =
                createSortedMap(propertiesHolder, excludedPropertyCodes, additionalPropertiesOrNull);
        int height = 0;
        for (Entry<String, String> entry : propertiesMap.entrySet())
        {
            addProperty(propertiesPanel, entry.getKey(), entry.getValue());
            height += getEstimatedHeight(entry.getValue());
        }
        panel.add(propertiesPanel);
        return height;
    }

    private static Map<String, String> createSortedMap(
            IEntityInformationHolderWithProperties propertiesHolder,
            String[] excludedPropertyCodes, Map<String, String> additionalPropertiesOrNull)
    {
        Set<String> excludedPropertyCodesSet =
                new HashSet<String>(Arrays.asList(excludedPropertyCodes));

        Map<String, String> propertiesMap = new TreeMap<String, String>();
        for (IEntityProperty property : propertiesHolder.getProperties())
        {
            PropertyType propertyType = property.getPropertyType();
            if (excludedPropertyCodesSet.contains(propertyType.getCode()) == false)
            {
                String value = property.tryGetAsString();
                if (value != null && value.length() > 0)
                {
                    propertiesMap.put(propertyType.getLabel(), value);
                }
            }
        }
        if (additionalPropertiesOrNull != null)
        {
            for (Entry<String, String> entry : additionalPropertiesOrNull.entrySet())
            {
                propertiesMap.put(entry.getKey(), entry.getValue());
            }
        }
        return propertiesMap;
    }

    private static void addProperty(LayoutContainer propertiesPanel, String label, String value)
    {
        TableData labelLayout = new TableData(HorizontalAlignment.RIGHT, VerticalAlignment.TOP);
        labelLayout.setWidth("80px");
        propertiesPanel.add(new Html(label + ": "), labelLayout);
        TableData spacerLayout = new TableData();
        spacerLayout.setWidth("5px");
        propertiesPanel.add(new Text(""), spacerLayout);
        TableData valueLayout = new TableData(HorizontalAlignment.LEFT, VerticalAlignment.TOP);
        propertiesPanel.add(new Html(StringEscapeUtils.unescapeHtml(value)), valueLayout);
    }

    private static int getEstimatedHeight(String text)
    {
        int charsPerLine = 150;
        return LayoutUtils.ONE_HEADER_LINE_HEIGHT_PX * ((text.length() / charsPerLine) + 1);
    }

    public static Margins createHeaderInfoMargin()
    {
        return new Margins(3, 3, 10, 3);
    }

    public static RowData createHeaderTitleLayoutData()
    {
        return new RowData(-1, -1, new Margins(0, 0, 5, 0));
    }

    public static Html createHeaderTitle(String headingText)
    {
        Html headingWidget = new Html(headingText);
        // NOTE: this should be refactored to an external CSS style
        headingWidget.setTagName("h1");
        return headingWidget;
    }
}
