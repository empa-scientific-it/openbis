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
package ch.systemsx.cisd.openbis.plugin.generic.client.web.server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.core.IsAnything;
import org.jmock.Expectations;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.springframework.web.multipart.MultipartFile;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.generic.client.web.server.AbstractClientServiceTest;
import ch.systemsx.cisd.openbis.generic.client.web.server.UploadedFilesBean;
import ch.systemsx.cisd.openbis.generic.shared.CommonTestUtils;
import ch.systemsx.cisd.openbis.generic.shared.ISessionWorkspaceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchRegistrationResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterialsWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.UpdatedBasicExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.UpdatedExperimentsWithType;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.translator.MaterialTypeTranslator;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServerInternal;

/**
 * Test cases for corresponding {@link GenericClientService} class.
 * 
 * @author Christian Ribeaud
 */
@Friend(toClasses = GenericClientService.class)
public final class GenericClientServiceTest extends AbstractClientServiceTest
{
    private static final MaterialType MATERIAL_TYPE = MaterialTypeTranslator
            .translateSimple(CommonTestUtils.createMaterialType());

    private MultipartFile multipartFile;

    private IGenericServerInternal genericServer;

    private GenericClientService genericClientService;

    private ISessionWorkspaceProvider sessionWorkspaceProvider;

    private final static NewSample createNewSample(final String sampleIdentifier,
            final String sampleTypeCode, final IEntityProperty[] properties)
    {
        final NewSample newSample = new NewSample();
        newSample.setIdentifier(sampleIdentifier);
        final SampleType sampleType = createSampleType(sampleTypeCode);
        newSample.setSampleType(sampleType);
        newSample.setProperties(properties);
        return newSample;
    }

    private final static SampleType createSampleType(final String sampleTypeCode)
    {
        final SampleType sampleType = new SampleType();
        sampleType.setCode(sampleTypeCode);
        return sampleType;
    }

    private final static ExperimentType createExperimentType(final String experimentTypeCode)
    {
        final ExperimentType experimentType = new ExperimentType();
        experimentType.setCode(experimentTypeCode);
        return experimentType;
    }

    private final static IEntityProperty createSampleProperty(final String propertyTypeCode,
            final String value)
    {
        final IEntityProperty sampleProperty = new EntityProperty();
        sampleProperty.setValue(value);
        final PropertyType propertyType = new PropertyType();
        propertyType.setCode(propertyTypeCode);
        sampleProperty.setPropertyType(propertyType);
        return sampleProperty;
    }

    //
    // AbstractClientServiceTest
    //

    @Override
    @BeforeMethod
    public final void setUp()
    {
        super.setUp();
        genericServer = context.mock(IGenericServerInternal.class);
        multipartFile = context.mock(MultipartFile.class);
        sessionWorkspaceProvider = context.mock(ISessionWorkspaceProvider.class);
        genericClientService = new GenericClientService(genericServer, requestContextProvider);
        context.checking(new Expectations()
            {
                {
                    allowing(genericServer).isProjectSamplesEnabled(SESSION_TOKEN);
                    will(returnValue(true));
                }
            });
    }

    @Test
    public void testGetSampleGenerationInfo()
    {
        final TechId sampleId = new TechId(4711L);
        context.checking(new Expectations()
            {
                {
                    prepareGetSessionToken(this);

                    one(genericServer).getSampleInfo(SESSION_TOKEN, sampleId);
                    SampleParentWithDerived parentWithDerived = new SampleParentWithDerived();
                    Sample sample = new Sample();
                    sample.setProperties(Arrays.asList(createXmlProperty()));
                    parentWithDerived.setParent(sample);
                    will(returnValue(parentWithDerived));
                }
            });

        SampleParentWithDerived info = genericClientService.getSampleGenerationInfo(sampleId);

        IEntityProperty transformedXMLProperty = info.getParent().getProperties().get(0);
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><b>hello</b>",
                transformedXMLProperty.tryGetAsString());
        assertEquals("<root>hello</root>", transformedXMLProperty.tryGetOriginalValue());
        context.assertIsSatisfied();
    }

    @Test
    public void testGetSampleInfo()
    {
        final TechId sampleId = new TechId(4711L);
        context.checking(new Expectations()
            {
                {
                    prepareGetSessionToken(this);

                    one(genericServer).getSampleInfo(SESSION_TOKEN, sampleId);
                    SampleParentWithDerived parentWithDerived = new SampleParentWithDerived();
                    Sample sample = new Sample();
                    sample.setProperties(Arrays.asList(createXmlProperty()));
                    parentWithDerived.setParent(sample);
                    will(returnValue(parentWithDerived));
                }
            });

        Sample info = genericClientService.getSampleInfo(sampleId);

        IEntityProperty transformedXMLProperty = info.getProperties().get(0);
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><b>hello</b>",
                transformedXMLProperty.tryGetAsString());
        assertEquals("<root>hello</root>", transformedXMLProperty.tryGetOriginalValue());
        context.assertIsSatisfied();
    }

    @Test
    public void testGetDataSetInfo()
    {
        final TechId id = new TechId(4711L);
        context.checking(new Expectations()
            {
                {
                    prepareGetSessionToken(this);

                    one(genericServer).getDataSetInfo(SESSION_TOKEN, id);
                    PhysicalDataSet dataSet = new PhysicalDataSet();
                    dataSet.setDataSetProperties(Arrays.asList(createXmlProperty()));
                    will(returnValue(dataSet));
                }
            });

        AbstractExternalData info = genericClientService.getDataSetInfo(id);

        IEntityProperty transformedXMLProperty = info.getProperties().get(0);
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><b>hello</b>",
                transformedXMLProperty.tryGetAsString());
        assertEquals("<root>hello</root>", transformedXMLProperty.tryGetOriginalValue());
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterSample()
    {
        final String sessionKey = "some-session-key";
        final NewSample newSample =
                createNewSample("/SPACE1/SAMPLE1", "MASTER_PLATE", IEntityProperty.EMPTY_ARRAY);
        context.checking(new Expectations()
            {
                {
                    prepareGetSessionToken(this);
                    allowing(httpSession).getAttribute(sessionKey);
                    will(returnValue(new UploadedFilesBean()));
                    one(httpSession).removeAttribute(sessionKey);
                    one(genericServer).registerSample(with(SESSION_TOKEN), getTranslatedSample(),
                            anyAttachmentCollection());
                    will(new CustomAction("check sample")
                        {
                            @Override
                            @SuppressWarnings("unchecked")
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                final NewSample sample = (NewSample) invocation.getParameter(1);
                                assertEquals("MASTER_PLATE", sample.getSampleType().getCode());
                                assertEquals("/SPACE1/SAMPLE1", sample.getIdentifier());
                                final Collection<NewAttachment> attachments =
                                        (Collection<NewAttachment>) invocation.getParameter(2);
                                assertEquals(0, attachments.size());
                                return null;
                            }
                        });
                }

                private final NewSample getTranslatedSample()
                {
                    return with(any(NewSample.class));
                }

                @SuppressWarnings("unchecked")
                private final Collection<NewAttachment> anyAttachmentCollection()
                {
                    return with(any(Collection.class));
                }
            });
        genericClientService.registerSample(sessionKey, newSample);
        context.assertIsSatisfied();
    }

    @SuppressWarnings("deprecation")
    @Test
    public final void testRegisterSamples() throws IOException
    {
        final UploadedFilesBean uploadedFilesBean = new UploadedFilesBean();
        final String sessionKey = "uploaded-files";
        final NewSample newSample = new NewSample();
        newSample.setIdentifier("MP1");
        newSample.setContainerIdentifier("MP2");
        newSample.setParentIdentifier("MP3");
        newSample.setProperties(new IEntityProperty[] { createSampleProperty("prop1", "RED"), createSampleProperty("prop2", "1") });
        final SampleType sampleType = createSampleType("MASTER_PLATE");
        final String fileName = "originalFileName.txt";

        final List<NewSamplesWithTypes> samplesWithType = new ArrayList<NewSamplesWithTypes>();
        List<NewSample> newSamples = new ArrayList<NewSample>();
        newSamples.add(newSample);
        samplesWithType.add(new NewSamplesWithTypes(sampleType, newSamples));
        context.checking(new Expectations()
            {
                {
                    prepareGetHttpSession(this);
                    prepareGetSessionToken(this);

                    allowing(sessionWorkspaceProvider).getSessionWorkspace(SESSION_TOKEN);
                    will(returnValue(getSessionWorkspaceDir()));

                    allowing(httpSession).getAttribute(sessionKey);
                    will(returnValue(uploadedFilesBean));

                    allowing(httpSession).removeAttribute(sessionKey);

                    allowing(multipartFile).getOriginalFilename();
                    will(returnValue(fileName));

                    one(multipartFile).transferTo(with(any(File.class)));
                    will(new CustomAction("copy content")
                        {
                            @Override
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                final File target = (File) invocation.getParameter(0);
                                FileUtilities
                                        .writeToFile(target,
                                                "[DEFAULT]\nprop1\tRED\n[DEFAULT]\nidentifier\tcontainer\tparent\tprop2\nMP1\tMP2\tMP3\t1");
                                return null;
                            }
                        });

                    one(genericServer).registerOrUpdateSamples(with(equal(SESSION_TOKEN)),
                            with(newSampleWithTypesList()));
                    will(new CustomAction("check sample")
                        {

                            @Override
                            @SuppressWarnings({ "unchecked" })
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                final List<NewSamplesWithTypes> samplesSecions =
                                        (List<NewSamplesWithTypes>) invocation.getParameter(1);
                                assertEquals(1, samplesSecions.size());
                                final NewSamplesWithTypes samples = samplesSecions.get(0);
                                // Do not compare sampleType, as the registration code doesn't set
                                // the database instance.
                                assertEquals(sampleType.getCode(), samples.getEntityType()
                                        .getCode());
                                assertEquals(1, samples.getNewEntities().size());
                                final NewSample sample = samples.getNewEntities().get(0);
                                assertEquals("MP1", sample.getIdentifier());
                                assertEquals("MP2", sample.getContainerIdentifier());
                                assertEquals("MP3", sample.getParentIdentifier());
                                assertEquals(2, sample.getProperties().length);
                                final IEntityProperty prop1 = sample.getProperties()[0];
                                final IEntityProperty prop2 = sample.getProperties()[1];
                                assertEquals("RED", prop1.getValue());
                                assertEquals("1", prop2.getValue());
                                return null;
                            }
                        });
                }
            });
        uploadedFilesBean.addMultipartFile(SESSION_TOKEN, multipartFile, sessionWorkspaceProvider);
        final List<BatchRegistrationResult> result =
                genericClientService.registerSamples(sampleType, sessionKey, false, null, null, false);
        assertEquals(1, result.size());
        final BatchRegistrationResult batchRegistrationResult = result.get(0);
        assertEquals(fileName, batchRegistrationResult.getFileName());
        assertEquals("Registration of 1 sample(s) is complete.",
                batchRegistrationResult.getMessage());
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterSamplesWithParents() throws IOException
    {
        final UploadedFilesBean uploadedFilesBean = new UploadedFilesBean();
        final String sessionKey = "uploaded-files";
        final NewSample newSample = new NewSample();
        newSample.setIdentifier("MP");
        newSample.setParentsOrNull(new String[] { "MP_1", "MP_2" });
        newSample.setProperties(new IEntityProperty[0]);
        final SampleType sampleType = createSampleType("MASTER_PLATE");
        final String fileName = "originalFileName.txt";

        final List<NewSamplesWithTypes> samplesWithType = new ArrayList<NewSamplesWithTypes>();
        List<NewSample> newSamples = new ArrayList<NewSample>();
        newSamples.add(newSample);
        samplesWithType.add(new NewSamplesWithTypes(sampleType, newSamples));
        context.checking(new Expectations()
            {
                {
                    prepareGetHttpSession(this);
                    prepareGetSessionToken(this);

                    allowing(sessionWorkspaceProvider).getSessionWorkspace(SESSION_TOKEN);
                    will(returnValue(getSessionWorkspaceDir()));

                    allowing(httpSession).getAttribute(sessionKey);
                    will(returnValue(uploadedFilesBean));

                    allowing(httpSession).removeAttribute(sessionKey);

                    allowing(multipartFile).getOriginalFilename();
                    will(returnValue(fileName));

                    one(multipartFile).transferTo(with(any(File.class)));
                    will(new CustomAction("copy content")
                        {
                            @Override
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                final File target = (File) invocation.getParameter(0);
                                FileUtilities.writeToFile(target,
                                        "identifier\tparents\nMP\tMP_1,MP_2");
                                return null;
                            }
                        });

                    one(genericServer).registerOrUpdateSamples(with(equal(SESSION_TOKEN)),
                            with(newSampleWithTypesList()));
                    will(new CustomAction("check sample")
                        {

                            @Override
                            @SuppressWarnings({ "unchecked" })
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                final List<NewSamplesWithTypes> samplesSecions =
                                        (List<NewSamplesWithTypes>) invocation.getParameter(1);
                                assertEquals(1, samplesSecions.size());
                                final NewSamplesWithTypes samples = samplesSecions.get(0);
                                // Do not compare sampleType, as the registration code doesn't set
                                // the database instance.
                                assertEquals(sampleType.getCode(), samples.getEntityType()
                                        .getCode());
                                assertEquals(1, samples.getNewEntities().size());
                                final NewSample sample = samples.getNewEntities().get(0);
                                assertEquals("MP", sample.getIdentifier());
                                assertEquals(2, sample.getParentsOrNull().length);
                                assertEquals("MP_1", sample.getParentsOrNull()[0]);
                                assertEquals("MP_2", sample.getParentsOrNull()[1]);
                                assertEquals(0, sample.getProperties().length);
                                return null;
                            }
                        });
                }
            });
        uploadedFilesBean.addMultipartFile(SESSION_TOKEN, multipartFile, sessionWorkspaceProvider);
        final List<BatchRegistrationResult> result =
                genericClientService.registerSamples(sampleType, sessionKey, false, null, null, false);
        assertEquals(1, result.size());
        final BatchRegistrationResult batchRegistrationResult = result.get(0);
        assertEquals(fileName, batchRegistrationResult.getFileName());
        assertEquals("Registration of 1 sample(s) is complete.",
                batchRegistrationResult.getMessage());
        context.assertIsSatisfied();
    }

    @Test
    public final void testUpdateSamples() throws IOException
    {
        final UploadedFilesBean uploadedFilesBean = new UploadedFilesBean();
        final String sessionKey = "uploaded-files";

        final SampleType sampleType = createSampleType("MASTER_PLATE");
        final String defaultGroupIdentifier = "/G1";
        final String fileName = "fileName.txt";

        context.checking(new Expectations()
            {
                {
                    prepareGetHttpSession(this);
                    prepareGetSessionToken(this);

                    allowing(sessionWorkspaceProvider).getSessionWorkspace(SESSION_TOKEN);
                    will(returnValue(getSessionWorkspaceDir()));

                    allowing(httpSession).getAttribute(sessionKey);
                    will(returnValue(uploadedFilesBean));

                    allowing(httpSession).removeAttribute(sessionKey);

                    allowing(multipartFile).getOriginalFilename();
                    will(returnValue(fileName));

                    one(multipartFile).transferTo(with(any(File.class)));
                    will(new CustomAction("copy content")
                        {
                            @Override
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                final File target = (File) invocation.getParameter(0);
                                FileUtilities.writeToFile(target,
                                        "identifier\tcontainer\tparent\texperiment\tprop1\tprop2\n"
                                                + "MP1\t/G1/MP2\t\tEXP1\tRED\t\n"
                                                + "/G2/MP1\t\t/G2/MP2\tEXP2\t\t1");
                                return null;
                            }
                        });

                    one(genericServer).updateSamples(with(equal(SESSION_TOKEN)),
                            with(newSampleWithTypesList()));
                    will(new CustomAction("check sample")
                        {

                            @Override
                            @SuppressWarnings({ "unchecked", "deprecation" })
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                final List<NewSamplesWithTypes> samplesSecions =
                                        (List<NewSamplesWithTypes>) invocation.getParameter(1);
                                assertEquals(1, samplesSecions.size());
                                final NewSamplesWithTypes samples = samplesSecions.get(0);
                                // Do not compare sampleType, as the update code doesn't check it
                                assertEquals(2, samples.getNewEntities().size());

                                final NewSample sample1 = samples.getNewEntities().get(0);
                                assertEquals("/G1/MP1", sample1.getIdentifier());
                                assertEquals("/G1/MP2", sample1.getContainerIdentifier());
                                assertEquals(null, sample1.getParentIdentifier());
                                assertEquals("EXP1", sample1.getExperimentIdentifier());
                                assertEquals(1, sample1.getProperties().length);
                                final IEntityProperty prop1 = sample1.getProperties()[0];
                                assertEquals("RED", prop1.getValue());

                                final NewSample sample2 = samples.getNewEntities().get(1);
                                assertEquals("/G2/MP1", sample2.getIdentifier());
                                assertEquals(null, sample2.getContainerIdentifier());
                                assertEquals("/G2/MP2", sample2.getParentIdentifier());
                                assertEquals("EXP2", sample2.getExperimentIdentifier());
                                assertEquals(1, sample2.getProperties().length);
                                final IEntityProperty prop2 = sample2.getProperties()[0];
                                assertEquals("1", prop2.getValue());

                                return null;
                            }
                        });
                }
            });
        uploadedFilesBean.addMultipartFile(SESSION_TOKEN, multipartFile, sessionWorkspaceProvider);
        final List<BatchRegistrationResult> result =
                genericClientService.updateSamples(sampleType, sessionKey, false, null, defaultGroupIdentifier);
        assertEquals(1, result.size());
        final BatchRegistrationResult batchRegistrationResult = result.get(0);
        assertEquals(fileName, batchRegistrationResult.getFileName());
        assertEquals("Update of 2 sample(s) is complete.", batchRegistrationResult.getMessage());
        context.assertIsSatisfied();
    }

    @Test
    public final void testUpdateSamplesWithParents() throws IOException
    {
        final UploadedFilesBean uploadedFilesBean = new UploadedFilesBean();
        final String sessionKey = "uploaded-files";

        final SampleType sampleType = createSampleType("MASTER_PLATE");
        final String defaultGroupIdentifier = "/G1";
        final String fileName = "fileName.txt";

        context.checking(new Expectations()
            {
                {
                    prepareGetHttpSession(this);
                    prepareGetSessionToken(this);

                    allowing(sessionWorkspaceProvider).getSessionWorkspace(SESSION_TOKEN);
                    will(returnValue(getSessionWorkspaceDir()));

                    allowing(httpSession).getAttribute(sessionKey);
                    will(returnValue(uploadedFilesBean));

                    allowing(httpSession).removeAttribute(sessionKey);

                    allowing(multipartFile).getOriginalFilename();
                    will(returnValue(fileName));

                    one(multipartFile).transferTo(with(any(File.class)));
                    will(new CustomAction("copy content")
                        {
                            @Override
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                final File target = (File) invocation.getParameter(0);
                                FileUtilities.writeToFile(target, "identifier\tparents\n"
                                        + "MP_1\t/G1/MP_11, /G1/MP_12\n" + "/G2/MP_2\t/G2/MP_21");
                                return null;
                            }
                        });

                    one(genericServer).updateSamples(with(equal(SESSION_TOKEN)),
                            with(newSampleWithTypesList()));
                    will(new CustomAction("check sample")
                        {

                            @Override
                            @SuppressWarnings("unchecked")
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                final List<NewSamplesWithTypes> samplesSecions =
                                        (List<NewSamplesWithTypes>) invocation.getParameter(1);
                                assertEquals(1, samplesSecions.size());
                                final NewSamplesWithTypes samples = samplesSecions.get(0);
                                // Do not compare sampleType, as the update code doesn't check it
                                assertEquals(2, samples.getNewEntities().size());

                                final NewSample sample1 = samples.getNewEntities().get(0);
                                assertEquals("/G1/MP_1", sample1.getIdentifier());
                                assertEquals(2, sample1.getParentsOrNull().length);
                                assertEquals("/G1/MP_11", sample1.getParentsOrNull()[0]);
                                assertEquals("/G1/MP_12", sample1.getParentsOrNull()[1]);

                                final NewSample sample2 = samples.getNewEntities().get(1);
                                assertEquals("/G2/MP_2", sample2.getIdentifier());
                                assertEquals(1, sample2.getParentsOrNull().length);
                                assertEquals("/G2/MP_21", sample2.getParentsOrNull()[0]);

                                return null;
                            }
                        });
                }
            });
        uploadedFilesBean.addMultipartFile(SESSION_TOKEN, multipartFile, sessionWorkspaceProvider);
        final List<BatchRegistrationResult> result =
                genericClientService.updateSamples(sampleType, sessionKey, false, null, defaultGroupIdentifier);
        assertEquals(1, result.size());
        final BatchRegistrationResult batchRegistrationResult = result.get(0);
        assertEquals(fileName, batchRegistrationResult.getFileName());
        assertEquals("Update of 2 sample(s) is complete.", batchRegistrationResult.getMessage());
        context.assertIsSatisfied();
    }

    @Test
    public void testUpdateMaterials() throws IOException
    {
        final String sessionKey = "s-key";
        final boolean ignoreUnregisteredMaterials = false;
        final int updateCount = 1;
        prepareMaterialsUpdate(sessionKey, ignoreUnregisteredMaterials, updateCount);

        List<BatchRegistrationResult> results =
                genericClientService.updateMaterials(MATERIAL_TYPE, sessionKey,
                        ignoreUnregisteredMaterials, false, null);

        assertEquals(1, results.size());
        assertEquals(updateCount + " material(s) updated.", results.get(0).getMessage());
        context.assertIsSatisfied();
    }

    @Test
    public void testUpdateMaterialsIgnoringUnregisteredButNoUnregistered() throws IOException
    {
        final String sessionKey = "s-key";
        final boolean ignoreUnregisteredMaterials = true;
        final int updateCount = 1;
        prepareMaterialsUpdate(sessionKey, ignoreUnregisteredMaterials, updateCount);

        List<BatchRegistrationResult> results =
                genericClientService.updateMaterials(MATERIAL_TYPE, sessionKey,
                        ignoreUnregisteredMaterials, false, null);

        assertEquals(1, results.size());
        assertEquals(updateCount + " material(s) updated, non ignored.", results.get(0)
                .getMessage());
        context.assertIsSatisfied();
    }

    @Test
    public void testUpdateMaterialsIgnoringUnregistered() throws IOException
    {
        final String sessionKey = "s-key";
        final boolean ignoreUnregisteredMaterials = true;
        final int updateCount = 0;
        prepareMaterialsUpdate(sessionKey, ignoreUnregisteredMaterials, updateCount);

        List<BatchRegistrationResult> results =
                genericClientService.updateMaterials(MATERIAL_TYPE, sessionKey,
                        ignoreUnregisteredMaterials, false, null);

        assertEquals(1, results.size());
        assertEquals(updateCount + " material(s) updated, 1 ignored.", results.get(0).getMessage());
        context.assertIsSatisfied();
    }

    @SuppressWarnings("unchecked")
    protected void prepareMaterialsUpdate(final String sessionKey,
            final boolean ignoreUnregisteredMaterials, final int updateCount) throws IOException
    {
        final UploadedFilesBean uploadedFilesBean = new UploadedFilesBean();
        final MaterialTypePE materialTypePE = CommonTestUtils.createMaterialType();
        final MaterialType materialType = MaterialTypeTranslator.translateSimple(materialTypePE);
        final List<NewMaterialsWithTypes> newMaterials =
                Collections.singletonList(new NewMaterialsWithTypes(materialType, Arrays
                        .asList(new NewMaterial("M1"))));
        context.checking(new Expectations()
            {
                {
                    prepareGetSessionToken(this);

                    allowing(sessionWorkspaceProvider).getSessionWorkspace(SESSION_TOKEN);
                    will(returnValue(getSessionWorkspaceDir()));

                    allowing(httpSession).getAttribute(sessionKey);
                    will(returnValue(uploadedFilesBean));

                    allowing(multipartFile).getOriginalFilename();
                    will(returnValue("file name"));

                    one(multipartFile).transferTo(with(any(File.class)));
                    will(new CustomAction("copy content")
                        {
                            @Override
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                final File target = (File) invocation.getParameter(0);
                                FileUtilities.writeToFile(target, "code\nM1");
                                return null;
                            }
                        });

                    one(httpSession).removeAttribute(sessionKey);

                    one(genericServer).updateMaterials(with(equal(SESSION_TOKEN)),
                            with(any(newMaterials.getClass())),
                            with(equal(ignoreUnregisteredMaterials)));
                    will(returnValue(updateCount));
                }
            });
        uploadedFilesBean.addMultipartFile(SESSION_TOKEN, multipartFile, sessionWorkspaceProvider);
    }

    @Test
    public final void testRegisterExperiment()
    {
        final NewExperiment newExperiment =
                createNewExperiment("/S1/P1/E1", "SIRNA_HCS", IEntityProperty.EMPTY_ARRAY);
        final String attachmentSessionKey = "attachment-session-key";
        final String sampleSessionKey = "sample-session-key";
        context.checking(new Expectations()
            {
                {
                    prepareGetSessionToken(this);
                    allowing(httpSession).getAttribute(attachmentSessionKey);
                    // TODO 2009-01-20, IA: Add test for attachment handling
                    will(returnValue(new UploadedFilesBean()));
                    one(httpSession).removeAttribute(attachmentSessionKey);
                    one(genericServer).registerExperiment(with(SESSION_TOKEN),
                            getTranslatedExperiment(), anyAttachmentCollection());
                    will(new CustomAction("check experiment")
                        {
                            @Override
                            @SuppressWarnings("unchecked")
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                final NewExperiment experiment =
                                        (NewExperiment) invocation.getParameter(1);
                                assertEquals("SIRNA_HCS", experiment.getExperimentTypeCode());
                                assertEquals("/S1/P1/E1", experiment.getIdentifier());
                                final Collection<NewAttachment> attachments =
                                        (Collection<NewAttachment>) invocation.getParameter(2);
                                assertEquals(0, attachments.size());
                                return null;
                            }
                        });
                }

                private final NewExperiment getTranslatedExperiment()
                {
                    return with(any(NewExperiment.class));
                }

                @SuppressWarnings("unchecked")
                private final Collection<NewAttachment> anyAttachmentCollection()
                {
                    return with(any(Collection.class));
                }

            });
        genericClientService.registerExperiment(attachmentSessionKey, sampleSessionKey,
                newExperiment);
        context.assertIsSatisfied();
    }

    @Test
    public final void testUpdateExperiments() throws IOException
    {
        final UploadedFilesBean uploadedFilesBean = new UploadedFilesBean();
        final String sessionKey = "uploaded-files";

        final ExperimentType experimentType = createExperimentType("EXP_TYPE");
        final String fileName = "fileName.txt";

        context.checking(new Expectations()
            {
                {
                    prepareGetHttpSession(this);
                    prepareGetSessionToken(this);

                    allowing(sessionWorkspaceProvider).getSessionWorkspace(SESSION_TOKEN);
                    will(returnValue(getSessionWorkspaceDir()));

                    allowing(httpSession).getAttribute(sessionKey);
                    will(returnValue(uploadedFilesBean));

                    allowing(httpSession).removeAttribute(sessionKey);

                    allowing(multipartFile).getOriginalFilename();
                    will(returnValue(fileName));

                    one(multipartFile).transferTo(with(any(File.class)));
                    will(new CustomAction("copy content")
                        {
                            @Override
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                final File target = (File) invocation.getParameter(0);
                                FileUtilities.writeToFile(target, "identifier\tprop1\tprop2\n"
                                        + "/SPACE1/PROJECT1/EXP1\tRED\t\n"
                                        + "/SPACE1/PROJECT2/EXP1\t\t1");
                                return null;
                            }
                        });

                    exactly(2).of(genericServer).getAuthSession(SESSION_TOKEN);

                    one(genericServer).updateExperiments(with(equal(SESSION_TOKEN)),
                            with(new IsAnything<UpdatedExperimentsWithType>()));
                    will(new CustomAction("check experiment")
                        {

                            @Override
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                final UpdatedExperimentsWithType experiments =
                                        (UpdatedExperimentsWithType) invocation.getParameter(1);
                                // Do not compare sampleType, as the update code doesn't check it
                                assertEquals(2, experiments.getUpdatedExperiments().size());

                                final UpdatedBasicExperiment experiment1 =
                                        experiments.getUpdatedExperiments().get(0);
                                assertEquals("/SPACE1/PROJECT1/EXP1", experiment1.getIdentifier());
                                assertEquals(1, experiment1.getProperties().length);
                                final IEntityProperty prop1 = experiment1.getProperties()[0];
                                assertEquals("RED", prop1.getValue());

                                final UpdatedBasicExperiment experiment2 =
                                        experiments.getUpdatedExperiments().get(1);
                                assertEquals("/SPACE1/PROJECT2/EXP1", experiment2.getIdentifier());
                                assertEquals(1, experiment2.getProperties().length);
                                final IEntityProperty prop2 = experiment2.getProperties()[0];
                                assertEquals("1", prop2.getValue());

                                return null;
                            }
                        });
                }
            });
        uploadedFilesBean.addMultipartFile(SESSION_TOKEN, multipartFile, sessionWorkspaceProvider);
        final List<BatchRegistrationResult> result =
                genericClientService.updateExperiments(experimentType, sessionKey, false, null);
        assertEquals(1, result.size());
        final BatchRegistrationResult batchRegistrationResult = result.get(0);
        assertEquals(fileName, batchRegistrationResult.getFileName());
        assertEquals("Update of 2 experiment(s) is complete.", batchRegistrationResult.getMessage());
        context.assertIsSatisfied();
    }

    @Test
    public final void testUpdateExperimentsWithProjects() throws IOException
    {
        final UploadedFilesBean uploadedFilesBean = new UploadedFilesBean();
        final String sessionKey = "uploaded-files";

        final ExperimentType experimentType = createExperimentType("EXP_TYPE");
        final String fileName = "fileName.txt";

        context.checking(new Expectations()
            {
                {
                    prepareGetHttpSession(this);
                    prepareGetSessionToken(this);

                    allowing(sessionWorkspaceProvider).getSessionWorkspace(SESSION_TOKEN);
                    will(returnValue(getSessionWorkspaceDir()));

                    allowing(httpSession).getAttribute(sessionKey);
                    will(returnValue(uploadedFilesBean));

                    allowing(httpSession).removeAttribute(sessionKey);

                    allowing(multipartFile).getOriginalFilename();
                    will(returnValue(fileName));

                    one(multipartFile).transferTo(with(any(File.class)));
                    will(new CustomAction("copy content")
                        {
                            @Override
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                final File target = (File) invocation.getParameter(0);
                                FileUtilities
                                        .writeToFile(
                                                target,
                                                "identifier\tproject\tprop1\tprop2\n"
                                                        + "/SPACE1/PROJECT1/EXP1\t/SPACE1/PROJECT3/\tRED\t\n"
                                                        + "/SPACE1/PROJECT2/EXP1\t\t\t1");
                                return null;
                            }
                        });

                    exactly(2).of(genericServer).getAuthSession(SESSION_TOKEN);

                    one(genericServer).updateExperiments(with(equal(SESSION_TOKEN)),
                            with(new IsAnything<UpdatedExperimentsWithType>()));
                    will(new CustomAction("check experiment")
                        {
                            @Override
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                final UpdatedExperimentsWithType experiments =
                                        (UpdatedExperimentsWithType) invocation.getParameter(1);
                                // Do not compare sampleType, as the update code doesn't check it
                                assertEquals(2, experiments.getUpdatedExperiments().size());

                                final UpdatedBasicExperiment experiment1 =
                                        experiments.getUpdatedExperiments().get(0);
                                assertEquals("/SPACE1/PROJECT1/EXP1", experiment1.getIdentifier());
                                assertEquals("/SPACE1/PROJECT3/",
                                        experiment1.getNewProjectIdentifierOrNull());
                                assertEquals(1, experiment1.getProperties().length);
                                final IEntityProperty prop1 = experiment1.getProperties()[0];
                                assertEquals("RED", prop1.getValue());

                                final UpdatedBasicExperiment experiment2 =
                                        experiments.getUpdatedExperiments().get(1);
                                assertEquals("/SPACE1/PROJECT2/EXP1", experiment2.getIdentifier());
                                assertEquals("", experiment2.getNewProjectIdentifierOrNull());
                                assertEquals(1, experiment2.getProperties().length);
                                final IEntityProperty prop2 = experiment2.getProperties()[0];
                                assertEquals("1", prop2.getValue());

                                return null;
                            }
                        });
                }
            });
        uploadedFilesBean.addMultipartFile(SESSION_TOKEN, multipartFile, sessionWorkspaceProvider);
        final List<BatchRegistrationResult> result =
                genericClientService.updateExperiments(experimentType, sessionKey, false, null);
        assertEquals(1, result.size());
        final BatchRegistrationResult batchRegistrationResult = result.get(0);
        assertEquals(fileName, batchRegistrationResult.getFileName());
        assertEquals("Update of 2 experiment(s) is complete.", batchRegistrationResult.getMessage());
        context.assertIsSatisfied();
    }

    //
    // Helper classes
    //

    private NewExperiment createNewExperiment(String identifier, String type,
            IEntityProperty[] properties)
    {
        final NewExperiment newExperiment = new NewExperiment();
        newExperiment.setIdentifier(identifier);
        newExperiment.setExperimentTypeCode(type);
        newExperiment.setProperties(properties);
        return newExperiment;
    }

    private IsAnything<List<NewSamplesWithTypes>> newSampleWithTypesList()
    {
        return new IsAnything<List<NewSamplesWithTypes>>();
    }

    private IEntityProperty createXmlProperty()
    {
        GenericEntityProperty property = new GenericEntityProperty();
        PropertyType propertyType = new PropertyType();
        propertyType.setDataType(new DataType(DataTypeCode.XML));
        propertyType
                .setTransformation(("<xsl:stylesheet version='1.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>"
                        + "<xsl:template match='/'><b><xsl:value-of select='.'/></b></xsl:template>"
                        + "</xsl:stylesheet>"));
        property.setPropertyType(propertyType);
        property.setValue("<root>hello</root>");
        return property;
    }

    /**
     * A {@link BaseMatcher} extension for checking the list of {@link NewSample NewSamples}.
     * 
     * @author Christian Ribeaud
     */
    @SuppressWarnings("unused")
    private final static class NewSampleListMatcher extends BaseMatcher<List<NewSample>>
    {
        private final List<NewSample> newSamples;

        NewSampleListMatcher(final List<NewSample> newSamples)
        {
            this.newSamples = newSamples;
        }

        private final boolean equals(final IEntityProperty[] properties,
                final IEntityProperty[] sampleProperties)
        {
            int i = -1;
            for (final IEntityProperty sampleProperty : sampleProperties)
            {
                if (StringUtils.equals(sampleProperty.getStringValue(), properties[++i].getStringValue()) == false
                        || StringUtils.equals(getPropertyTypeCode(sampleProperty),
                                getPropertyTypeCode(properties[i])) == false)
                {
                    return false;
                }
            }
            return true;
        }

        private final static String getPropertyTypeCode(final IEntityProperty sampleProperty)
        {
            return sampleProperty.getPropertyType().getCode();
        }

        private final boolean equals(final NewSample newSample, final NewSample thatNewSample)
        {
            // NOTE: this should take default container and default space into account to be
            // complete
            return ObjectUtils.equals(newSample, thatNewSample)
                    && StringUtils.equals(newSample.getContainerIdentifier(),
                            thatNewSample.getContainerIdentifier())
                    && StringUtils.equals(Arrays.toString(newSample.getParentsOrNull()),
                            Arrays.toString(thatNewSample.getParentsOrNull()))
                    && equals(newSample.getProperties(), thatNewSample.getProperties());
        }

        //
        // BaseMatcher
        //

        @Override
        public final void describeTo(final Description description)
        {
            description.appendValue(newSamples);
        }

        @Override
        @SuppressWarnings("unchecked")
        public final boolean matches(final Object item)
        {
            final List<NewSample> thisNewSamples = (List<NewSample>) item;
            if (thisNewSamples.size() == newSamples.size() == false)
            {
                return false;
            }
            int i = 0;
            for (final NewSample newSample : thisNewSamples)
            {
                final NewSample thatNewSample = newSamples.get(i++);
                if (equals(newSample, thatNewSample) == false)
                {
                    return false;
                }
            }
            return true;
        }
    }

    private File getSessionWorkspaceDir()
    {
        File dir = new File("targets/sessionWorkspace");
        if (false == dir.exists())
        {
            dir.mkdirs();
        }
        return dir;
    }
}
