package ch.ethz.sis.openbis.generic.server.xls.importxls.utils;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.VocabularyTerm;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PropertyTypeSearcher
{

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
            code2PropertyType.put(propertyType.getCode().toLowerCase(Locale.ROOT), propertyType);
            label2PropertyType.put(propertyType.getLabel().toLowerCase(Locale.ROOT), propertyType);
        }
    }

    public PropertyType findPropertyType(String code)
    {
        code = code.toLowerCase(Locale.ROOT);

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
        }
        return value;
    }
}
