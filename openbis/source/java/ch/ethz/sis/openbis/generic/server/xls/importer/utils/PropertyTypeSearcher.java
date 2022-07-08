package ch.ethz.sis.openbis.generic.server.xls.importer.utils;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.LongDateFormat;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ShortDateFormat;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.VocabularyTerm;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import org.apache.poi.ss.usermodel.DateUtil;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropertyTypeSearcher
{

    public static final SimpleDateFormat timestampFormatter = new SimpleDateFormat(new LongDateFormat().getFormat());

    public static final SimpleDateFormat dateFormatter = new SimpleDateFormat(new ShortDateFormat().getFormat());

    public static final String SAMPLE_DATA_TYPE_PREFIX = "SAMPLE";

    public static final String SAMPLE_DATA_TYPE_MANDATORY_TYPE = ":";

    public static final String VARIABLE_PREFIX = "$";

    private Map<String, PropertyType> code2PropertyType;

    private Map<String, PropertyType> label2PropertyType;

    public PropertyTypeSearcher(List<PropertyAssignment> assignment)
    {
        this.code2PropertyType = new HashMap<>();
        this.label2PropertyType = new HashMap<>();

        for (PropertyAssignment propertyAssignment : assignment)
        {
            PropertyType propertyType = propertyAssignment.getPropertyType();
            code2PropertyType.put(propertyType.getCode(), propertyType);
            label2PropertyType.put(propertyType.getLabel(), propertyType);
        }
    }

    public Map<String, PropertyType> getCode2PropertyType() {
        return code2PropertyType;
    }

    public Map<String, PropertyType> getLabel2PropertyType() {
        return label2PropertyType;
    }

    public PropertyType findPropertyType(String code)
    {
        if (code2PropertyType.containsKey(code))
        {
            return code2PropertyType.get(code);
        }
        if (label2PropertyType.containsKey(code))
        {
            return label2PropertyType.get(code);
        }

        throw new UserFailureException("Can't find property with code or label " + code);
    }

    public static String getPropertyValue(PropertyType propertyType, String value)
    {
        if (propertyType.getDataType() == DataType.CONTROLLEDVOCABULARY)
        {
            // First we try to code match, codes have priority
            for (VocabularyTerm term : propertyType.getVocabulary().getTerms())
            {
                if (term.getCode().equals(value))
                {
                    return term.getCode();
                }
            }
            // If we can't match by code we try to match by label
            for (VocabularyTerm term : propertyType.getVocabulary().getTerms())
            {
                if (term.getLabel() != null && term.getLabel().equals(value))
                {
                    return term.getCode();
                }
            }
        } else if (propertyType.getDataType() == DataType.TIMESTAMP) { // Converts native excel timestamps
            if (value != null && isDouble(value)) {
                value = timestampFormatter.format(DateUtil.getJavaDate(Double.parseDouble(value)));
            }
        } else if (propertyType.getDataType() == DataType.DATE) { // Converts native excel dates
            if (value != null && isDouble(value)) {
                value = dateFormatter.format(DateUtil.getJavaDate(Double.parseDouble(value)));
            }
        }
        return value;
    }

    private static boolean isDouble(String string)
    {
        try
        {
            Double.parseDouble(string);
        }
        catch (NumberFormatException e)
        {
            return false;
        }
        return true;
    }

}
