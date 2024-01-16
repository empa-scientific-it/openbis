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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.server.business.bo.AbstractBOTest;
import ch.systemsx.cisd.openbis.generic.server.business.bo.CollectionMatcher;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PluginType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.PropertyBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.SamplePEBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Test cases for corresponding {@link EntityPropertiesConverter} class.
 * 
 * @author Christian Ribeaud
 */
// TODO 2011-01-11, Piotr Buczek: test dynamic and managed properties handling
@Friend(toClasses = EntityPropertiesConverter.class)
public final class EntityPropertiesConverterTest extends AbstractBOTest
{
    private static final String VARCHAR_PROPERTY_TYPE_CODE = "color";

    private static final String SAMPLE_TYPE_CODE = "MASTER_PLATE";

    private static final String SAMPLE_TYPE2_CODE = "SAMPLE_TYPE2";

    private IPropertyValueValidator propertyValueValidator;

    private IPropertyPlaceholderCreator placeholderCreator;

    @Override
    @BeforeMethod
    public final void beforeMethod()
    {
        super.beforeMethod();
        propertyValueValidator = context.mock(IPropertyValueValidator.class);
        placeholderCreator = context.mock(IPropertyPlaceholderCreator.class);

    }

    private final IEntityPropertiesConverter createEntityPropertiesConverter(
            final EntityKind entityKind)
    {
        return new EntityPropertiesConverter(entityKind, daoFactory, propertyValueValidator,
                placeholderCreator, null, managedPropertyEvaluatorFactory);
    }

    private void prepareForConvertion(final Expectations exp)
    {
        prepareForConvertion(exp, false);
    }

    private void prepareForConvertion(final Expectations exp, boolean mandatory)
    {
        final SampleTypePE sampleType = createSampleType(SAMPLE_TYPE_CODE);
        final SampleTypePropertyTypePE sampleTypePropertyTypePE = new SampleTypePropertyTypePE();
        sampleTypePropertyTypePE.setEntityType(sampleType);
        final PropertyTypePE propertyType = new PropertyTypePE();
        propertyType.setCode(VARCHAR_PROPERTY_TYPE_CODE);
        sampleTypePropertyTypePE.setPropertyType(propertyType);
        sampleTypePropertyTypePE.setMandatory(mandatory);

        exp.allowing(daoFactory).getEntityPropertyTypeDAO(EntityKind.SAMPLE);
        exp.will(Expectations.returnValue(entityPropertyTypeDAO));

        exp.allowing(daoFactory).getEntityTypeDAO(EntityKind.SAMPLE);
        exp.will(Expectations.returnValue(entityTypeDAO));

        exp.allowing(daoFactory).getPropertyTypeDAO();
        exp.will(Expectations.returnValue(propertyTypeDAO));

        exp.atLeast(1).of(entityTypeDAO).listEntityTypes();
        exp.will(Expectations.returnValue(Collections.singletonList(sampleType)));

        exp.allowing(entityPropertyTypeDAO).listEntityPropertyTypes(sampleType);
        exp.will(Expectations.returnValue(Collections.singletonList(sampleTypePropertyTypePE)));
    }

    private final static SampleTypePE createSampleType(final String sampleTypeCode)
    {
        final SampleTypePE sampleType = new SampleTypePE();
        sampleType.setCode(sampleTypeCode);
        return sampleType;
    }

    private final static IEntityProperty createVarcharSampleProperty(final boolean lowerCase,
            String code)
    {
        final IEntityProperty sampleProperty = new EntityProperty();
        sampleProperty.setValue("blue");
        final PropertyType propertyType = new PropertyType();
        String newCode = code;
        if (lowerCase)
        {
            newCode = newCode.toLowerCase();
        }
        propertyType.setLabel(newCode);
        propertyType.setCode(newCode);
        final DataType dataType = new DataType();
        dataType.setCode(DataTypeCode.VARCHAR);
        propertyType.setDataType(dataType);
        sampleProperty.setPropertyType(propertyType);
        return sampleProperty;
    }

    private final IEntityProperty[] createSampleProperties(final boolean lowerCase)
    {
        return new IEntityProperty[]
        { createVarcharSampleProperty(lowerCase, VARCHAR_PROPERTY_TYPE_CODE) };
    }

    @Test
    public final void testConvertPropertiesFailed()
    {
        final IEntityPropertiesConverter entityPropertiesConverter =
                createEntityPropertiesConverter(EntityKind.MATERIAL);
        boolean fail = true;
        try
        {
            entityPropertiesConverter.convertProperties(null, null, null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        context.assertIsSatisfied();
    }

    @Test
    public final void testConvertPropertiesWithEmptyProperties()
    {
        final IEntityPropertiesConverter entityPropertiesConverter =
                createEntityPropertiesConverter(EntityKind.SAMPLE);

        final RecordingMatcher<Set<IEntityProperty>> definedPropertiesMatcher =
                RecordingMatcher.create();
        context.checking(new Expectations()
            {
                {
                    prepareForConvertion(this);
                    CollectionMatcher<Set<String>> dynamicPropertiesMatcher =
                            new CollectionMatcher<Set<String>>(new HashSet<String>(
                                    new ArrayList<String>()));
                    one(placeholderCreator).addDynamicPropertiesPlaceholders(
                            with(definedPropertiesMatcher), with(dynamicPropertiesMatcher));
                    one(placeholderCreator).addManagedPropertiesPlaceholders(
                            with(definedPropertiesMatcher), with(dynamicPropertiesMatcher));
                }
            });
        final List<EntityPropertyPE> properties =
                entityPropertiesConverter.convertProperties(IEntityProperty.EMPTY_ARRAY,
                        SAMPLE_TYPE_CODE, ManagerTestTool.EXAMPLE_PERSON);
        assertEquals(0, properties.size());
        context.assertIsSatisfied();
    }

    @Test
    public final void testConvertProperties()
    {
        final IEntityPropertiesConverter entityPropertiesConverter =
                createEntityPropertiesConverter(EntityKind.SAMPLE);
        final PropertyTypePE propertyTypePE = createPropertyType();
        final IEntityProperty[] properties = createSampleProperties(false);
        final String value = BasicConstant.MANAGED_PROPERTY_JSON_PREFIX + "[{\"A\":\"alpha\"}]";
        properties[0].setValue(value);

        final RecordingMatcher<Set<IEntityProperty>> definedPropertiesMatcher =
                RecordingMatcher.create();
        context.checking(new Expectations()
            {
                {

                    final SampleTypePE sampleType = createSampleType(SAMPLE_TYPE_CODE);
                    final SampleTypePropertyTypePE sampleTypePropertyTypePE =
                            createETPT(VARCHAR_PROPERTY_TYPE_CODE, sampleType);
                    sampleTypePropertyTypePE.setMandatory(true);
                    ScriptPE script = new ScriptPE();
                    script.setScriptType(ScriptType.MANAGED_PROPERTY);
                    script.setPluginType(PluginType.JYTHON);
                    script.setScript("def batchColumnNames():\n return ['A']\n"
                            + "def updateFromBatchInput(bindings):\n None\n"
                            + "def updateFromRegistrationForm(bindings):\n"
                            + " property.setValue('Hello ' + bindings.get(0).get('A'))");
                    sampleTypePropertyTypePE.setScript(script);

                    final SampleTypePE sampleType2 = createSampleType(SAMPLE_TYPE2_CODE);
                    final SampleTypePropertyTypePE sampleTypePropertyTypePE2 =
                            createETPT(VARCHAR_PROPERTY_TYPE_CODE, sampleType2);

                    this.allowing(daoFactory).getEntityPropertyTypeDAO(EntityKind.SAMPLE);
                    this.will(Expectations.returnValue(entityPropertyTypeDAO));

                    this.allowing(daoFactory).getEntityTypeDAO(EntityKind.SAMPLE);
                    this.will(Expectations.returnValue(entityTypeDAO));

                    this.atLeast(1).of(entityTypeDAO).listEntityTypes();
                    this.will(Expectations.returnValue(Arrays.asList(sampleType, sampleType2)));

                    this.allowing(entityPropertyTypeDAO).listEntityPropertyTypes(sampleType);
                    this.will(Expectations.returnValue(Arrays.asList(sampleTypePropertyTypePE)));

                    one(placeholderCreator).addManagedPropertiesPlaceholders(
                            new HashSet<IEntityProperty>(Arrays.asList(properties)),
                            new HashSet<String>(Arrays.asList(VARCHAR_PROPERTY_TYPE_CODE
                                    .toUpperCase())));

                    this.allowing(entityPropertyTypeDAO).listEntityPropertyTypes(sampleType2);
                    this.will(Expectations.returnValue(Arrays.asList(sampleTypePropertyTypePE2)));

                    one(propertyTypeDAO).tryFindPropertyTypeByCode(VARCHAR_PROPERTY_TYPE_CODE);
                    will(returnValue(propertyTypePE));

                    atLeast(1).of(propertyValueValidator).validatePropertyValue(propertyTypePE,
                            "Hello alpha");
                    will(returnValue("Hello alpha"));

                    CollectionMatcher<Set<String>> dynamicPropertiesMatcher =
                            new CollectionMatcher<Set<String>>(new HashSet<String>(
                                    new ArrayList<String>()));

                    ArrayList<IEntityProperty> listOfProperties = new ArrayList<IEntityProperty>();
                    for (IEntityProperty p : properties)
                    {
                        listOfProperties.add(p);
                    }
                    exactly(2).of(placeholderCreator).addDynamicPropertiesPlaceholders(
                            with(definedPropertiesMatcher), with(dynamicPropertiesMatcher));
                    one(placeholderCreator).addManagedPropertiesPlaceholders(
                            with(definedPropertiesMatcher), with(dynamicPropertiesMatcher));
                }
            });

        List<EntityPropertyPE> convertedProperties =
                entityPropertiesConverter.convertProperties(properties, SAMPLE_TYPE_CODE,
                        ManagerTestTool.EXAMPLE_PERSON);

        assertEquals(
                "[SamplePropertyPE{entityTypePropertyType="
                        + "SampleTypePropertyTypePE{managedInternally=false,mandatory=true,"
                        + "propertyType=COLOR,entityType=SampleTypePE{code=MASTER_PLATE,description=<null>,"
                        + "listable=<null>,containerHierarchyDepth=<null>,"
                        + "generatedFromHierarchyDepth=<null>},ordinal=<null>,"
                        + "section=<null>,dynamic=false,managed=true,unique=false},value=Hello alpha}]",
                convertedProperties.toString());

        // Check that for sample type SAMPLE_TYPE2_CODE property is not mandatory as for previous
        // sample type. This checks that for same property type but different sample types the
        // right sample-type-property-type has been picked
        properties[0].setValue(null);
        convertedProperties =
                entityPropertiesConverter.convertProperties(properties, SAMPLE_TYPE2_CODE,
                        ManagerTestTool.EXAMPLE_PERSON);

        context.assertIsSatisfied();
    }

    private SampleTypePropertyTypePE createETPT(String code, final SampleTypePE sampleType)
    {
        final SampleTypePropertyTypePE sampleTypePropertyTypePE = new SampleTypePropertyTypePE();
        sampleTypePropertyTypePE.setEntityType(sampleType);

        final PropertyTypePE propertyType = new PropertyTypePE();
        propertyType.setCode(code);
        sampleTypePropertyTypePE.setPropertyType(propertyType);
        sampleTypePropertyTypePE.setMandatory(false);
        return sampleTypePropertyTypePE;
    }

    @Test
    public final void testConvertPropertiesWithLowerCase()
    {
        final IEntityPropertiesConverter entityPropertiesConverter =
                createEntityPropertiesConverter(EntityKind.SAMPLE);
        final PropertyTypePE propertyTypePE = createPropertyType();
        final IEntityProperty[] properties = createSampleProperties(true);

        final RecordingMatcher<Set<IEntityProperty>> definedPropertiesMatcher =
                RecordingMatcher.create();
        context.checking(new Expectations()
            {
                {
                    prepareForConvertion(this);

                    one(propertyTypeDAO).tryFindPropertyTypeByCode(VARCHAR_PROPERTY_TYPE_CODE);
                    will(returnValue(propertyTypePE));

                    one(propertyValueValidator).validatePropertyValue(propertyTypePE, "blue");

                    CollectionMatcher<Set<String>> dynamicPropertiesMatcher =
                            new CollectionMatcher<Set<String>>(new HashSet<String>(
                                    new ArrayList<String>()));

                    ArrayList<IEntityProperty> listOfProperties = new ArrayList<IEntityProperty>();
                    for (IEntityProperty p : properties)
                    {
                        listOfProperties.add(p);
                    }
                    one(placeholderCreator).addDynamicPropertiesPlaceholders(
                            with(definedPropertiesMatcher), with(dynamicPropertiesMatcher));
                    one(placeholderCreator).addManagedPropertiesPlaceholders(
                            with(definedPropertiesMatcher), with(dynamicPropertiesMatcher));
                }
            });
        final List<EntityPropertyPE> convertedProperties =
                entityPropertiesConverter.convertProperties(properties,
                        SAMPLE_TYPE_CODE.toLowerCase(), ManagerTestTool.EXAMPLE_PERSON);
        assertEquals(1, convertedProperties.size());
        context.assertIsSatisfied();
    }

    // @Test
    public void testUpdateProperties()
    {
        final SampleTypePE entityType = createSampleType(SAMPLE_TYPE_CODE);
        PersonPE registrator = new PersonPE();
        SamplePEBuilder builder =
                new SamplePEBuilder().property("TEXT", DataTypeCode.VARCHAR, "hello").property(
                        "NUMBER", DataTypeCode.INTEGER, "123");
        SamplePEBuilder builder2 = new SamplePEBuilder().property("C", DataTypeCode.INTEGER, "123");
        Set<SamplePropertyPE> oldProperties = builder.getSample().getProperties();
        final List<EntityTypePropertyTypePE> assignments =
                new ArrayList<EntityTypePropertyTypePE>();
        for (SamplePropertyPE sampleProperty : oldProperties)
        {
            assignments.add(sampleProperty.getEntityTypePropertyType());
        }
        for (SamplePropertyPE sampleProperty : builder2.getSample().getProperties())
        {
            assignments.add(sampleProperty.getEntityTypePropertyType());
        }
        PropertyBuilder p1 = new PropertyBuilder("NUMBER").type(DataTypeCode.INTEGER).value("42");
        PropertyBuilder p2 = new PropertyBuilder("C").type(DataTypeCode.INTEGER).value("137");
        List<IEntityProperty> newProperties =
                Arrays.<IEntityProperty> asList(p1.getProperty(), p2.getProperty());
        IEntityPropertiesConverter entityPropertiesConverter =
                createEntityPropertiesConverter(EntityKind.SAMPLE);
        final RecordingMatcher<Set<IEntityProperty>> matcherOfProperties =
                new RecordingMatcher<Set<IEntityProperty>>();
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getEntityTypeDAO(EntityKind.SAMPLE);
                    will(Expectations.returnValue(entityTypeDAO));

                    one(entityTypeDAO).listEntityTypes();
                    will(returnValue(Arrays.asList(entityType)));

                    allowing(daoFactory).getEntityPropertyTypeDAO(EntityKind.SAMPLE);
                    will(Expectations.returnValue(entityPropertyTypeDAO));

                    allowing(entityPropertyTypeDAO).listEntityPropertyTypes(entityType);
                    will(returnValue(assignments));

                    one(placeholderCreator).addDynamicPropertiesPlaceholders(
                            with(matcherOfProperties), with(Collections.<String> emptySet()));

                    for (String[] codeAndValue : new String[][]
                    {
                            { "NUMBER", "42" },
                            { "C", "137" } })
                    {
                        String code = codeAndValue[0];
                        one(propertyTypeDAO).tryFindPropertyTypeByCode(code);
                        PropertyTypePE type = tryToFind(assignments, code);
                        if (type == null)
                        {
                            type = new PropertyTypePE();
                            type.setCode(code);
                        }
                        will(returnValue(type));

                        String value = codeAndValue[1];
                        one(propertyValueValidator).validatePropertyValue(type, value);
                        will(returnValue(value));
                    }
                }
            });

        Set<SamplePropertyPE> properties =
                entityPropertiesConverter.updateProperties(oldProperties, entityType,
                        newProperties, registrator);

        List<SamplePropertyPE> props = new ArrayList<SamplePropertyPE>(properties);
        Collections.sort(props, new Comparator<SamplePropertyPE>()
            {
                @Override
                public int compare(SamplePropertyPE o1, SamplePropertyPE o2)
                {
                    return o1.getEntityTypePropertyType().getPropertyType().getCode()
                            .compareTo(o2.getEntityTypePropertyType().getPropertyType().getCode());
                }
            });
        assertEquals("C", props.get(0).getEntityTypePropertyType().getPropertyType().getCode());
        assertEquals(DataTypeCode.INTEGER, props.get(0).getEntityTypePropertyType()
                .getPropertyType().getType().getCode());
        assertEquals("137", props.get(0).getValue());
        assertEquals("NUMBER", props.get(1).getEntityTypePropertyType().getPropertyType().getCode());
        assertEquals(DataTypeCode.INTEGER, props.get(1).getEntityTypePropertyType()
                .getPropertyType().getType().getCode());
        assertEquals("42", props.get(1).getValue());
        assertEquals("TEXT", props.get(2).getEntityTypePropertyType().getPropertyType().getCode());
        assertEquals(DataTypeCode.VARCHAR, props.get(2).getEntityTypePropertyType()
                .getPropertyType().getType().getCode());
        assertEquals("hello", props.get(2).getValue());
        assertEquals(3, props.size());
        context.assertIsSatisfied();
    }

    private PropertyTypePE tryToFind(List<EntityTypePropertyTypePE> assignments, String code)
    {
        for (EntityTypePropertyTypePE entityTypePropertyTypePE : assignments)
        {
            if (entityTypePropertyTypePE.getPropertyType().getCode().equals(code))
            {
                return entityTypePropertyTypePE.getPropertyType();
            }
        }
        return null;
    }

    private PropertyTypePE createPropertyType()
    {
        final PropertyTypePE propertyTypePE = new PropertyTypePE();
        propertyTypePE.setCode(VARCHAR_PROPERTY_TYPE_CODE.toLowerCase());
        DataTypePE type = new DataTypePE();
        type.setCode(DataTypeCode.VARCHAR);
        propertyTypePE.setType(type);
        return propertyTypePE;
    }

    @Test
    public void testCreateProperty() throws Exception
    {
        final IEntityPropertiesConverter entityPropertiesConverter =
                createEntityPropertiesConverter(EntityKind.SAMPLE);
        final PropertyTypePE propertyType = createPropertyType();
        EntityKind entityKind = EntityKind.EXPERIMENT;
        EntityTypePropertyTypePE assignment =
                EntityTypePropertyTypePE.createEntityTypePropertyType(entityKind);
        PersonPE registrator = new PersonPE();
        final String defaultValue = "val";
        context.checking(new Expectations()
            {
                {
                    one(propertyValueValidator).validatePropertyValue(propertyType, defaultValue);
                }
            });
        entityPropertiesConverter.tryCreateValidatedPropertyValue(propertyType, assignment,
                defaultValue);
        assertEquals(
                registrator,
                entityPropertiesConverter.createValidatedProperty(propertyType, assignment,
                        registrator, defaultValue).get(0).getRegistrator());
        context.assertIsSatisfied();
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testCreateValidatedPropertyValueMandatoryWithNullGlobal() throws Exception
    {
        final IEntityPropertiesConverter entityPropertiesConverter =
                createEntityPropertiesConverter(EntityKind.SAMPLE);
        final PropertyTypePE propertyType = new PropertyTypePE();
        EntityKind entityKind = EntityKind.EXPERIMENT;
        EntityTypePropertyTypePE assignment =
                EntityTypePropertyTypePE.createEntityTypePropertyType(entityKind);
        assignment.setMandatory(true);
        final String defaultValue = null;

        entityPropertiesConverter.tryCreateValidatedPropertyValue(propertyType, assignment,
                defaultValue);
        context.assertIsSatisfied();
    }

    @Test
    public void testCreateValidatedPropertyValueNotMandatoryWithNullGlobal() throws Exception
    {
        final IEntityPropertiesConverter entityPropertiesConverter =
                createEntityPropertiesConverter(EntityKind.EXPERIMENT);
        final PropertyTypePE propertyType = new PropertyTypePE();
        EntityKind entityKind = EntityKind.EXPERIMENT;
        EntityTypePropertyTypePE assignment =
                EntityTypePropertyTypePE.createEntityTypePropertyType(entityKind);
        assignment.setMandatory(false);
        final String defaultValue = null;
        assertNull(entityPropertiesConverter.tryCreateValidatedPropertyValue(propertyType,
                assignment, defaultValue));
        context.assertIsSatisfied();
    }

}
