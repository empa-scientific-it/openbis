/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cina.dss.bundle.registrators;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.cina.shared.constants.CinaConstants;
import ch.systemsx.cisd.etlserver.IDataSetHandlerRpc;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypeWithVocabularyTerms;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public abstract class CinaBundleRegistrationTest extends AbstractFileSystemTestCase
{

    protected static final String TEST_USER_NAME = "test";

    private static final String SESSION_TOKEN = "session-token";

    private static final String RAW_IMAGES_DATA_SET_CODE = "RAW_IMAGES_DATA_SET_CODE";

    private static final String METADATA_DATA_SET_CODE = "METADATA_DATA_SET_CODE";

    private static final String SPACE_CODE = "SPACE";

    private static final String PROJECT_CODE = "PROJECT";

    private static final String EXPERIMENT_CODE = "EXP-1";

    protected static final String EXPERIMENT_IDENTIFIER = "/" + SPACE_CODE + "/"
            + PROJECT_CODE + "/" + EXPERIMENT_CODE;

    private static final String GRID_SAMPLE_CODE = "GRID-CODE";

    protected static final String GRID_SAMPLE_IDENTIFIER = "/" + SPACE_CODE + "/"
            + GRID_SAMPLE_CODE;

    private static final String COLLECTION_SAMPLE_CODE = "REPLICA54";

    protected static final String COLLECTION_SAMPLE_IDENTIFIER = "/" + SPACE_CODE + "/"
            + COLLECTION_SAMPLE_CODE;

    private static final String BUNDLE_METADATA_DATA_SET_CODE = "BUNDLE_METADATA";

    public static abstract class MatcherNoDesc<T> extends BaseMatcher<T>
    {

        @Override
        public void describeTo(Description description)
        {

        }

    }

    protected Mockery context;

    protected IEncapsulatedOpenBISService openbisService;

    protected IDataSetHandlerRpc delegator;

    private PhysicalDataSet dataSet;

    private DataSetTypeWithVocabularyTerms rawImagesDataSetTypeWithTerms;

    private DataSetTypeWithVocabularyTerms metadataDataSetTypeWithTerms;

    private DataSetTypeWithVocabularyTerms imageDataSetTypeWithTerms;

    /**
     *
     *
     */
    public CinaBundleRegistrationTest()
    {
        super();
    }

    /**
     * @param cleanAfterMethod
     */
    public CinaBundleRegistrationTest(boolean cleanAfterMethod)
    {
        super(cleanAfterMethod);
    }

    @Override
    @BeforeMethod
    public void setUp() throws IOException
    {
        super.setUp();

        context = new Mockery();
        openbisService = context.mock(IEncapsulatedOpenBISService.class);
        delegator = context.mock(IDataSetHandlerRpc.class);
    }

    @AfterMethod
    public void tearDown()
    {
        context.assertIsSatisfied();
    }

    protected void setupOpenBisExpectations()
    {
        final SampleType gridPrepSampleType = new SampleType();
        gridPrepSampleType.setCode(CinaConstants.GRID_PREP_SAMPLE_TYPE_CODE);
        gridPrepSampleType.setAutoGeneratedCode(true);
        gridPrepSampleType.setGeneratedCodePrefix("GridPrep-");

        final SampleType replicaSampleType = new SampleType();
        replicaSampleType.setCode(CinaConstants.COLLECTION_SAMPLE_TYPE_CODE);
        replicaSampleType.setAutoGeneratedCode(true);
        replicaSampleType.setGeneratedCodePrefix("Replica-");

        DataSetType dataSetType = new DataSetType(CinaConstants.IMAGE_DATA_SET_TYPE_CODE);
        rawImagesDataSetTypeWithTerms = new DataSetTypeWithVocabularyTerms();
        rawImagesDataSetTypeWithTerms.setDataSetType(dataSetType);

        dataSetType = new DataSetType(CinaConstants.METADATA_DATA_SET_TYPE_CODE);
        metadataDataSetTypeWithTerms = new DataSetTypeWithVocabularyTerms();
        metadataDataSetTypeWithTerms.setDataSetType(dataSetType);

        dataSetType = new DataSetType(CinaConstants.IMAGE_DATA_SET_TYPE_CODE);
        imageDataSetTypeWithTerms = new DataSetTypeWithVocabularyTerms();
        imageDataSetTypeWithTerms.setDataSetType(dataSetType);

        dataSet = new PhysicalDataSet();
        dataSet.setCode("1");

        // set up the expectations
        context.checking(new Expectations()
            {
                {
                    one(openbisService).getSampleType(CinaConstants.GRID_PREP_SAMPLE_TYPE_CODE);
                    will(returnValue(gridPrepSampleType));
                    one(openbisService).getSampleType(CinaConstants.COLLECTION_SAMPLE_TYPE_CODE);
                    will(returnValue(replicaSampleType));
                    one(openbisService).getDataSetType(CinaConstants.RAW_IMAGES_DATA_SET_TYPE_CODE);
                    will(returnValue(rawImagesDataSetTypeWithTerms));
                    one(openbisService).getDataSetType(CinaConstants.METADATA_DATA_SET_TYPE_CODE);
                    will(returnValue(metadataDataSetTypeWithTerms));
                    one(openbisService).getDataSetType(CinaConstants.IMAGE_DATA_SET_TYPE_CODE);
                    will(returnValue(imageDataSetTypeWithTerms));
                    // one(openbisService).tryGetDataSet("session-token", externalData.getCode());
                    // will(returnValue(externalData));
                }
            });
    }

    protected void setupExistingGridPrepExpectations()
    {
        final Sample sample = new Sample();
        Experiment exp = new Experiment();
        exp.setIdentifier(EXPERIMENT_IDENTIFIER);
        sample.setExperiment(exp);
        sample.setIdentifier(GRID_SAMPLE_IDENTIFIER);

        // set up the expectations
        context.checking(new Expectations()
            {
                {
                    one(openbisService).tryGetSampleWithExperiment(
                            with(new SampleIdentifierFactory(GRID_SAMPLE_IDENTIFIER)
                                    .createIdentifier()));
                    will(returnValue(sample));
                }
            });
    }

    protected void setupExistingCollectionSampleExpectations()
    {
        final Sample sample = new Sample();
        Experiment exp = new Experiment();
        exp.setIdentifier(EXPERIMENT_IDENTIFIER);
        exp.setCode(EXPERIMENT_CODE);
        Project project = new Project();
        project.setCode(PROJECT_CODE);
        Space space = new Space();
        space.setCode(SPACE_CODE);
        DatabaseInstance dbInstance = new DatabaseInstance();
        project.setSpace(space);
        exp.setProject(project);
        sample.setId((long) 1);
        sample.setExperiment(exp);
        sample.setIdentifier(COLLECTION_SAMPLE_IDENTIFIER);

        // set up the expectations
        context.checking(new Expectations()
            {
                {
                    // Get the sample
                    one(openbisService).tryGetSampleWithExperiment(
                            with(new SampleIdentifierFactory(COLLECTION_SAMPLE_IDENTIFIER)
                                    .createIdentifier()));
                    will(returnValue(sample));

                    // Update it with new data
                    one(openbisService).updateSample(with(new MatcherNoDesc<SampleUpdatesDTO>()
                        {
                            @Override
                            public boolean matches(Object item)
                            {
                                if (item instanceof SampleUpdatesDTO)
                                {
                                    SampleUpdatesDTO sampleUpdatesDto = (SampleUpdatesDTO) item;
                                    assertEquals(COLLECTION_SAMPLE_IDENTIFIER, sampleUpdatesDto
                                            .getSampleIdentifier().toString());
                                    assertEquals(EXPERIMENT_IDENTIFIER, sampleUpdatesDto
                                            .getExperimentIdentifierOrNull().toString());
                                    List<IEntityProperty> properties =
                                            sampleUpdatesDto.getProperties();

                                    assertTrue("The update should include properties.",
                                            properties.size() > 0);
                                    for (IEntityProperty property : properties)
                                    {
                                        if (property.getPropertyType().getCode()
                                                .equals(CinaConstants.DESCRIPTION_PROPERTY_CODE))
                                        {
                                            assertEquals(
                                                    "This replica is a test for imported MRC files",
                                                    property.getValue());
                                        }

                                        if (property.getPropertyType().getCode()
                                                .equals(CinaConstants.CREATOR_EMAIL_PROPERTY_CODE))
                                        {
                                            assertEquals("cramakri@inf.ethz.ch",
                                                    property.getValue());
                                        }
                                    }
                                    return true;
                                }
                                return false;
                            }
                        }));

                    // Return the sample
                    one(openbisService).tryGetSampleWithExperiment(
                            with(new SampleIdentifierFactory(COLLECTION_SAMPLE_IDENTIFIER)
                                    .createIdentifier()));
                    will(returnValue(sample));
                }
            });
    }

    protected void setupHandleRawDataSetExpectations(final String path)
    {
        // Create the Raw Images Data Set
        final DataSetInformation dataSetInformation = new DataSetInformation();
        dataSetInformation.setDataSetCode(RAW_IMAGES_DATA_SET_CODE);
        dataSetInformation.setSampleCode(COLLECTION_SAMPLE_CODE);
        dataSetInformation.setSpaceCode(SPACE_CODE);
        dataSetInformation.setShareId("42");

        // set up the expectations
        context.checking(new Expectations()
            {
                {
                    one(delegator).handleDataSet(with(new File(path)),
                            with(new MatcherNoDesc<DataSetInformation>()
                                {
                                    @Override
                                    public boolean matches(Object item)
                                    {
                                        if (item instanceof DataSetInformation)
                                        {
                                            DataSetInformation dataSetInfo =
                                                    (DataSetInformation) item;
                                            assertEquals(
                                                    rawImagesDataSetTypeWithTerms.getDataSetType(),
                                                    dataSetInfo.getDataSetType());
                                            assertEquals(COLLECTION_SAMPLE_CODE,
                                                    dataSetInfo.getSampleCode());
                                            return true;
                                        }
                                        return false;
                                    }
                                }));
                    will(returnValue(Collections.singletonList(dataSetInformation)));
                }
            });
    }

    protected void setupHandleCollectionMetadataDataSetExpectations(final String path)
    {
        // Create the Raw Images Data Set
        final DataSetInformation dataSetInformation = new DataSetInformation();
        dataSetInformation.setDataSetCode(METADATA_DATA_SET_CODE);
        dataSetInformation.setSampleCode(COLLECTION_SAMPLE_CODE);
        dataSetInformation.setSpaceCode(SPACE_CODE);
        dataSetInformation.setShareId("42");

        dataSet = new PhysicalDataSet();
        dataSet.setCode(METADATA_DATA_SET_CODE);

        final File dataSetFile = new File(path);
        final File imageDataSetFile1 = new File(dataSetFile, "Test.dm3/Representations");
        final File imageDataSetFile2 = new File(dataSetFile, "Test2.dm3/Representations");

        // set up the expectations
        context.checking(new Expectations()
            {
                {
                    // Register the metadata data set
                    one(delegator).handleDataSet(with(dataSetFile),
                            with(new MatcherNoDesc<DataSetInformation>()
                                {
                                    @Override
                                    public boolean matches(Object item)
                                    {
                                        if (item instanceof DataSetInformation)
                                        {
                                            DataSetInformation dataSetInfo =
                                                    (DataSetInformation) item;
                                            assertEquals(
                                                    metadataDataSetTypeWithTerms.getDataSetType(),
                                                    dataSetInfo.getDataSetType());
                                            assertEquals(COLLECTION_SAMPLE_CODE,
                                                    dataSetInfo.getSampleCode());
                                            assertEquals(BUNDLE_METADATA_DATA_SET_CODE, dataSetInfo
                                                    .getParentDataSetCodes().get(0));
                                            return true;
                                        }
                                        return false;
                                    }
                                }));
                    will(returnValue(Collections.singletonList(dataSetInformation)));

                    // Retrieve the registered data set from openBIS
                    one(openbisService).tryGetDataSet(SESSION_TOKEN, METADATA_DATA_SET_CODE);
                    will(returnValue(dataSet));
                    // Retrieve the registered data set from the store
                    one(delegator).getFileForDataSet(dataSet, "42");
                    will(returnValue(dataSetFile.getParentFile()));

                    // Register the images data set
                    one(delegator).linkAndHandleDataSet(with(imageDataSetFile1),
                            with(new MatcherNoDesc<DataSetInformation>()
                                {
                                    @Override
                                    public boolean matches(Object item)
                                    {
                                        if (item instanceof DataSetInformation)
                                        {
                                            DataSetInformation dataSetInfo =
                                                    (DataSetInformation) item;
                                            assertEquals(
                                                    imageDataSetTypeWithTerms.getDataSetType(),
                                                    dataSetInfo.getDataSetType());
                                            assertEquals(COLLECTION_SAMPLE_CODE,
                                                    dataSetInfo.getSampleCode());
                                            assertEquals(BUNDLE_METADATA_DATA_SET_CODE, dataSetInfo
                                                    .getParentDataSetCodes().get(0));
                                            return true;
                                        }
                                        return false;
                                    }
                                }));
                    will(returnValue(Collections.singletonList(dataSetInformation)));

                    one(delegator).linkAndHandleDataSet(with(imageDataSetFile2),
                            with(new MatcherNoDesc<DataSetInformation>()
                                {
                                    @Override
                                    public boolean matches(Object item)
                                    {
                                        if (item instanceof DataSetInformation)
                                        {
                                            DataSetInformation dataSetInfo =
                                                    (DataSetInformation) item;
                                            assertEquals(
                                                    imageDataSetTypeWithTerms.getDataSetType(),
                                                    dataSetInfo.getDataSetType());
                                            assertEquals(COLLECTION_SAMPLE_CODE,
                                                    dataSetInfo.getSampleCode());
                                            assertEquals(BUNDLE_METADATA_DATA_SET_CODE, dataSetInfo
                                                    .getParentDataSetCodes().get(0));
                                            return true;
                                        }
                                        return false;
                                    }
                                }));
                    will(returnValue(Collections.singletonList(dataSetInformation)));
                }
            });
    }

    protected void setupHandleBundleMetadataDataSetExpectations(final String path)
    {
        // Create the Raw Images Data Set
        final DataSetInformation dataSetInformation = new DataSetInformation();
        dataSetInformation.setDataSetCode(BUNDLE_METADATA_DATA_SET_CODE);
        dataSetInformation.setSampleCode(GRID_SAMPLE_CODE);
        dataSetInformation.setSpaceCode(SPACE_CODE);

        final File dataSetFile = new File(path);

        // set up the expectations
        context.checking(new Expectations()
            {
                {
                    // Register the metadata data set
                    one(delegator).handleDataSet(with(dataSetFile),
                            with(new MatcherNoDesc<DataSetInformation>()
                                {
                                    @Override
                                    public boolean matches(Object item)
                                    {
                                        if (item instanceof DataSetInformation)
                                        {
                                            DataSetInformation dataSetInfo =
                                                    (DataSetInformation) item;
                                            assertEquals(
                                                    metadataDataSetTypeWithTerms.getDataSetType(),
                                                    dataSetInfo.getDataSetType());
                                            assertEquals(GRID_SAMPLE_CODE,
                                                    dataSetInfo.getSampleCode());
                                            return true;
                                        }
                                        return false;
                                    }
                                }));
                    will(returnValue(Collections.singletonList(dataSetInformation)));
                }
            });
    }

    protected void setupCallerDataSetInfoExpectations()
    {

        final DataSetInformation callerDataSetInfo = new DataSetInformation();
        callerDataSetInfo.setSpaceCode(SPACE_CODE);
        callerDataSetInfo.setExperimentIdentifier(new ExperimentIdentifierFactory(
                EXPERIMENT_IDENTIFIER).createIdentifier());

        // set up the expectations
        context.checking(new Expectations()
            {
                {
                    one(delegator).getCallerDataSetInformation();
                    will(returnValue(callerDataSetInfo));
                }
            });
    }

    protected void setupSessionContextExpectations()
    {
        final SessionContextDTO sessionContext = new SessionContextDTO();
        sessionContext.setSessionToken(SESSION_TOKEN);
        sessionContext.setUserEmail("test@test.bar");
        sessionContext.setUserName(TEST_USER_NAME);

        // set up the expectations
        context.checking(new Expectations()
            {
                {
                    one(delegator).getSessionContext();
                    will(returnValue(sessionContext));
                }
            });

    }

    protected void setupNewEntititesExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    // The Grid Prep does not yet exist
                    one(openbisService).tryGetSampleWithExperiment(
                            with(new SampleIdentifierFactory(GRID_SAMPLE_IDENTIFIER)
                                    .createIdentifier()));
                    will(returnValue(null));

                    // Create the Grid Prep
                    one(openbisService).registerSample(with(new BaseMatcher<NewSample>()
                        {
                            @Override
                            @SuppressWarnings("deprecation")
                            public boolean matches(Object item)
                            {
                                if (item instanceof NewSample)
                                {
                                    NewSample newSample = (NewSample) item;
                                    assertEquals(GRID_SAMPLE_IDENTIFIER, newSample.getIdentifier());
                                    assertEquals(EXPERIMENT_IDENTIFIER.toString(),
                                            newSample.getExperimentIdentifier());
                                    assertEquals(null, newSample.getParentIdentifier());
                                    return true;
                                }
                                return false;
                            }

                            @Override
                            public void describeTo(Description description)
                            {
                            }
                        }), with(TEST_USER_NAME));
                    will(returnValue(Long.valueOf(1)));

                    // The Replica does not yet exist
                    one(openbisService).tryGetSampleWithExperiment(
                            with(new SampleIdentifierFactory(COLLECTION_SAMPLE_IDENTIFIER)
                                    .createIdentifier()));
                    will(returnValue(null));

                    // Create the Replica
                    one(openbisService).registerSample(with(new MatcherNoDesc<NewSample>()
                        {
                            @Override
                            @SuppressWarnings("deprecation")
                            public boolean matches(Object item)
                            {
                                if (item instanceof NewSample)
                                {
                                    NewSample newSample = (NewSample) item;
                                    assertEquals(COLLECTION_SAMPLE_IDENTIFIER,
                                            newSample.getIdentifier());
                                    assertEquals(EXPERIMENT_IDENTIFIER.toString(),
                                            newSample.getExperimentIdentifier());
                                    assertEquals(GRID_SAMPLE_IDENTIFIER,
                                            newSample.getParentIdentifier());
                                    return true;
                                }
                                return false;
                            }
                        }), with(TEST_USER_NAME));
                    will(returnValue(Long.valueOf(2)));

                    Sample sample = new Sample();
                    Experiment exp = new Experiment();
                    exp.setIdentifier(EXPERIMENT_IDENTIFIER);
                    sample.setId((long) 1);
                    sample.setExperiment(exp);
                    sample.setIdentifier(COLLECTION_SAMPLE_IDENTIFIER);
                    one(openbisService).tryGetSampleWithExperiment(
                            with(new SampleIdentifierFactory(COLLECTION_SAMPLE_IDENTIFIER)
                                    .createIdentifier()));
                    will(returnValue(sample));
                }
            });
    }

}