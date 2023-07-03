/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import java.util.EnumMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;

import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.collection.IToStringConverter;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.util.SimplePropertyValidator;
import ch.systemsx.cisd.openbis.generic.shared.util.XmlUtils;

/**
 * The default {@link IPropertyValueValidator} implementation.
 * 
 * @author Christian Ribeaud
 */
public final class PropertyValidator implements IPropertyValueValidator
{
    private final static SimplePropertyValidator simplePropertyValidator =
            new SimplePropertyValidator();

    private final static Map<DataTypeCode, IDataTypeValidator> createDataTypeValidators()
    {
        final Map<DataTypeCode, IDataTypeValidator> map =
                new EnumMap<DataTypeCode, IDataTypeValidator>(DataTypeCode.class);
        map.put(DataTypeCode.CONTROLLEDVOCABULARY, new ControlledVocabularyValidator());
        map.put(DataTypeCode.MATERIAL, new MaterialValidator());
        map.put(DataTypeCode.XML, new XmlValidator());
        map.put(DataTypeCode.SAMPLE, new SampleValidator());
        map.put(DataTypeCode.JSON, new JsonValidator());
        map.put(DataTypeCode.ARRAY_STRING, new ArrayValidator());
        map.put(DataTypeCode.ARRAY_INTEGER, new ArrayValidator());
        map.put(DataTypeCode.ARRAY_REAL, new ArrayValidator());
        map.put(DataTypeCode.ARRAY_TIMESTAMP, new ArrayValidator());
        return map;
    }

    @Override
    public final String validatePropertyValue(final PropertyTypePE propertyType, final String value)
            throws UserFailureException
    {
        assert propertyType != null : "Unspecified property type.";
        assert value != null : "Unspecified value.";

        // don't validate error messages and placeholders
        if (value.startsWith(BasicConstant.ERROR_PROPERTY_PREFIX))
        {
            return value;
        }
        final DataTypeCode entityDataType = propertyType.getType().getCode();
        // If the simplePropertyValidator can handle this, use it. .
        if (simplePropertyValidator.canValidate(entityDataType))
        {
            return simplePropertyValidator.validatePropertyValue(entityDataType, value);
        }
        final IDataTypeValidator dataTypeValidator = createDataTypeValidators().get(entityDataType);
        assert dataTypeValidator != null : String.format("No IDataTypeValidator implementation "
                + "specified for '%s'.", entityDataType);
        switch (entityDataType)
        {
            case CONTROLLEDVOCABULARY:
                ((ControlledVocabularyValidator) dataTypeValidator).setVocabulary(propertyType);
                break;
            case MATERIAL:
                ((MaterialValidator) dataTypeValidator).setMaterialType(propertyType
                        .getMaterialType());
                break;
            case XML:
                ((XmlValidator) dataTypeValidator).setXmlSchema(propertyType.getSchema());
                ((XmlValidator) dataTypeValidator).setPropertyTypeLabel(propertyType.getLabel());
                break;
            case ARRAY_STRING:
            case ARRAY_INTEGER:
            case ARRAY_REAL:
            case ARRAY_TIMESTAMP:
                ((ArrayValidator) dataTypeValidator).setArrayType(entityDataType);
                break;
            default:
                break;
        }
        return dataTypeValidator.validate(value);
    }

    //
    // Helper classes
    //

    private static interface IDataTypeValidator
    {
        /**
         * Validates given <var>value</var> according to this data type.
         * 
         * @return the validated value. Note that it can differ from the given one.
         * @throws UserFailureException if given <var>value</var> is not valid.
         */
        public String validate(final String value) throws UserFailureException;
    }

    private final static class SampleValidator implements IDataTypeValidator
    {
        @Override
        public String validate(String value) throws UserFailureException {
            assert value != null : "Unspecified value.";

            if (StringUtils.isBlank(value))
            {
                return null;
            }

            if (value.startsWith("/")) {
                // Is well formed identifier?
            } else {
                // Is well formed permId?
            }

            return value;
        }
    }

    private final static class JsonValidator implements IDataTypeValidator
    {
        @Override
        public String validate(String value) throws UserFailureException {
            assert value != null : "Unspecified value.";

            if (StringUtils.isBlank(value))
            {
                return null;
            }
            //TODO: implement validation for json
            return value;
        }
    }

    private final static class ArrayValidator implements IDataTypeValidator
    {
        DataTypeCode arrayType;

        public void setArrayType(DataTypeCode arrayType)
        {
            this.arrayType = arrayType;
        }

        @Override
        public String validate(String value) throws UserFailureException {
            assert value != null : "Unspecified value.";

            if (StringUtils.isBlank(value))
            {
                return null;
            }
            //TODO: implement validation for array types
            return value;
        }
    }

    private final static class ControlledVocabularyValidator implements IDataTypeValidator
    {

        private VocabularyPE vocabulary;
        private PropertyTypePE propertyTypePE;

        final void setVocabulary(final PropertyTypePE propertyTypePE)
        {
            this.propertyTypePE = propertyTypePE;
            this.vocabulary = propertyTypePE.getVocabulary();
        }

        //
        // IDataTypeValidator
        //

        @Override
        public final String validate(final String value) throws UserFailureException
        {
            assert value != null : "Unspecified value.";
            assert vocabulary != null : "Unspecified vocabulary.";

            String upperCaseValue = value.toUpperCase();
            boolean guard = true;
            if(propertyTypePE.isMultiValue()) {
                if(upperCaseValue.startsWith("[") && upperCaseValue.endsWith("]")) {
                    upperCaseValue = upperCaseValue.substring(1, upperCaseValue.length()-1);
                }
                final String[] split = upperCaseValue.split(",");
                for(String singleValue : split) {
                    guard = guard && hasTerm(singleValue.trim());
                }
            } else {
                guard = hasTerm(upperCaseValue);
            }
            if(guard) {
                return upperCaseValue;
            }
            throw UserFailureException.fromTemplate("Vocabulary value '%s' of property '%s' is not valid. "
                    + "It must exist in '%s' controlled vocabulary %s", upperCaseValue, propertyTypePE.getCode(),
                    vocabulary.getCode(), getVocabularyDetails());
        }

        private boolean hasTerm(String value) {
            vocabulary.tryGetVocabularyTerm(value);
            VocabularyTermPE termOrNull = vocabulary.tryGetVocabularyTerm(value);
            return termOrNull != null;
        }

        /**
         * @return Details about vocabulary dependent on {@link VocabularyPE#isChosenFromList()} value:
         *         <ul>
         *         <li>for <var>true</var> - returns a list of first few vocabulary terms from it.
         *         <li>for <var>false</var> - returns a vocabulary description
         *         </ul>
         */
        private final String getVocabularyDetails()
        {
            if (vocabulary.isChosenFromList())
            {
                return CollectionUtils.abbreviate(vocabulary.getTerms(), 10,
                        new IToStringConverter<VocabularyTermPE>()
                            {

                                //
                                // IToStringConverter
                                //

                                @Override
                                public final String toString(final VocabularyTermPE term)
                                {
                                    return term.getCode();
                                }
                            });
            } else
            {
                String descriptionOrNull = vocabulary.getDescription();
                return descriptionOrNull == null ? "" : " - " + descriptionOrNull;
            }
        }

    }

    private final static class MaterialValidator implements IDataTypeValidator
    {

        private MaterialTypePE materialTypeOrNull;

        public void setMaterialType(MaterialTypePE materialType)
        {
            this.materialTypeOrNull = materialType;
        }

        //
        // IDataTypeValidator
        //

        @Override
        public final String validate(final String value) throws UserFailureException
        {
            assert value != null : "Unspecified value.";

            if (StringUtils.isBlank(value))
            {
                return null;
            }
            final MaterialIdentifier identifierOrNull =
                    MaterialIdentifier.tryParseIdentifier(value);
            if (identifierOrNull == null)
            {
                if (materialTypeOrNull == null)
                {
                    throw UserFailureException
                            .fromTemplate(
                                    "Material specification '%s' has improper format. "
                                            + "Expected format is '<CODE> (<TYPE>)'. Type has to be specified because any type of material can be assigned.",
                                    value);
                } else
                {
                    return value;
                }
            }
            if (materialTypeOrNull != null
                    && identifierOrNull.getTypeCode()
                            .equalsIgnoreCase(materialTypeOrNull.getCode()) == false)
            {
                throw UserFailureException.fromTemplate(
                        "Material '%s' is of wrong type. Expected: '%s'.", value,
                        materialTypeOrNull.getCode());
            }
            return value;
        }
    }

    private final static class XmlValidator implements IDataTypeValidator
    {

        private String xmlSchema;

        private String propertyTypeLabel;

        public void setXmlSchema(String xmlSchema)
        {
            this.xmlSchema = xmlSchema;
        }

        public void setPropertyTypeLabel(String label)
        {
            this.propertyTypeLabel = label;
        }

        //
        // IDataTypeValidator
        //

        @Override
        public final String validate(final String value) throws UserFailureException
        {
            assert value != null : "Unspecified value.";

            // parsing checks if the value is a well-formed XML document
            Document document = XmlUtils.parseXmlDocument(value);
            if (xmlSchema != null)
            {
                // validate against schema
                try
                {
                    XmlUtils.validate(document, xmlSchema);
                } catch (Exception e)
                {
                    // instance document is invalid!
                    throw UserFailureException.fromTemplate(
                            "Provided value doesn't validate against schema "
                                    + "of property type '%s'. %s", propertyTypeLabel,
                            e.getMessage());
                }
            }

            // validated value is valid
            return value;
        }
    }
}
