/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.generic.server;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExperimentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IMaterialDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IProjectDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISpaceDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterialsWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author Franz-Josef Elmer
 */
public class EntityExistenceCheckerTest extends AssertJUnit
{
    private static final String DATABASE_INSTANCE_CODE = "MY-DB";

    private Mockery context;

    private ISpaceDAO spaceDAO;

    private IProjectDAO projectDAO;

    private IExperimentDAO experimentDAO;

    private ISampleDAO sampleDAO;

    private IMaterialDAO materialDAO;

    private IEntityTypeDAO materialTypeDAO;

    private ISampleTypeDAO sampleTypeDAO;

    private EntityExistenceChecker checker;

    @BeforeMethod
    public void setUp()
    {
        LogInitializer.init();
        context = new Mockery();
        final IDAOFactory daoFactory = context.mock(IDAOFactory.class);
        spaceDAO = context.mock(ISpaceDAO.class);
        projectDAO = context.mock(IProjectDAO.class);
        experimentDAO = context.mock(IExperimentDAO.class);
        sampleDAO = context.mock(ISampleDAO.class);
        materialDAO = context.mock(IMaterialDAO.class);
        materialTypeDAO = context.mock(IEntityTypeDAO.class);
        sampleTypeDAO = context.mock(ISampleTypeDAO.class);
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getSpaceDAO();
                    will(returnValue(spaceDAO));

                    allowing(daoFactory).getProjectDAO();
                    will(returnValue(projectDAO));

                    allowing(daoFactory).getExperimentDAO();
                    will(returnValue(experimentDAO));

                    allowing(daoFactory).getSampleDAO();
                    will(returnValue(sampleDAO));

                    allowing(daoFactory).getEntityTypeDAO(EntityKind.MATERIAL);
                    will(returnValue(materialTypeDAO));

                    allowing(daoFactory).getMaterialDAO();
                    will(returnValue(materialDAO));

                    allowing(daoFactory).getSampleTypeDAO();
                    will(returnValue(sampleTypeDAO));
                }
            });
        checker = new EntityExistenceChecker(daoFactory);
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testCheckNewMaterials()
    {
        MaterialType type = new MaterialType();
        type.setCode("T1");
        prepareForAssertMaterialTypeExists(type.getCode(), materialType(type, "ALPHA", "BETA"));

        checker.checkNewMaterials(Arrays.asList(new NewMaterialsWithTypes(type, Arrays.asList(
                material("A", "alpha:12", "beta:42"), material("B", "BETa:47", "Alpha:11")))));

        assertThat(checker.getErrors().size(), is(0));
        context.assertIsSatisfied();
    }

    @Test
    public void testCheckNewMaterialsWithUnknownType()
    {
        MaterialType type = new MaterialType();
        type.setCode("T1");
        prepareForAssertMaterialTypeExists(type.getCode(), null);

        checker.checkNewMaterials(Arrays.asList(new NewMaterialsWithTypes(type, Arrays
                .asList(material("A", "alpha:12", "beta:42")))));

        assertThat(checker.getErrors(), containsExactly("Unknown material type: T1"));
        context.assertIsSatisfied();
    }

    @Test
    public void testCheckNewMaterialsWithUnknownPropertyType()
    {
        MaterialType type = new MaterialType();
        type.setCode("T1");
        prepareForAssertMaterialTypeExists(type.getCode(), materialType(type, "ALPHA"));

        checker.checkNewMaterials(Arrays.asList(new NewMaterialsWithTypes(type, Arrays
                .asList(material("A", "alpha:12", "beta:42")))));

        assertThat(checker.getErrors(),
                containsExactly("Material type T1 has no property type BETA assigned."));
        context.assertIsSatisfied();
    }

    @Test
    public void testCheckNewSamplesOnSpaceLevel()
    {
        SampleType sampleType = new SampleType();
        sampleType.setCode("T1");
        prepareForAssertSampleTypeExists(sampleType.getCode(), sampleType("ALPHA", "BETA"));
        context.checking(new Expectations()
            {
                {
                    one(projectDAO).tryFindProject("S1", "P1");
                    ProjectPE project = new ProjectPE();
                    project.setCode("P1");
                    SpacePE space = new SpacePE();
                    space.setCode("S1");
                    project.setSpace(space);
                    will(returnValue(project));

                    one(experimentDAO).tryFindByCodeAndProject(project, "E1");
                    will(returnValue(new ExperimentPE()));

                    one(spaceDAO).tryFindSpaceByCode("S1");
                    will(returnValue(space));

                    one(sampleDAO).tryFindByCodeAndSpace("PLATE", space);
                    SamplePE sample = new SamplePE();
                    sample.setCode("PLATE");
                    will(returnValue(sample));
                }
            });

        checker.checkNewSamples(Arrays.asList(new NewSamplesWithTypes(sampleType, Arrays.asList(
                sample("/S1/A", "/S1/P1/E1", "/S1/PLATE", "alpha:12"),
                sample("/S1/B", "/S1/P1/E1", "/S1/PLATE", "Beta:42")))));

        assertThat(checker.getErrors().size(), is(0));
        context.assertIsSatisfied();
    }

    @Test
    public void testCheckNewSamplesOnDatabaseInstanceLevel()
    {
        SampleType sampleType = new SampleType();
        sampleType.setCode("T1");
        prepareForAssertSampleTypeExists(sampleType.getCode(), sampleType("ALPHA", "BETA"));
        context.checking(new Expectations()
            {
                {
                    one(projectDAO).tryFindProject("S1", "P1");
                    ProjectPE project = new ProjectPE();
                    project.setCode("P1");
                    SpacePE space = new SpacePE();
                    space.setCode("S1");
                    project.setSpace(space);
                    will(returnValue(project));

                    one(experimentDAO).tryFindByCodeAndProject(project, "E1");
                    will(returnValue(new ExperimentPE()));

                    one(sampleDAO).tryFindByCodeAndDatabaseInstance("PLATE");
                    SamplePE sample = new SamplePE();
                    sample.setCode("PLATE");
                    will(returnValue(sample));
                }
            });

        checker.checkNewSamples(Arrays.asList(new NewSamplesWithTypes(sampleType, Arrays.asList(
                sample("A", "/S1/P1/E1", "/PLATE", "alpha:12"),
                sample("B", "/S1/P1/E1", "/PLATE", "Beta:42")))));

        assertThat(checker.getErrors().size(), is(0));
        context.assertIsSatisfied();
    }

    @Test
    public void testCheckNewSamplesWithManagedProperty()
    {
        SampleType sampleType = new SampleType();
        sampleType.setCode("T1");
        prepareForAssertSampleTypeExists(sampleType.getCode(), sampleType("ALPHA"));
        context.checking(new Expectations()
            {
                {
                    one(projectDAO).tryFindProject("S1", "P1");
                    ProjectPE project = new ProjectPE();
                    project.setCode("P1");
                    SpacePE space = new SpacePE();
                    space.setCode("S1");
                    project.setSpace(space);
                    will(returnValue(project));

                    one(experimentDAO).tryFindByCodeAndProject(project, "E1");
                    will(returnValue(new ExperimentPE()));

                    one(sampleDAO).tryFindByCodeAndDatabaseInstance("PLATE");
                    SamplePE sample = new SamplePE();
                    sample.setCode("PLATE");
                    will(returnValue(sample));
                }
            });

        checker.checkNewSamples(Arrays.asList(new NewSamplesWithTypes(sampleType, Arrays.asList(
                sample("A", "/S1/P1/E1", "/PLATE", "alpha:f1:12", "alpha:f2:42")))));

        assertEquals("[]", checker.getErrors().toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testCheckNewSamplesWithContainerAndContainedSamples()
    {
        SampleType containerType = new SampleType();
        containerType.setCode("PLATE");
        prepareForAssertSampleTypeExists(containerType.getCode(), sampleType("ALPHA"));
        SampleType containedType = new SampleType();
        containedType.setCode("WELL");
        prepareForAssertSampleTypeExists(containedType.getCode(), sampleType("BETA"));
        context.checking(new Expectations()
            {
                {
                    one(projectDAO).tryFindProject("S1", "P1");
                    ProjectPE project = new ProjectPE();
                    project.setCode("P1");
                    SpacePE space = new SpacePE();
                    space.setCode("S1");
                    project.setSpace(space);
                    will(returnValue(project));

                    one(experimentDAO).tryFindByCodeAndProject(project, "E1");
                    will(returnValue(new ExperimentPE()));
                }
            });

        NewSamplesWithTypes containers =
                new NewSamplesWithTypes(containerType, Arrays.asList(sample("PLATE", "/S1/P1/E1",
                        null, "alpha:12")));
        NewSamplesWithTypes contained =
                new NewSamplesWithTypes(containedType, Arrays.asList(
                        sample("A1", null, "/S1/PLATE", "beta:12"),
                        sample("A2", null, "/S1/PLATE", "beta:42")));
        checker.checkNewSamples(Arrays.asList(containers, contained));

        assertThat(checker.getErrors().size(), is(0));
        context.assertIsSatisfied();
    }

    @Test
    public void testCheckNewSamplesWithUnknownType()
    {
        SampleType sampleType = new SampleType();
        sampleType.setCode("T1");
        prepareForAssertSampleTypeExists(sampleType.getCode(), null);

        checker.checkNewSamples(Arrays.asList(new NewSamplesWithTypes(sampleType, Arrays
                .asList(sample("/S1/A", null, null, "alpha:12")))));

        assertThat(checker.getErrors(), containsExactly("Unknown sample type: T1"));
        context.assertIsSatisfied();
    }

    @Test
    public void testCheckNewSamplesWithUnknownExperiment()
    {
        SampleType sampleType = new SampleType();
        sampleType.setCode("T1");
        prepareForAssertSampleTypeExists(sampleType.getCode(), sampleType("ALPHA"));
        context.checking(new Expectations()
            {
                {
                    one(projectDAO).tryFindProject("S1", "P1");
                    ProjectPE project = new ProjectPE();
                    project.setCode("P1");
                    SpacePE space = new SpacePE();
                    space.setCode("S1");
                    project.setSpace(space);
                    will(returnValue(project));

                    one(experimentDAO).tryFindByCodeAndProject(project, "E1");
                }
            });

        checker.checkNewSamples(Arrays.asList(new NewSamplesWithTypes(sampleType, Arrays
                .asList(sample("A", "/S1/P1/E1", null, "alpha:12")))));

        assertThat(checker.getErrors(), containsExactly("Unknown experiment: /S1/P1/E1"));
        context.assertIsSatisfied();
    }

    @Test
    public void testCheckNewSamplesWithUnknownExperimentBecauseOfUnknownProject()
    {
        SampleType sampleType = new SampleType();
        sampleType.setCode("T1");
        prepareForAssertSampleTypeExists(sampleType.getCode(), sampleType("ALPHA"));
        context.checking(new Expectations()
            {
                {
                    one(projectDAO).tryFindProject("S1", "P1");
                }
            });

        checker.checkNewSamples(Arrays.asList(new NewSamplesWithTypes(sampleType, Arrays
                .asList(sample("A", "/S1/P1/E1", null, "alpha:12")))));

        assertThat(checker.getErrors(),
                containsExactly("Unknown experiment because of unknown project: /S1/P1/E1"));
        context.assertIsSatisfied();
    }

    @Test
    public void testCheckNewSamplesWithUnknownSpace()
    {
        SampleType sampleType = new SampleType();
        sampleType.setCode("T1");
        prepareForAssertSampleTypeExists(sampleType.getCode(), sampleType("ALPHA"));
        context.checking(new Expectations()
            {
                {
                    one(spaceDAO).tryFindSpaceByCode("S1");
                }
            });

        checker.checkNewSamples(Arrays.asList(new NewSamplesWithTypes(sampleType, Arrays
                .asList(sample("A", null, "/S1/PLATE", "alpha:12")))));

        assertThat(checker.getErrors(), containsExactly("Unknown space: /S1"));
        context.assertIsSatisfied();
    }

    @Test
    public void testCheckNewSamplesWithUnknownContainerSample()
    {
        SampleType sampleType = new SampleType();
        sampleType.setCode("T1");
        prepareForAssertSampleTypeExists(sampleType.getCode(), sampleType("ALPHA"));
        context.checking(new Expectations()
            {
                {
                    one(sampleDAO).tryFindByCodeAndDatabaseInstance("PLATE");
                }
            });

        checker.checkNewSamples(Arrays.asList(new NewSamplesWithTypes(sampleType, Arrays
                .asList(sample("A", null, "/PLATE", "alpha:12")))));

        assertThat(checker.getErrors(), containsExactly("Unknown sample: /PLATE"));
        context.assertIsSatisfied();
    }

    @Test
    public void testCheckNewSamplesWithUnknownPropertyType()
    {
        SampleType sampleType = new SampleType();
        sampleType.setCode("T1");
        prepareForAssertSampleTypeExists(sampleType.getCode(), sampleType());

        checker.checkNewSamples(Arrays.asList(new NewSamplesWithTypes(sampleType, Arrays
                .asList(sample("/S1/A", null, null, "alpha:12")))));

        assertThat(checker.getErrors(),
                containsExactly("Sample type T1 has no property type ALPHA assigned."));
        context.assertIsSatisfied();
    }

    @Test
    public void testCheckNewSamplesWithMaterialProperties()
    {
        MaterialType type = new MaterialType();
        type.setCode("M1");
        MaterialTypePE materialType = materialType(type, "ALPHA");
        prepareForAssertMaterialTypeExists(type.getCode(), materialType);
        SampleType sampleType = new SampleType();
        sampleType.setCode("S1");
        prepareForAssertSampleTypeExists(sampleType.getCode(), sampleType(materialType, "MATERIAL"));
        context.checking(new Expectations()
            {
                {
                    one(materialDAO).tryFindMaterial(new MaterialIdentifier("B", "M1"));
                    will(returnValue(new MaterialPE()));
                }
            });

        checker.checkNewMaterials(Arrays.asList(new NewMaterialsWithTypes(type, Arrays
                .asList(material("A", "alpha:12")))));
        checker.checkNewSamples(Arrays.asList(new NewSamplesWithTypes(sampleType, Arrays.asList(
                sample("/S1/A1", null, null, "material:A"),
                sample("/S1/A2", null, null, "material:B"),
                sample("/S1/A3", null, null, "material:B")))));

        assertThat(checker.getErrors(), containsExactly(new String[0]));
        context.assertIsSatisfied();
    }

    @Test
    public void testCheckNewSamplesWithMaterialPropertOfAnyType()
    {
        MaterialType type = new MaterialType();
        type.setCode("M1");
        MaterialTypePE materialType = materialType(type, "ALPHA");
        prepareForAssertMaterialTypeExists(type.getCode(), materialType);
        SampleType sampleType = new SampleType();
        sampleType.setCode("S1");
        prepareForAssertSampleTypeExists(sampleType.getCode(),
                sampleType((MaterialTypePE) null, "MATERIAL"));
        context.checking(new Expectations()
            {
                {
                    one(materialDAO).tryFindMaterial(new MaterialIdentifier("B", "M1"));
                    will(returnValue(new MaterialPE()));
                }
            });

        checker.checkNewMaterials(Arrays.asList(new NewMaterialsWithTypes(type, Arrays
                .asList(material("A", "alpha:12")))));
        checker.checkNewSamples(Arrays.asList(new NewSamplesWithTypes(sampleType, Arrays.asList(
                sample("/S1/A1", null, null, "material:A (M1)"),
                sample("/S1/A2", null, null, "material:B (M1)"),
                sample("/S1/A3", null, null, "material:B (M1)")))));

        assertThat(checker.getErrors(), containsExactly(new String[0]));
        context.assertIsSatisfied();
    }

    @Test
    public void testCheckNewSamplesWithMaterialPropertyOfAnyTypeWithInvalidMaterialIdentifier()
    {
        SampleType sampleType = new SampleType();
        sampleType.setCode("S1");
        prepareForAssertSampleTypeExists(sampleType.getCode(),
                sampleType((MaterialTypePE) null, "MATERIAL"));

        checker.checkNewSamples(Arrays.asList(new NewSamplesWithTypes(sampleType, Arrays.asList(
                sample("/S1/A1", null, null, "material:A"),
                sample("/S1/A2", null, null, "material:B"),
                sample("/S1/A3", null, null, "material:B")))));

        assertEquals("[Material identifier not in the form "
                + "'<material code> (<material type code>)': A, "
                + "Material identifier not in the form '<material code> "
                + "(<material type code>)': B]", checker.getErrors().toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testCheckNewSamplesWithNonExistingMaterialProperties()
    {
        MaterialType type = new MaterialType();
        type.setCode("M1");
        MaterialTypePE materialType = materialType(type, "ALPHA");
        prepareForAssertMaterialTypeExists(type.getCode(), materialType);
        SampleType sampleType = new SampleType();
        sampleType.setCode("S1");
        prepareForAssertSampleTypeExists(sampleType.getCode(), sampleType(materialType, "MATERIAL"));
        context.checking(new Expectations()
            {
                {
                    one(materialDAO).tryFindMaterial(new MaterialIdentifier("B", "M1"));
                    will(returnValue(null));
                }
            });

        checker.checkNewMaterials(Arrays.asList(new NewMaterialsWithTypes(type, Arrays
                .asList(material("A", "alpha:12")))));
        checker.checkNewSamples(Arrays.asList(new NewSamplesWithTypes(sampleType, Arrays.asList(
                sample("/S1/A1", null, null, "material:A"),
                sample("/S1/A2", null, null, "material:B"),
                sample("/S1/A3", null, null, "material:B")))));

        assertThat(checker.getErrors(), containsExactly("Unknown material: B (M1)"));
    }

    @Test
    public void multipleErrors()
    {
        final SampleType sampleType = new SampleType();
        sampleType.setCode("T1");

        context.checking(new Expectations()
            {
                {
                    allowing(sampleTypeDAO).tryFindSampleTypeByCode("T1");
                    will(returnValue(sampleType()));
                    allowing(sampleTypeDAO).tryFindSampleTypeByCode("ALPHA");
                    will(returnValue(sampleType("ALPHA")));
                    one(sampleDAO).tryFindByCodeAndDatabaseInstance("PLATE");

                }
            });

        checker.checkNewSamples(Arrays.asList(new NewSamplesWithTypes(sampleType, Arrays.asList(
                sample("/S1/A", null, null, "alpha:12"), sample("A", null, "/PLATE", "alpha:12")))));

        assertThat(
                checker.getErrors(),
                containsExactly("Sample type T1 has no property type ALPHA assigned.",
                        "Unknown sample: /PLATE"));
        context.assertIsSatisfied();
    }

    private NewMaterial material(String code, String... properties)
    {
        NewMaterial newMaterial = new NewMaterial();
        newMaterial.setCode(code);
        newMaterial.setProperties(createProperties(properties));
        return newMaterial;
    }

    private MaterialTypePE materialType(MaterialType materialType, String... propertyTypes)
    {
        MaterialTypePE type = new MaterialTypePE();
        List<MaterialTypePropertyTypePE> list = new ArrayList<MaterialTypePropertyTypePE>();
        for (String propertyType : propertyTypes)
        {
            MaterialTypePropertyTypePE materialTypePropertyType = new MaterialTypePropertyTypePE();
            PropertyTypePE propertyTypePE = new PropertyTypePE();
            propertyTypePE.setCode(propertyType);
            DataTypePE dataType = new DataTypePE();
            dataType.setCode(DataTypeCode.VARCHAR);
            propertyTypePE.setType(dataType);
            materialTypePropertyType.setPropertyType(propertyTypePE);
            list.add(materialTypePropertyType);
        }
        type.setCode(materialType.getCode());
        type.setMaterialTypePropertyTypes(new HashSet<MaterialTypePropertyTypePE>(list));
        return type;
    }

    private void prepareForAssertMaterialTypeExists(final String materialTypeCode,
            final MaterialTypePE materialTypeOrNull)
    {
        context.checking(new Expectations()
            {
                {
                    one(materialTypeDAO).tryToFindEntityTypeByCode(materialTypeCode);
                    will(returnValue(materialTypeOrNull));
                }
            });
    }

    private NewSample sample(String sampleIdentifier, String experimentIdentifierOrNull,
            String containerIdentifierOrNull, String... properties)
    {
        NewSample newSample = new NewSample();
        newSample.setIdentifier(sampleIdentifier);
        newSample.setExperimentIdentifier(experimentIdentifierOrNull);
        newSample.setContainerIdentifier(containerIdentifierOrNull);
        newSample.setProperties(createProperties(properties));
        return newSample;
    }

    private SampleTypePE sampleType(String... propertyTypes)
    {
        return sampleType(DataTypeCode.VARCHAR, null, propertyTypes);
    }

    private SampleTypePE sampleType(MaterialTypePE materialType, String... propertyTypes)
    {
        return sampleType(DataTypeCode.MATERIAL, materialType, propertyTypes);
    }

    private SampleTypePE sampleType(DataTypeCode dataTypeCode, MaterialTypePE materialType,
            String... propertyTypes)
    {
        SampleTypePE type = new SampleTypePE();
        List<SampleTypePropertyTypePE> list = new ArrayList<SampleTypePropertyTypePE>();
        for (String propertyType : propertyTypes)
        {
            SampleTypePropertyTypePE sampleTypePropertyType = new SampleTypePropertyTypePE();
            PropertyTypePE propertyTypePE = new PropertyTypePE();
            propertyTypePE.setCode(propertyType);
            DataTypePE dataType = new DataTypePE();
            dataType.setCode(dataTypeCode);
            if (dataTypeCode.equals(DataTypeCode.MATERIAL))
            {
                propertyTypePE.setMaterialType(materialType);
            }
            propertyTypePE.setType(dataType);
            sampleTypePropertyType.setPropertyType(propertyTypePE);
            list.add(sampleTypePropertyType);
        }
        type.setSampleTypePropertyTypes(new HashSet<SampleTypePropertyTypePE>(list));
        return type;
    }

    private void prepareForAssertSampleTypeExists(final String sampleTypeCode,
            final SampleTypePE sampleTypeOrNull)
    {
        context.checking(new Expectations()
            {
                {
                    one(sampleTypeDAO).tryFindSampleTypeByCode(sampleTypeCode);
                    will(returnValue(sampleTypeOrNull));
                }
            });
    }

    private IEntityProperty[] createProperties(String... properties)
    {
        IEntityProperty[] entityProperties = new IEntityProperty[properties.length];
        for (int i = 0; i < properties.length; i++)
        {
            String prop = properties[i];
            int lastIndexOfColon = prop.lastIndexOf(':');
            String key = prop.substring(0, lastIndexOfColon);
            String value = prop.substring(lastIndexOfColon + 1);
            EntityProperty property = new EntityProperty();
            PropertyType propertyType = new PropertyType();
            propertyType.setCode(key);
            property.setPropertyType(propertyType);
            property.setValue(value);
            entityProperties[i] = property;
        }
        return entityProperties;
    }

    private <T> Matcher<Collection<T>> containsExactly(T... t)
    {
        return new CollectionContainsExactlyMatcher<T>(t);
    }

    private static class CollectionContainsExactlyMatcher<T> extends TypeSafeMatcher<Collection<T>>
    {

        private List<T> expected;

        public CollectionContainsExactlyMatcher(T... expected)
        {
            this.expected = Arrays.asList(expected);
        }

        @Override
        public void describeTo(Description description)
        {
            description.appendText("A collection containing exactly items " + expected.toString());
        }

        @Override
        public boolean matchesSafely(Collection<T> collection)
        {
            return collection.containsAll(expected) && expected.containsAll(collection);
        }

    }

}
