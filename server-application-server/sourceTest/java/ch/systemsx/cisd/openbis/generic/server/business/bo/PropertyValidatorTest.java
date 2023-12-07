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
package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.PropertyValidator;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.util.SupportedDateTimePattern;
import ch.systemsx.cisd.openbis.generic.shared.util.XmlUtilsTest;

/**
 * Test cases for corresponding {@link PropertyValidator} class.
 * 
 * @author Christian Ribeaud
 */
@Friend(toClasses = PropertyValidator.class)
public final class PropertyValidatorTest extends AbstractBOTest
{
    private final PropertyValidator createPropertyValidator()
    {
        return new PropertyValidator();
    }

    private final static PropertyTypePE createPropertyType(final DataTypeCode entityDataType)
    {
        final PropertyTypePE propertyType = new PropertyTypePE();
        propertyType.setSimpleCode("PROP");
        final DataTypePE dataType = new DataTypePE();
        dataType.setCode(entityDataType);
        propertyType.setType(dataType);
        return propertyType;
    }

    private final static VocabularyTermPE createVocabularyTerm(final String code)
    {
        final VocabularyTermPE vocabularyTermPE = new VocabularyTermPE();
        vocabularyTermPE.setCode(code);
        return vocabularyTermPE;
    }

    private final static PropertyTypePE createBooleanPropertyType()
    {
        final PropertyTypePE propertyType = createPropertyType(DataTypeCode.BOOLEAN);
        return propertyType;
    }

    private final static PropertyTypePE createRealPropertyType()
    {
        final PropertyTypePE propertyType = createPropertyType(DataTypeCode.REAL);
        return propertyType;
    }

    private final static PropertyTypePE createIntegerPropertyType()
    {
        final PropertyTypePE propertyType = createPropertyType(DataTypeCode.INTEGER);
        return propertyType;
    }

    private final static PropertyTypePE createTimestampPropertyType()
    {
        final PropertyTypePE propertyType = createPropertyType(DataTypeCode.TIMESTAMP);
        return propertyType;
    }

    private final static PropertyTypePE createVarcharPropertyType()
    {
        final PropertyTypePE propertyType = createPropertyType(DataTypeCode.VARCHAR);
        return propertyType;
    }

    private final static PropertyTypePE createXmlPropertyType(String label, String schema)
    {
        final PropertyTypePE propertyType = createPropertyType(DataTypeCode.XML);
        propertyType.setLabel(label);
        propertyType.setSchema(schema);
        return propertyType;
    }

    @DataProvider
    private final static Object[][] getNonWorkingValues()
    {
        return new Object[][]
        {
                { createTimestampPropertyType(), DateFormatUtils.format(new Date(), "yyyy") },
                { createIntegerPropertyType(), "a" },
                { createIntegerPropertyType(), "1.1" },
                { createRealPropertyType(), "b" },
                { createBooleanPropertyType(), "BOB" }, };
    }

    @DataProvider
    private final static Object[][] getWorkingValues()
    {
        return new Object[][]
        {
                { createVarcharPropertyType(), "" },
                { createVarcharPropertyType(), "varchar" },
                {
                        createTimestampPropertyType(),
                        DateFormatUtils.format(new Date(),
                                SupportedDateTimePattern.CANONICAL_DATE_PATTERN.getPattern()) },
                {
                        createTimestampPropertyType(),
                        DateFormatUtils.format(new Date(),
                                SupportedDateTimePattern.US_DATE_TIME_24_PATTERN.getPattern()) },
                { createIntegerPropertyType(), "1" },
                { createRealPropertyType(), "1" },
                { createRealPropertyType(), "1.1" },
                { createBooleanPropertyType(), "yes" },
                { createBooleanPropertyType(), "1" },
                { createBooleanPropertyType(), "true" } };
    }

    @Test
    public final void testValidatePropertyValueWithNull()
    {
        boolean fail = true;
        try
        {
            createPropertyValidator().validatePropertyValue(null, null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
    }

    @Test(dataProvider = "getWorkingValues")
    public final void testValidatePropertyValue(final PropertyTypePE propertyType,
            final String value)
    {
        final PropertyValidator propertyValidator = createPropertyValidator();
        propertyValidator.validatePropertyValue(propertyType, value);
    }

    @Test(dataProvider = "getNonWorkingValues")
    public final void testValidatePropertyValueFailed(final PropertyTypePE propertyType,
            final String value)
    {
        final PropertyValidator propertyValidator = createPropertyValidator();
        try
        {
            propertyValidator.validatePropertyValue(propertyType, value);
            fail(String.format("'%s' expected.", UserFailureException.class.getSimpleName()));
        } catch (final UserFailureException ex)
        {
            // Nothing to do here.
        }
    }

    //
    // XML property with schema
    //

    @Test
    public final void testValidateXMLPropertyValue()
    {
        final String propertyLabel = "prop";
        final String propertySchema = XmlUtilsTest.EXAMPLE_SCHEMA;
        final String propertyValue = XmlUtilsTest.EXAMPLE_XML;
        final PropertyTypePE propertyType = createXmlPropertyType(propertyLabel, propertySchema);
        final PropertyValidator propertyValidator = createPropertyValidator();
        propertyValidator.validatePropertyValue(propertyType, propertyValue);
    }

    @Test
    public final void testValidateXMLPropertyValueFailed()
    {
        final String propertyLabel = "prop";
        final String propertySchema = XmlUtilsTest.EXAMPLE_SCHEMA;
        final String propertyValue = XmlUtilsTest.EXAMPLE_INCORRECT_XML;
        final PropertyTypePE propertyType = createXmlPropertyType(propertyLabel, propertySchema);
        final PropertyValidator propertyValidator = createPropertyValidator();
        try
        {
            propertyValidator.validatePropertyValue(propertyType, propertyValue);
            fail(String.format("'%s' expected.", UserFailureException.class.getSimpleName()));
        } catch (final UserFailureException ex)
        {
            assertEquals(String.format(
                    "Provided value doesn't validate against schema of property type '%s'. "
                            + "cvc-complex-type.2.4.d: "
                            + "Invalid content was found starting with element 'footer'. "
                            + "No child element is expected at this point.", propertyLabel),
                    ex.getMessage());
        }
    }

    //
    // Controlled Vocabulary
    //

    private final static PropertyTypePE createControlledVocabularyPropertyType()
    {
        final PropertyTypePE propertyType = createPropertyType(DataTypeCode.CONTROLLEDVOCABULARY);
        final VocabularyPE vocabularyPE = new VocabularyPE();
        final List<VocabularyTermPE> terms = new ArrayList<VocabularyTermPE>();
        terms.add(createVocabularyTerm("GOODVALUE"));
        vocabularyPE.setTerms(terms);
        propertyType.setVocabulary(vocabularyPE);
        return propertyType;
    }

    @Test
    public final void testValidateControlledVocabularyPropertyValue()
    {
        final PropertyTypePE propertyType = createControlledVocabularyPropertyType();
        final String value = "goodValue";
        final PropertyValidator propertyValidator = createPropertyValidator();
        propertyValidator.validatePropertyValue(propertyType, value);
    }

    @Test
    public final void testValidateControlledVocabularyPropertyValueFailed()
    {
        PropertyTypePE propertyType = createControlledVocabularyPropertyType();
        final String value = "wrongValue";
        final PropertyValidator propertyValidator = createPropertyValidator();
        try
        {
            propertyValidator.validatePropertyValue(propertyType, value);
            fail(String.format("'%s' expected.", UserFailureException.class.getSimpleName()));
        } catch (final UserFailureException ex)
        {
            // Nothing to do here.
        }
    }

    //
    // Material
    //

    private final static PropertyTypePE createMaterialPropertyType(MaterialTypePE materialType)
    {
        final PropertyTypePE propertyType = createPropertyType(DataTypeCode.MATERIAL);
        propertyType.setMaterialType(materialType);
        return propertyType;
    }

    @Test
    public final void testValidateMaterialPropertyValueNoType()
    {
        final PropertyTypePE propertyType = createMaterialPropertyType(null);
        final String value = "code (type)";
        final PropertyValidator propertyValidator = createPropertyValidator();
        propertyValidator.validatePropertyValue(propertyType, value);
    }

    @Test
    public final void testValidateMaterialPropertyValueWithType()
    {
        final MaterialTypePE materialType = new MaterialTypePE();
        materialType.setCode("t1");
        final PropertyTypePE propertyType = createMaterialPropertyType(materialType);
        final String value = "code (" + materialType.getCode() + ")";
        final PropertyValidator propertyValidator = createPropertyValidator();
        propertyValidator.validatePropertyValue(propertyType, value);
    }

    @Test
    public final void testValidateMaterialPropertyValueNoTypeFailed()
    {
        PropertyTypePE propertyType = createMaterialPropertyType(null);
        final String value = "noType";
        final PropertyValidator propertyValidator = createPropertyValidator();
        try
        {
            propertyValidator.validatePropertyValue(propertyType, value);
            fail(String.format("'%s' expected.", UserFailureException.class.getSimpleName()));
        } catch (final UserFailureException ex)
        {
            assertEquals(
                    "Material specification '"
                            + value
                            + "' has improper format. Expected format is '<CODE> (<TYPE>)'. Type has to be specified because any type of material can be assigned.",
                    ex.getMessage());
        }
    }

    @Test
    public final void testValidateMaterialPropertyValueWithTypeFailed()
    {
        final MaterialTypePE materialType = new MaterialTypePE();
        materialType.setCode("t1");
        PropertyTypePE propertyType = createMaterialPropertyType(materialType);
        final String value = "wrongType (t2)";
        final PropertyValidator propertyValidator = createPropertyValidator();
        try
        {
            propertyValidator.validatePropertyValue(propertyType, value);
            fail(String.format("'%s' expected.", UserFailureException.class.getSimpleName()));
        } catch (final UserFailureException ex)
        {
            assertEquals(
                    "Material '" + value + "' is of wrong type. Expected: '"
                            + materialType.getCode() + "'.", ex.getMessage());
        }
    }
}
