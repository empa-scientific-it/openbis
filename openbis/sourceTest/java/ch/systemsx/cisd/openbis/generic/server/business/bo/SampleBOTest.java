/*
 * Copyright 2007 ETH Zuerich, CISD
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

import static ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool.EXAMPLE_GROUP;
import static ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool.EXAMPLE_PERSON;
import static ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool.EXAMPLE_SESSION;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDatabaseInstanceDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IGroupDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleToRegisterDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.IdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityDataType;
import ch.systemsx.cisd.openbis.generic.shared.dto.types.SampleTypeCode;

/**
 * Test cases for corresponding {@link SampleBO} class.
 * 
 * @author Franz-Josef Elmer
 */
public final class SampleBOTest
{
    private static final String DILUTION_PLATE = SampleTypeCode.DILUTION_PLATE.getCode();

    private static final String MASTER_PLATE = SampleTypeCode.MASTER_PLATE.getCode();

    private static final String DEFAULT_SAMPLE_CODE = "xx";

    private Mockery context;

    private IDAOFactory daoFactory;

    private ISampleDAO sampleDAO;

    private ISampleTypeDAO sampleTypeDAO;

    private IEntityPropertiesConverter propertiesConverter;

    private IGroupDAO groupDAO;

    private IDatabaseInstanceDAO databaseInstanceDAO;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        propertiesConverter = context.mock(IEntityPropertiesConverter.class);
        daoFactory = context.mock(IDAOFactory.class);
        sampleDAO = context.mock(ISampleDAO.class);
        sampleTypeDAO = context.mock(ISampleTypeDAO.class);
        groupDAO = context.mock(IGroupDAO.class);
        databaseInstanceDAO = context.mock(IDatabaseInstanceDAO.class);
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testGetUndefinedSample()
    {
        try
        {
            createSampleBO().getSample();
            fail("UserFailureException expected");
        } catch (final IllegalStateException e)
        {
            assertEquals("Unloaded sample.", e.getMessage());
        }
    }

    static SamplePE createSample(final String sampleCode)
    {
        return createSample(sampleCode, "sample-type-code");
    }

    private static SamplePE createSample(final String sampleCode, final String sampleTypeCode)
    {
        return createSample(SampleIdentifier.createHomeGroup(sampleCode), sampleTypeCode);
    }

    private static SamplePE createSample(final SampleIdentifier sampleIdentifier,
            final String sampleTypeCode)
    {
        final SamplePE sample = new SamplePE();
        sample.setRegistrator(EXAMPLE_PERSON);
        final SampleTypePE sampleTypeDTO = new SampleTypePE();
        sampleTypeDTO.setCode(sampleTypeCode);
        sample.setSampleType(sampleTypeDTO);
        return sample;
    }

    private final static SampleIdentifier getSampleIdentifier(final String code)
    {
        return new SampleIdentifier(IdentifierHelper.createIdentifier(EXAMPLE_GROUP), code);
    }

    @Test
    public final void testDefineSampleHappyCase()
    {
        final SampleIdentifier sampleIdentifier = getSampleIdentifier(DEFAULT_SAMPLE_CODE);
        final SampleToRegisterDTO newSample = new SampleToRegisterDTO();
        newSample.setSampleIdentifier(sampleIdentifier);
        newSample.setSampleTypeCode(SampleTypeCode.DILUTION_PLATE.getCode());

        final SampleIdentifier generatedFromIdentifier = getSampleIdentifier("SAMPLE_GENERATOR");
        newSample.setGeneratorParent(generatedFromIdentifier);

        final SampleIdentifier containerIdentifier = getSampleIdentifier("SAMPLE_CONTAINER");
        newSample.setContainerParent(containerIdentifier);

        newSample.setProperties(SimpleEntityProperty.EMPTY_ARRAY);

        final SamplePE generatedFrom = new SamplePE();
        generatedFrom.setRegistrator(EXAMPLE_PERSON);
        generatedFrom.setGroup(EXAMPLE_GROUP);
        generatedFrom.setCode("SAMPLE_GENERATOR");

        final SamplePE container = new SamplePE();
        container.setRegistrator(EXAMPLE_PERSON);
        container.setGroup(EXAMPLE_GROUP);
        container.setCode("SAMPLE_CONTAINER");

        final SampleTypePE sampleType = new SampleTypePE();
        sampleType.setCode(DILUTION_PLATE);

        final SamplePE samplePE = new SamplePE();
        samplePE.setRegistrator(EXAMPLE_PERSON);
        samplePE.setGeneratedFrom(generatedFrom);
        samplePE.setContainer(container);
        samplePE.setSampleType(sampleType);

        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getSampleDAO();
                    will(returnValue(sampleDAO));

                    ManagerTestTool.prepareFindGroup(this, daoFactory, groupDAO,
                            databaseInstanceDAO);

                    one(sampleDAO).tryFindByCodeAndGroup(generatedFromIdentifier.getSampleCode(),
                            EXAMPLE_GROUP);
                    will(returnValue(generatedFrom));

                    one(sampleDAO).tryFindByCodeAndGroup(containerIdentifier.getSampleCode(),
                            EXAMPLE_GROUP);
                    will(returnValue(container));

                    one(daoFactory).getSampleTypeDAO();
                    will(returnValue(sampleTypeDAO));
                    one(sampleTypeDAO).tryFindSampleTypeByCode(DILUTION_PLATE);
                    will(returnValue(sampleType));

                    one(propertiesConverter).convertProperties(newSample.getProperties(),
                            DILUTION_PLATE, EXAMPLE_PERSON);
                    will(returnValue(new ArrayList<SamplePropertyPE>()));
                }
            });

        final SampleBO sampleBO = createSampleBO();
        sampleBO.define(newSample);

        final SamplePE sample = sampleBO.getSample();
        assertEquals(sampleIdentifier.toString(), sample.getSampleIdentifier().toString());
        assertEquals(EXAMPLE_PERSON, sample.getRegistrator());
        assertSame(sampleType, sample.getSampleType());
        assertEquals(container, sample.getContainer());
        assertEquals(generatedFrom, sample.getGeneratedFrom());

        context.assertIsSatisfied();
    }

    @Test
    public void testDefineAndSaveSampleWithProperties()
    {
        final SampleToRegisterDTO newSample = new SampleToRegisterDTO();

        newSample.setSampleIdentifier(getSampleIdentifier(DEFAULT_SAMPLE_CODE));
        newSample.setSampleTypeCode(SampleTypeCode.MASTER_PLATE.getCode());

        final SampleTypePE sampleType = new SampleTypePE();
        sampleType.setCode(MASTER_PLATE);
        sampleType.setId(new Long(21L));
        final SimpleEntityProperty property =
                new SimpleEntityProperty("color", "color", EntityDataType.VARCHAR, "blue");
        newSample.setProperties(new SimpleEntityProperty[]
            { property });
        final SamplePropertyPE sampleProperty = new SamplePropertyPE();
        sampleProperty.setRegistrator(EXAMPLE_SESSION.tryGetPerson());
        final SampleTypePropertyTypePE sampleTypePropertyType = new SampleTypePropertyTypePE();
        sampleProperty.setEntityTypePropertyType(sampleTypePropertyType);

        context.checking(new Expectations()
            {
                {
                    ManagerTestTool.prepareFindGroup(this, daoFactory, groupDAO,
                            databaseInstanceDAO);

                    one(daoFactory).getSampleTypeDAO();
                    will(returnValue(sampleTypeDAO));

                    one(sampleTypeDAO).tryFindSampleTypeByCode(MASTER_PLATE);
                    will(returnValue(sampleType));

                    one(propertiesConverter).convertProperties(newSample.getProperties(),
                            MASTER_PLATE, EXAMPLE_PERSON);
                    final List<SamplePropertyPE> set = new ArrayList<SamplePropertyPE>();
                    set.add(sampleProperty);
                    will(returnValue(set));

                    one(daoFactory).getSampleDAO();
                    will(returnValue(sampleDAO));

                    one(sampleDAO).createSample(with(new BaseMatcher<SamplePE>()
                        {
                            public void describeTo(final Description description)
                            {
                            }

                            public boolean matches(final Object item)
                            {
                                if (item instanceof SamplePE == false)
                                {
                                    return false;
                                }
                                final SamplePE sample = (SamplePE) item;
                                assertEquals(EXAMPLE_SESSION.tryGetHomeGroupId(), sample.getGroup()
                                        .getId());
                                assertNull(sample.getDatabaseInstance());
                                assertEquals(newSample.getSampleIdentifier(), sample
                                        .getSampleIdentifier());
                                assertEquals(EXAMPLE_PERSON, sample.getRegistrator());
                                return true;
                            }
                        }));

                }
            });

        final SampleBO sampleBO = createSampleBO();
        sampleBO.define(newSample);

        final Set<SamplePropertyPE> properties = sampleBO.getSample().getProperties();
        assertEquals(1, properties.size());
        assertSame(sampleProperty, properties.iterator().next());

        sampleBO.save();

        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterSampleWithUnknownContainerParent()
    {
        final SampleToRegisterDTO sample = new SampleToRegisterDTO();
        sample.setSampleIdentifier(getSampleIdentifier(DEFAULT_SAMPLE_CODE));
        sample.setSampleTypeCode(SampleTypeCode.DILUTION_PLATE.getCode());
        sample.setContainerParent(getSampleIdentifier(""));

        context.checking(new Expectations()
            {
                {
                    ManagerTestTool.prepareFindGroup(this, daoFactory, groupDAO,
                            databaseInstanceDAO);

                    one(daoFactory).getSampleTypeDAO();
                    will(returnValue(sampleTypeDAO));

                    one(sampleTypeDAO).tryFindSampleTypeByCode(
                            SampleTypeCode.DILUTION_PLATE.getCode());
                    will(returnValue(new SampleTypePE()));

                    one(propertiesConverter).convertProperties(null, null, EXAMPLE_PERSON);

                    one(daoFactory).getSampleDAO();
                    will(returnValue(sampleDAO));

                    one(sampleDAO).tryFindByCodeAndGroup("", EXAMPLE_SESSION.tryGetHomeGroup());
                    will(returnValue(null));
                }
            });
        try
        {
            createSampleBO().define(sample);
        } catch (final UserFailureException ex)
        {
            assertEquals(
                    "No sample could be found for identifier \'my database instance:/MY GROUP/\'.",
                    ex.getMessage());
        }
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterSampleWithUnknownGeneratedFromSample()
    {
        final SampleToRegisterDTO sample = new SampleToRegisterDTO();
        sample.setSampleIdentifier(getSampleIdentifier(DEFAULT_SAMPLE_CODE));
        sample.setSampleTypeCode(SampleTypeCode.DILUTION_PLATE.getCode());
        sample.setGeneratorParent(getSampleIdentifier(""));

        context.checking(new Expectations()
            {
                {
                    ManagerTestTool.prepareFindGroup(this, daoFactory, groupDAO,
                            databaseInstanceDAO);

                    one(daoFactory).getSampleTypeDAO();
                    will(returnValue(sampleTypeDAO));

                    one(sampleTypeDAO).tryFindSampleTypeByCode(
                            SampleTypeCode.DILUTION_PLATE.getCode());
                    will(returnValue(new SampleTypePE()));

                    one(propertiesConverter).convertProperties(null, null, EXAMPLE_PERSON);

                    one(daoFactory).getSampleDAO();
                    will(returnValue(sampleDAO));

                    one(sampleDAO).tryFindByCodeAndGroup("", EXAMPLE_SESSION.tryGetHomeGroup());
                    will(returnValue(null));
                }
            });
        try
        {
            createSampleBO().define(sample);
        } catch (final UserFailureException ex)
        {
            assertEquals(
                    "No sample could be found for identifier \'my database instance:/MY GROUP/\'.",
                    ex.getMessage());
        }
        context.assertIsSatisfied();
    }

    private SampleBO createSampleBO()
    {
        return new SampleBO(daoFactory, propertiesConverter, EXAMPLE_SESSION);
    }

}
