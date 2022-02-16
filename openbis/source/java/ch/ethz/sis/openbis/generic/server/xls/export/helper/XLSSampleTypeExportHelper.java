package ch.ethz.sis.openbis.generic.server.xls.export.helper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.Vocabulary;
import ch.ethz.sis.openbis.generic.server.xls.export.ExportablePermId;

public class XLSSampleTypeExportHelper implements IXLSExportHelper
{

    @Override
    public int add(final IApplicationServerApi api, final String sessionToken, final Workbook wb,
            final ExportablePermId exportablePermId, int rowNumber)
    {
        final CellStyle cellStyle = wb.createCellStyle();
        final Font boldFont = wb.createFont();
        boldFont.setBold(true);
        cellStyle.setFont(boldFont);

        final List<EntityTypePermId> objectPermIds = Collections.singletonList(
                (EntityTypePermId) exportablePermId.getPermId());
        final SampleTypeFetchOptions fetchOptions = new SampleTypeFetchOptions();
        fetchOptions.withValidationPlugin().withScript();
        final PropertyAssignmentFetchOptions propertyAssignmentFetchOptions = fetchOptions.withPropertyAssignments();
        propertyAssignmentFetchOptions.withPropertyType().withVocabulary();
        propertyAssignmentFetchOptions.withPlugin().withScript();
        final Map<IEntityTypeId, SampleType> sampleTypes = api.getSampleTypes(sessionToken,
                Collections.singletonList(new EntityTypePermId(exportablePermId.getPermId().getPermId(),
                        EntityKind.SAMPLE)), fetchOptions);


        for (Map.Entry<IEntityTypeId, SampleType> entry : sampleTypes.entrySet())
        {
            final IEntityTypeId iEntityTypeId = entry.getKey();
            final SampleType sampleType = entry.getValue();

            addRow(wb, rowNumber++, true, "SAMPLE_TYPE");
            addRow(wb, rowNumber++, true, "Version", "Code", "Auto generate codes", "Validation script",
                    "Generated Code Prefix");

            // TODO: what to put to validation script?
            final Plugin validationPlugin = sampleType.getValidationPlugin();
            final String script = validationPlugin != null
                    ? (validationPlugin.getScript() != null ? validationPlugin.getScript() : "") : "";

            addRow(wb, rowNumber++, false, "1", sampleType.getCode(),
                    String.valueOf(sampleType.isAutoGeneratedCode()).toUpperCase(),
                    script != null ? script : "",
                    sampleType.getGeneratedCodePrefix());

            addRow(wb, rowNumber++, true, "Version", "Code", "Mandatory", "Show in edit views", "Section",
                    "Property label", "Data type", "Vocabulary Code", "Description", "Metadata", "Dynamic script");

            for (final PropertyAssignment propertyAssignment : sampleType.getPropertyAssignments())
            {
                final PropertyType propertyType = propertyAssignment.getPropertyType();
                final Plugin plugin = propertyAssignment.getPlugin();
                final Vocabulary vocabulary = propertyType.getVocabulary();
                addRow(wb, rowNumber++, false, "1", propertyType.getCode(),
                        String.valueOf(propertyAssignment.isMandatory()).toUpperCase(),
                        String.valueOf(propertyAssignment.isShowInEditView()).toUpperCase(), propertyAssignment.getSection(),
                        propertyType.getLabel(), String.valueOf(propertyType.getDataType()),
                        String.valueOf(vocabulary != null ? vocabulary.getCode() : ""), propertyType.getDescription(),
                        mapToJSON(propertyType.getMetaData()),
                        plugin != null ? (plugin.getScript() != null ? plugin.getScript() : "") : "");
            }
        }

        return rowNumber;
    }

    private static String mapToJSON(final Map<?, ?> map)
    {
        if (map.isEmpty())
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

    private static List<EntityTypePermId> toPermIdList(final EntityKind entityKind, final String... codes)
    {
        return Arrays.stream(codes).map(code -> new EntityTypePermId(code, entityKind)).collect(Collectors.toList());
    }

    private static void addRow(final Workbook wb, final int rowNumber, final boolean bold, final String... values)
    {
        final CellStyle cellStyle = wb.createCellStyle();
        final Font font = wb.createFont();
        font.setBold(bold);
        cellStyle.setFont(font);

        final Sheet sheet = wb.getSheetAt(0);
        final Row row = sheet.createRow(rowNumber);
        for (int i = 0; i < values.length; i++)
        {
            final Cell cell = row.createCell(i);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(values[i]);
        }
    }

}
