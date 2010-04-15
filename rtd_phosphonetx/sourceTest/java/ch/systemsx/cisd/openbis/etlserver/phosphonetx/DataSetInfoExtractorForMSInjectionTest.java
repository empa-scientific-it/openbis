/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.etlserver.phosphonetx;

import static ch.systemsx.cisd.openbis.etlserver.phosphonetx.DataSetInfoExtractorForMSInjection.DATA_SET_PROPERTIES_FILE;
import static ch.systemsx.cisd.openbis.etlserver.phosphonetx.DataSetInfoExtractorForMSInjection.DATA_SET_TYPE_KEY;
import static ch.systemsx.cisd.openbis.etlserver.phosphonetx.DataSetInfoExtractorForMSInjection.EXPERIMENT_CODE_KEY;
import static ch.systemsx.cisd.openbis.etlserver.phosphonetx.DataSetInfoExtractorForMSInjection.EXPERIMENT_TYPE_CODE;
import static ch.systemsx.cisd.openbis.etlserver.phosphonetx.DataSetInfoExtractorForMSInjection.MS_INJECTION_PROPERTIES_FILE;
import static ch.systemsx.cisd.openbis.etlserver.phosphonetx.DataSetInfoExtractorForMSInjection.PROJECT_CODE_KEY;
import static ch.systemsx.cisd.openbis.etlserver.phosphonetx.DataSetInfoExtractorForMSInjection.SAMPLE_CODE_KEY;
import static ch.systemsx.cisd.openbis.etlserver.phosphonetx.DataSetInfoExtractorForMSInjection.SAMPLE_TYPE_CODE;
import static ch.systemsx.cisd.openbis.etlserver.phosphonetx.DataSetInfoExtractorForMSInjection.USER_KEY;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.etlserver.IDataSetInfoExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypeWithVocabularyTerms;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class DataSetInfoExtractorForMSInjectionTest extends AbstractFileSystemTestCase
{
    private static final String PROJECT_CODE = "MS2";
    private static final String EXPERIMENT_CODE = "2010-02";
    private static final String SAMPLE_CODE = "U09-1242";
    private static final String SAMPLE_IDENTIFIER =
            DatabaseInstanceIdentifier.Constants.IDENTIFIER_SEPARATOR + Constants.MS_DATA_SPACE
                    + DatabaseInstanceIdentifier.Constants.IDENTIFIER_SEPARATOR + SAMPLE_CODE;
    private static final String EXPERIMENT_IDENTIFIER =
            DatabaseInstanceIdentifier.Constants.IDENTIFIER_SEPARATOR + Constants.MS_DATA_SPACE
                    + DatabaseInstanceIdentifier.Constants.IDENTIFIER_SEPARATOR + PROJECT_CODE
                    + DatabaseInstanceIdentifier.Constants.IDENTIFIER_SEPARATOR + EXPERIMENT_CODE;
    private static final long EXPERIMENT_ID = 42;
    
    private Mockery context;
    private IEncapsulatedOpenBISService service;
    private IDataSetInfoExtractor extractor;
    private File dataSet;
    
    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        extractor =
                new DataSetInfoExtractorForMSInjection(service);
        dataSet = new File(workingDirectory, "data-set");
        dataSet.mkdirs();
    }
    
    @AfterMethod
    public void afterMethod()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }
    
    @Test
    public void testMissingPropertiesFile()
    {
        try
        {
            extractor.getDataSetInformation(dataSet, service);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Missing properties file '" + MS_INJECTION_PROPERTIES_FILE + "'.", ex
                    .getMessage());
        }
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testPropertiesFileIsAFolder()
    {
        new File(dataSet, MS_INJECTION_PROPERTIES_FILE).mkdir();
        try
        {
            extractor.getDataSetInformation(dataSet, service);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Properties file '" + MS_INJECTION_PROPERTIES_FILE
                    + "' is a folder.", ex.getMessage());
        }
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testMissingSampleCode()
    {
        Properties properties = new Properties();
        save(properties, MS_INJECTION_PROPERTIES_FILE);
        
        try
        {
            extractor.getDataSetInformation(dataSet, service);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Given key '" + SAMPLE_CODE_KEY + "' not found in properties '[]'", ex
                    .getMessage());
        }
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testMissingProjectCode()
    {
        Properties properties = new Properties();
        
        properties.setProperty(SAMPLE_CODE_KEY, SAMPLE_CODE);
        save(properties, MS_INJECTION_PROPERTIES_FILE);
        prepareGetSampleType();
        
        try
        {
            extractor.getDataSetInformation(dataSet, service);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Given key '" + PROJECT_CODE_KEY + "' not found in properties '["
                    + SAMPLE_CODE_KEY + "]'", ex.getMessage());
        }
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testMissingExperimentCode()
    {
        Properties properties = new Properties();
        properties.setProperty(SAMPLE_CODE_KEY, SAMPLE_CODE);
        properties.setProperty(PROJECT_CODE_KEY, PROJECT_CODE);
        save(properties, MS_INJECTION_PROPERTIES_FILE);
        prepareGetSampleType();
        
        try
        {
            extractor.getDataSetInformation(dataSet, service);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Given key '" + EXPERIMENT_CODE_KEY + "' not found in properties '["
                    + PROJECT_CODE_KEY + ", " + SAMPLE_CODE_KEY + "]'", ex.getMessage());
        }
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testMissingDataSetPropertiesFile()
    {
        Properties sampleProperties = new Properties();
        sampleProperties.setProperty(SAMPLE_CODE_KEY, SAMPLE_CODE);
        sampleProperties.setProperty(PROJECT_CODE_KEY, PROJECT_CODE);
        sampleProperties.setProperty(EXPERIMENT_CODE_KEY, EXPERIMENT_CODE);
        sampleProperties.setProperty(USER_KEY, "user1");
        save(sampleProperties, MS_INJECTION_PROPERTIES_FILE);
        SampleTypePropertyType pt = createPropertyType(SAMPLE_CODE_KEY, true);
        prepareGetExperimentAndGetSampleType(true, pt);
        prepareRegisterSample();
        
        try
        {
            extractor.getDataSetInformation(dataSet, service);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Missing properties file '" + DATA_SET_PROPERTIES_FILE + "'.", ex.getMessage());
        }
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testMissingDataSetType()
    {
        Properties sampleProperties = new Properties();
        sampleProperties.setProperty(SAMPLE_CODE_KEY, SAMPLE_CODE);
        sampleProperties.setProperty(PROJECT_CODE_KEY, PROJECT_CODE);
        sampleProperties.setProperty(EXPERIMENT_CODE_KEY, EXPERIMENT_CODE);
        sampleProperties.setProperty(USER_KEY, "user1");
        save(sampleProperties, MS_INJECTION_PROPERTIES_FILE);
        Properties dataSetProperties = new Properties();
        save(dataSetProperties, DATA_SET_PROPERTIES_FILE);
        SampleTypePropertyType pt = createPropertyType(SAMPLE_CODE_KEY, true);
        prepareGetExperimentAndGetSampleType(true, pt);
        prepareRegisterSample();
        
        try
        {
            extractor.getDataSetInformation(dataSet, service);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Given key '" + DATA_SET_TYPE_KEY + "' not found in properties '[]'", ex
                    .getMessage());
        }
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void test()
    {
        Properties sampleProperties = new Properties();
        sampleProperties.setProperty(PROJECT_CODE_KEY, PROJECT_CODE);
        sampleProperties.setProperty(SAMPLE_CODE_KEY, SAMPLE_CODE);
        sampleProperties.setProperty(EXPERIMENT_CODE_KEY, EXPERIMENT_CODE);
        sampleProperties.setProperty(USER_KEY, "user1");
        sampleProperties.setProperty("TEMPERATURE", "47.11");
        save(sampleProperties, MS_INJECTION_PROPERTIES_FILE);
        Properties dataSetProperties = new Properties();
        dataSetProperties.setProperty(DATA_SET_TYPE_KEY, "MZXML_DATA");
        dataSetProperties.setProperty("CENTROID", "true");
        dataSetProperties.setProperty("BLABLA", "blub");
        save(dataSetProperties, DATA_SET_PROPERTIES_FILE);
        SampleTypePropertyType pt1 = createPropertyType(SAMPLE_CODE_KEY, true);
        SampleTypePropertyType pt2 = createPropertyType("VOLUME", false);
        prepareGetExperimentAndGetSampleType(false, pt1, pt2);
        prepareRegisterSample();
        prepareGetDataSetType(createDataSetPropertyType("CENTROID", false));
        
        DataSetInformation info = extractor.getDataSetInformation(dataSet, service);
        
        assertEquals(Constants.MS_DATA_SPACE, info.getSpaceCode());
        assertEquals(SAMPLE_CODE, info.getSampleCode());
        assertEquals(null, info.getExperimentIdentifier());
        List<NewProperty> dProps = info.getDataSetProperties();
        assertEquals(1, dProps.size());
        assertEquals("CENTROID", dProps.get(0).getPropertyCode());
        assertEquals("true", dProps.get(0).getValue());
        
        context.assertIsSatisfied();
    }

    @Test
    public void testMissingMandatoryProperties()
    {
        Properties properties = new Properties();
        properties.setProperty(PROJECT_CODE_KEY, PROJECT_CODE);
        properties.setProperty(SAMPLE_CODE_KEY, SAMPLE_CODE);
        properties.setProperty(EXPERIMENT_CODE_KEY, EXPERIMENT_CODE);
        save(properties, MS_INJECTION_PROPERTIES_FILE);
        SampleTypePropertyType pt1 = createPropertyType(SAMPLE_CODE_KEY, true);
        SampleTypePropertyType pt2 = createPropertyType("VOLUME", true);
        SampleTypePropertyType pt3 = createPropertyType("TEMPERATURE", true);
        prepareGetExperimentAndGetSampleType(true, pt1, pt2, pt3);
        
        try
        {
            extractor.getDataSetInformation(dataSet, service);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("The following mandatory properties are missed: [VOLUME, TEMPERATURE]", ex
                    .getMessage());
        }

        context.assertIsSatisfied();
    }

    private void prepareGetExperimentAndGetSampleType(
            final boolean experimentExists, final SampleTypePropertyType... sampleTypePropertyTypes)
    {
        context.checking(new Expectations()
            {
                {
                    ExperimentIdentifier identifier =
                            new ExperimentIdentifier(null, Constants.MS_DATA_SPACE, PROJECT_CODE,
                                    EXPERIMENT_CODE);
                    one(service).tryToGetExperiment(identifier);
                    Experiment experiment = new Experiment();
                    experiment.setId(EXPERIMENT_ID);
                    will(returnValue(experimentExists ? experiment : null));

                    if (experimentExists == false)
                    {
                        one(service).registerExperiment(
                                new NewExperiment(identifier.toString(), EXPERIMENT_TYPE_CODE));
                    }
                }
            });
        prepareGetSampleType(sampleTypePropertyTypes);
    }

    private void prepareGetSampleType(final SampleTypePropertyType... sampleTypePropertyTypes)
    {
        context.checking(new Expectations()
            {
                {
                    one(service).getSampleType(DataSetInfoExtractorForMSInjection.SAMPLE_TYPE_CODE);
                    SampleType sampleType = new SampleType();
                    sampleType.setCode(DataSetInfoExtractorForMSInjection.SAMPLE_TYPE_CODE);
                    sampleType.setSampleTypePropertyTypes(Arrays.asList(sampleTypePropertyTypes));
                    will(returnValue(sampleType));
                }
            });
    }

    private void prepareGetDataSetType(final DataSetTypePropertyType... types)
    {
        context.checking(new Expectations()
            {
                {
                    one(service).getDataSetType("MZXML_DATA");
                    DataSetTypeWithVocabularyTerms result = new DataSetTypeWithVocabularyTerms();
                    DataSetType dataSetType = new DataSetType("MZXML_DATA");
                    dataSetType.setDataSetTypePropertyTypes(Arrays.asList(types));
                    result.setDataSetType(dataSetType);
                    will(returnValue(result));
                }
            });
    }

    private void prepareRegisterSample()
    {
        context.checking(new Expectations()
            {
                {
                    one(service).registerSample(with(new BaseMatcher<NewSample>()
                        {
                            public boolean matches(Object item)
                            {
                                if (item instanceof NewSample)
                                {
                                    NewSample sample = (NewSample) item;
                                    assertEquals(SAMPLE_TYPE_CODE, sample.getSampleType().getCode());
                                    assertEquals(SAMPLE_IDENTIFIER, sample.getIdentifier());
                                    assertEquals(EXPERIMENT_IDENTIFIER, sample.getExperimentIdentifier());
                                    IEntityProperty[] properties = sample.getProperties();
                                    Map<String, IEntityProperty> map =
                                            new HashMap<String, IEntityProperty>();
                                    for (IEntityProperty property : properties)
                                    {
                                        map.put(property.getPropertyType().getCode(), property);
                                    }

                                    assertEquals(SAMPLE_CODE, map.get(SAMPLE_CODE_KEY).getValue());
                                    assertEquals(1, map.size());
                                    return true;
                                }
                                return false;
                            }

                            public void describeTo(Description description)
                            {
                                description.appendText(SAMPLE_IDENTIFIER);
                            }
                        }), with("user1"));
                }
            });
    }
    
    private void save(Properties properties, String fileName)
    {
        File propertiesFile = new File(dataSet, fileName);
        FileOutputStream outputStream = null;
        try
        {
            outputStream = new FileOutputStream(propertiesFile);
            properties.store(outputStream, null);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            IOUtils.closeQuietly(outputStream);
        }
    }

    private SampleTypePropertyType createPropertyType(String key, boolean mandatory)
    {
        SampleTypePropertyType stpt = new SampleTypePropertyType();
        stpt.setMandatory(mandatory);
        PropertyType propertyType = new PropertyType();
        propertyType.setCode(key);
        stpt.setPropertyType(propertyType);
        return stpt;
    }
    
    private DataSetTypePropertyType createDataSetPropertyType(String key, boolean mandatory)
    {
        DataSetTypePropertyType etpt = new DataSetTypePropertyType();
        etpt.setMandatory(mandatory);
        PropertyType propertyType = new PropertyType();
        propertyType.setCode(key);
        etpt.setPropertyType(propertyType);
        return etpt;
    }
}