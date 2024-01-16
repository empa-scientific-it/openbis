/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.xls.export.helper;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.PictureData;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IEntityType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.server.xls.export.Attribute;
import ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind;
import ch.ethz.sis.openbis.generic.server.xls.export.FieldType;
import ch.ethz.sis.openbis.generic.server.xls.export.XLSExport;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;

public abstract class AbstractXLSExportHelper<ENTITY_TYPE extends IEntityType> implements IXLSExportHelper<ENTITY_TYPE>
{

    protected static final String[] ENTITY_ASSIGNMENT_COLUMNS = new String[] { "Code", "Mandatory",
            "Show in edit views", "Section", "Property label", "Data type", "Vocabulary code", "Description",
            "Metadata", "Dynamic script", "Multivalued" };

    protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(BasicConstant.DATE_HOURS_MINUTES_SECONDS_PATTERN);

    public static final String FIELD_TYPE_KEY = "type";

    public static final String FIELD_ID_KEY = "id";

    private final Workbook wb;
    
    private final CellStyle normalCellStyle;
    
    private final CellStyle boldCellStyle;

    private final CellStyle errorCellStyle;

    public AbstractXLSExportHelper(final Workbook wb)
    {
        this.wb = wb;
        
        normalCellStyle = wb.createCellStyle();
        boldCellStyle = wb.createCellStyle();
        errorCellStyle = wb.createCellStyle();
        
        final Font boldFont = wb.createFont();
        boldFont.setBold(true);
        boldCellStyle.setFont(boldFont);
        
        final Font normalFont = wb.createFont();
        normalFont.setBold(false);
        normalCellStyle.setFont(normalFont);
        
        errorCellStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.RED.getIndex());
        errorCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    }

    protected static boolean isFieldAcceptable(final Set<Attribute> attributeSet, final Map<String, String> field)
    {
        return FieldType.valueOf(field.get(FIELD_TYPE_KEY)) != FieldType.ATTRIBUTE ||
                attributeSet.contains(Attribute.valueOf(field.get(FIELD_ID_KEY)));
    }

    protected String mapToJSON(final Map<?, ?> map)
    {
        if (map == null || map.isEmpty())
        {
            return "";
        } else
        {
            try
            {
                return new ObjectMapper().writeValueAsString(map);
            } catch (final JsonProcessingException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    protected AddRowResult addRow(final int rowNumber, final boolean bold,
            final ExportableKind exportableKind, final String idForWarningsOrErrors, final String... values)
    {
        final Collection<String> warnings = new ArrayList<>();
        final Map<String, String> valueFiles = new HashMap<>();

        final Row row = wb.getSheetAt(0).createRow(rowNumber);
        for (int i = 0; i < values.length; i++)
        {
            final Cell cell = row.createCell(i);
            final String value = values[i] != null ? values[i] : "";

            if (value.length() <= Short.MAX_VALUE)
            {
                cell.setCellStyle(bold ? boldCellStyle : normalCellStyle);
                cell.setCellValue(value);
            } else
            {
                final String fileName = String.format("value-%c%d.txt", (char) ('A' + i), rowNumber + 1);

                cell.setCellValue(String.format("__%s__", fileName));
                valueFiles.put(fileName, value);
            }
        }

        return new AddRowResult(warnings, valueFiles);
    }

    protected void addRow(int rowNumber, boolean bold, final ExportableKind exportableKind, final String idForWarningsOrErrors,
            final Collection<String> warnings, final Map<String, String> valueFiles, final String... values)
    {
        final AddRowResult addRowResult = addRow(rowNumber, bold, exportableKind, idForWarningsOrErrors, values);
        warnings.addAll(addRowResult.getWarnings());
        valueFiles.putAll(addRowResult.getValueFiles());
    }

    @Override
    public ENTITY_TYPE getEntityType(final IApplicationServerApi api, final String sessionToken, final String permId)
    {
        return null;
    }

    protected static Function<PropertyType, String> getPropertiesMappingFunction(
            final XLSExport.TextFormatting textFormatting, final Map<String, Serializable> properties)
    {
        return textFormatting == XLSExport.TextFormatting.PLAIN
                ? propertyType -> propertyType.getDataType() == DataType.MULTILINE_VARCHAR
                        ? getProperty(properties, propertyType) != null
                                ? ((String)properties.get(propertyType.getCode())).replaceAll("<[^>]+>", "")
                                : null
                        : getProperty(properties, propertyType)
                : propertyType -> getProperty(properties, propertyType);
    }

    private static String getProperty(final Map<String, Serializable> properties, final PropertyType propertyType)
    {
        Serializable propertyValue = properties.get(propertyType.getCode());
        if(propertyValue == null)
        {
            return null;
        }
        if(propertyValue.getClass().isArray())
        {
            StringBuilder sb = new StringBuilder();
            Serializable[] values = (Serializable[]) propertyValue;
            for(Serializable value : values)
            {
                if(sb.length() > 0)
                {
                    sb.append(", ");
                }
                sb.append(value);
            }
            return sb.toString();
        } else
        {
            return propertyValue.toString();
        }
    }


    protected static class AddRowResult
    {

        private final Collection<String> warnings;

        private final Map<String, String> valueFiles;

        protected AddRowResult(final Collection<String> warnings, final Map<String, String> valueFiles)
        {
            this.warnings = warnings;
            this.valueFiles = valueFiles;
        }

        public Collection<String> getWarnings()
        {
            return warnings;
        }

        public Map<String, String> getValueFiles()
        {
            return valueFiles;
        }

    }

}
