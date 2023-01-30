/*
 * Copyright 2008 ETH Zuerich, CISD
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.plugin.IDataSetTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.CommonTestUtils;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentWithContent;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentBatchUpdateDetails;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewBasicExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperimentsWithType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterialsWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.UpdatedBasicExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.UpdatedExperimentsWithType;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialUpdateDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleRelationshipPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.translator.MaterialTypeTranslator;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;

/**
 * Test cases for corresponding {@link GenericServer} class.
 * 
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = GenericServer.class)
public final class GenericServerTest extends AbstractServerTestCase
{
    private static final String EXPERIMENT_IDENTIFIER1 = "/SPACE/PROJECT/EXP1";

    private static final String EXPERIMENT_IDENTIFIER2 = "/SPACE/PROJECT/EXP2";

    private static final String EXPERIMENT_TYPE = "EXP_TYPE";

    private static final String PROJECT_1 = "PROJECT-1";

    private static final String GROUP_1 = "GROUP-1";

    private static final String DATABASE_1 = "DATABASE-1";

    private IGenericBusinessObjectFactory genericBusinessObjectFactory;

    private ISampleTypeSlaveServerPlugin sampleTypeSlaveServerPlugin;

    private IDataSetTypeSlaveServerPlugin dataSetTypeSlaveServerPlugin;

    private ICommonServer commonServer;

    private final IGenericServer createServer()
    {
        GenericServer genericServer =
                new GenericServer(sessionManager, daoFactory, propertiesBatchManager,
                        genericBusinessObjectFactory, sampleTypeSlaveServerPlugin,
                        dataSetTypeSlaveServerPlugin);
        genericServer.commonServer = commonServer;
        return genericServer;
    }

    private static final ExperimentTypePE createExperimentType(String code)
    {
        ExperimentTypePE experimentTypePE = new ExperimentTypePE();
        experimentTypePE.setCode(code);
        return experimentTypePE;
    }

    private static final ArrayList<NewBasicExperiment> createNewExperiments(
            NewBasicExperiment... basicExperiments)
    {
        return new ArrayList<NewBasicExperiment>(Arrays.asList(basicExperiments));
    }

    private final NewSample createNewSample(final String identifier)
    {
        final NewSample newSample = new NewSample();
        newSample.setIdentifier(identifier);
        return newSample;
    }

    private final NewMaterial createNewMaterial(final String code)
    {
        final NewMaterial material = new NewMaterial();
        material.setCode(code);
        return material;
    }

    //
    // AbstractServerTestCase
    //

    @Override
    @BeforeMethod
    public final void setUp()
    {
        super.setUp();
        genericBusinessObjectFactory = context.mock(IGenericBusinessObjectFactory.class);
        sampleTypeSlaveServerPlugin = context.mock(ISampleTypeSlaveServerPlugin.class);
        dataSetTypeSlaveServerPlugin = context.mock(IDataSetTypeSlaveServerPlugin.class);
        commonServer = context.mock(ICommonServer.class);
    }

    public final void testRegisterSample()
    {
        prepareGetSession();
        final NewSample newSample = new NewSample();
        context.checking(new Expectations()
            {
                {
                    one(genericBusinessObjectFactory).createSampleBO(session);
                    will(returnValue(sampleBO));

                    one(sampleBO).define(newSample);
                    exactly(2).of(sampleBO).save();

                    final SamplePE sample = new SamplePE();
                    final Long id = 1L;
                    sample.setId(id);
                    allowing(sampleBO).getSample();
                    will((returnValue(sample)));
                }
            });
        createServer().registerSample(SESSION_TOKEN, newSample,
                Collections.<NewAttachment> emptyList());
        context.assertIsSatisfied();
    }

    @Test
    public void testGetExperimentFileAttachment() throws Exception
    {
        prepareGetSession();
        final TechId experimentId = CommonTestUtils.TECH_ID;
        final AttachmentPE attachmentPE = CommonTestUtils.createAttachment();
        context.checking(new Expectations()
            {
                {
                    one(genericBusinessObjectFactory).createExperimentBO(session);
                    will(returnValue(experimentBO));

                    one(experimentBO).loadDataByTechId(experimentId);

                    one(experimentBO).getExperimentFileAttachment(attachmentPE.getFileName(),
                            attachmentPE.getVersion());
                    will(returnValue(attachmentPE));

                }
            });
        final AttachmentWithContent attachment =
                createServer().getExperimentFileAttachment(SESSION_TOKEN, experimentId,
                        attachmentPE.getFileName(), attachmentPE.getVersion());
        assertEquals(attachmentPE.getFileName(), attachment.getFileName());
        assertEquals(attachmentPE.getAttachmentContent().getValue(), attachment.getContent());
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterSamplesWithoutExpectations()
    {
        prepareGetSession();
        final IGenericServer server = createServer();
        // Null values
        boolean fail = true;
        try
        {
            server.registerSamples(null, null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        // Empty collection
        server.registerSamples(SESSION_TOKEN, Collections.<NewSamplesWithTypes> emptyList());
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterSamplesWithDuplicatedNewSamples()
    {
        prepareGetSession();
        final IGenericServer server = createServer();
        final List<NewSample> newSamples = new ArrayList<NewSample>();
        newSamples.add(createNewSample("same"));
        newSamples.add(createNewSample("same"));
        List<NewSamplesWithTypes> samplesWithTypes = new ArrayList<NewSamplesWithTypes>();
        final NewSamplesWithTypes newSamplesWithType =
                new NewSamplesWithTypes(new SampleType(), newSamples);
        samplesWithTypes.add(newSamplesWithType);

        try
        {
            server.registerSamples(SESSION_TOKEN, samplesWithTypes);
            fail(String.format("'%s' expected.", UserFailureException.class));
        } catch (final UserFailureException ex)
        {
            // Nothing to do here.
        }
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterOrUpdateSamplesWithoutUpdate()
    {
        prepareGetSession();
        final SampleTypePE sampleTypePE = CommonTestUtils.createSampleType();
        final SampleType sampleType = new SampleType();
        sampleType.setCode(sampleTypePE.getCode());
        final List<NewSample> newSamples = new ArrayList<NewSample>();
        newSamples.add(createNewSample("one"));
        newSamples.add(createNewSample("two"));
        List<NewSamplesWithTypes> samplesWithTypes = new ArrayList<NewSamplesWithTypes>();
        final NewSamplesWithTypes newSamplesWithType =
                new NewSamplesWithTypes(sampleType, newSamples);
        samplesWithTypes.add(newSamplesWithType);

        context.checking(new Expectations()
            {
                {
                    exactly(2).of(sampleTypeDAO).tryFindSampleTypeByCode(
                            with(equal(sampleTypePE.getCode())));
                    will(returnValue(sampleTypePE));

                    one(sampleTypeSlaveServerPlugin).registerSamples(session, newSamples, null);

                    one(propertiesBatchManager).manageProperties(sampleTypePE, newSamples, null);
                }
            });
        createServer().registerOrUpdateSamples(SESSION_TOKEN, samplesWithTypes);
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterSamples()
    {
        prepareGetSession();
        final SampleTypePE sampleTypePE = CommonTestUtils.createSampleType();
        final SampleType sampleType = new SampleType();
        sampleType.setCode(sampleTypePE.getCode());
        final List<NewSample> newSamples = new ArrayList<NewSample>();
        newSamples.add(createNewSample("one"));
        newSamples.add(createNewSample("two"));
        List<NewSamplesWithTypes> samplesWithTypes = new ArrayList<NewSamplesWithTypes>();
        final NewSamplesWithTypes newSamplesWithType =
                new NewSamplesWithTypes(sampleType, newSamples);
        samplesWithTypes.add(newSamplesWithType);
        context.checking(new Expectations()
            {
                {
                    one(sampleTypeDAO).tryFindSampleTypeByCode(sampleTypePE.getCode());
                    will(returnValue(sampleTypePE));

                    one(sampleTypeSlaveServerPlugin).registerSamples(session, newSamples, null);

                    one(propertiesBatchManager).manageProperties(sampleTypePE, newSamples, null);
                }
            });
        createServer().registerSamples(SESSION_TOKEN, samplesWithTypes);
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterExperimentWithoutSamples()
    {
        prepareGetSession();
        final NewExperiment newExperiment = new NewExperiment();
        context.checking(new Expectations()
            {
                {
                    one(genericBusinessObjectFactory).createExperimentBO(session);
                    will(returnValue(experimentBO));

                    one(experimentBO).define(newExperiment);
                    one(experimentBO).save();

                    final ExperimentPE experiment = createExperiment(EXPERIMENT_TYPE, "E1", "S1");
                    final Long id = 1L;
                    experiment.setId(id);
                    allowing(experimentBO).getExperiment();
                    will((returnValue(experiment)));
                }
            });
        createServer().registerExperiment(SESSION_TOKEN, newExperiment,
                Collections.<NewAttachment> emptyList());
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterExperimentWithSamples()
    {
        prepareGetSession();
        final String experimentTypeCode = "EXP-TYPE1";
        final String experimentCode = "EXP1";
        final String spaceCode = "CISD";
        String sample1Code = "SAMPLE1";
        final String sample1 = createSampleIdentifier(spaceCode, sample1Code);
        final String sampleIdentifier2 = "SAMPLE2";
        final SampleIdentifier sampleIdentifier2WithGroup =
                SampleIdentifierFactory.parse(sampleIdentifier2);
        sampleIdentifier2WithGroup.getSpaceLevel().setSpaceCode(spaceCode);
        final SampleIdentifier sampleIdentifier1 = SampleIdentifierFactory.parse(sample1);
        final String[] samples =
        { sample1, sampleIdentifier2 };
        final ExperimentPE experimentPE =
                createExperiment(experimentTypeCode, experimentCode, spaceCode);
        final Long id = 1L;
        experimentPE.setId(id);
        final NewExperiment newExperiment =
                createNewExperiment(experimentTypeCode, experimentCode, spaceCode, samples);
        context.checking(new Expectations()
            {
                {
                    one(genericBusinessObjectFactory).createExperimentBO(session);
                    will(returnValue(experimentBO));

                    one(experimentBO).define(newExperiment);
                    one(experimentBO).save();

                    allowing(experimentBO).getExperiment();
                    will(returnValue(experimentPE));

                    one(genericBusinessObjectFactory).createSampleBO(session);
                    will(returnValue(sampleBO));
                    one(sampleBO).loadBySampleIdentifier(sampleIdentifier1);
                    one(sampleBO).setExperiment(experimentPE);

                    one(genericBusinessObjectFactory).createSampleBO(session);
                    will(returnValue(sampleBO));
                    one(sampleBO).loadBySampleIdentifier(sampleIdentifier2WithGroup);
                    one(sampleBO).setExperiment(experimentPE);
                }
            });
        createServer().registerExperiment(SESSION_TOKEN, newExperiment,
                Collections.<NewAttachment> emptyList());
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterExperimentWithSampleFromWrongSpace()
    {
        prepareGetSession();
        final String experimentTypeCode = "EXP-TYPE1";
        final String experimentCode = "EXP1";
        final String spaceCode = "CISD";
        String sample1Code = "SAMPLE1";
        final String sample1 = createSampleIdentifier("NOT_" + spaceCode, sample1Code);
        final String[] samples =
        { sample1 };
        final ExperimentPE experimentPE =
                createExperiment(experimentTypeCode, experimentCode, spaceCode);
        final NewExperiment newExperiment =
                createNewExperiment(experimentTypeCode, experimentCode, spaceCode, samples);
        context.checking(new Expectations()
            {
                {
                    one(genericBusinessObjectFactory).createExperimentBO(session);
                    will(returnValue(experimentBO));

                    one(experimentBO).define(newExperiment);
                    one(experimentBO).save();
                    one(experimentBO).getExperiment();
                    will(returnValue(experimentPE));

                }
            });
        try
        {
            createServer().registerExperiment(SESSION_TOKEN, newExperiment,
                    Collections.<NewAttachment> emptyList());
            fail("UserFailureException expected.");
        } catch (UserFailureException e)
        {
            assertTrue(e.getMessage().contains(
                    "Sample '/NOT_CISD/SAMPLE1' does not belong to the space 'CISD'"));
        }
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterExperimentTogetherWithSampleWithUndefinedSpace()
    {
        prepareGetSession();
        final String experimentTypeCode = "EXP-TYPE1";
        final String experimentCode = "EXP1";
        final String spaceCode = "CISD";
        final String sample1Code = "SAMPLE1";
        final String[] samples =
        { sample1Code };
        final ExperimentPE experimentPE =
                createExperiment(experimentTypeCode, experimentCode, spaceCode);
        final NewExperiment newExperiment =
                createNewExperiment(experimentTypeCode, experimentCode, null, samples);
        newExperiment.setRegisterSamples(true);
        final List<NewSample> newSamples = new ArrayList<NewSample>();
        NewSample newSample1 = createNewSample(sample1Code);
        newSamples.add(newSample1);
        List<NewSamplesWithTypes> samplesWithTypes = new ArrayList<NewSamplesWithTypes>();
        final SampleTypePE sampleTypePE = CommonTestUtils.createSampleType();
        final SampleType sampleType = new SampleType();
        sampleType.setCode(sampleTypePE.getCode());
        final NewSamplesWithTypes newSamplesWithType =
                new NewSamplesWithTypes(sampleType, newSamples);
        samplesWithTypes.add(newSamplesWithType);
        newExperiment.setNewSamples(samplesWithTypes);

        context.checking(new Expectations()
            {
                {
                    one(genericBusinessObjectFactory).createExperimentBO(session);
                    will(returnValue(experimentBO));

                    one(experimentBO).define(newExperiment);
                    one(experimentBO).save();
                    one(experimentBO).getExperiment();
                    will(returnValue(experimentPE));

                    one(sampleTypeDAO).tryFindSampleTypeByCode(sampleTypePE.getCode());
                    will(returnValue(sampleTypePE));

                    one(sampleTypeSlaveServerPlugin).registerSamples(session, newSamples, null);

                    one(propertiesBatchManager).manageProperties(sampleTypePE, newSamples, null);

                    one(genericBusinessObjectFactory).createSampleBO(session);
                    will(returnValue(sampleBO));
                    one(sampleBO).loadBySampleIdentifier(
                            SampleIdentifierFactory.parse(createSampleIdentifier(spaceCode,
                                    sample1Code)));
                    one(sampleBO).setExperiment(experimentPE);

                }
            });

        createServer().registerExperiment(SESSION_TOKEN, newExperiment,
                Collections.<NewAttachment> emptyList());

        assertEquals("/" + spaceCode, newSample1.getDefaultSpaceIdentifier());
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterMaterials()
    {
        prepareGetSession();
        final MaterialTypePE materialTypePE = CommonTestUtils.createMaterialType();
        final MaterialType materialType = MaterialTypeTranslator.translateSimple(materialTypePE);
        final List<NewMaterial> newMaterials = new ArrayList<NewMaterial>();
        newMaterials.add(createNewMaterial("one"));
        newMaterials.add(createNewMaterial("two"));
        NewMaterialsWithTypes nmwt = new NewMaterialsWithTypes(materialType, newMaterials);
        final String typeCode = materialTypePE.getCode();
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getEntityTypeDAO(EntityKind.MATERIAL);
                    will(returnValue(entityTypeDAO));

                    one(propertiesBatchManager)
                            .manageProperties(materialTypePE, newMaterials, null);

                    one(entityTypeDAO).tryToFindEntityTypeByCode(typeCode);
                    will(returnValue(materialTypePE));

                    one(genericBusinessObjectFactory).createMaterialTable(session);
                    will(returnValue(materialTable));

                    one(materialTable).add(newMaterials, materialTypePE);
                    one(materialTable).save();
                    one(materialTable).getMaterials();
                }
            });
        createServer().registerMaterials(SESSION_TOKEN, Collections.singletonList(nmwt));
        context.assertIsSatisfied();
    }

    @Test
    public final void testUpdateMaterials()
    {
        prepareGetSession();
        final MaterialTypePE materialTypePE = CommonTestUtils.createMaterialType();
        final MaterialType materialType = MaterialTypeTranslator.translateSimple(materialTypePE);
        final NewMaterial m1 = createNewMaterial("M1");
        final NewMaterial m2 = createNewMaterial("M2");
        final List<NewMaterialsWithTypes> newMaterials =
                Collections.singletonList(new NewMaterialsWithTypes(materialType, Arrays.asList(m1,
                        m2)));
        prepareMaterialUpdate(materialTypePE, false, newMaterials, m1, m2);

        int updateCount = createServer().updateMaterials(SESSION_TOKEN, newMaterials, false);

        assertEquals(2, updateCount);
        context.assertIsSatisfied();
    }

    @Test
    public final void testUpdateMaterialsFailedBecauseOfDuplicates()
    {
        prepareGetSession();
        final MaterialTypePE materialTypePE = CommonTestUtils.createMaterialType();
        final MaterialType materialType = MaterialTypeTranslator.translateSimple(materialTypePE);
        final NewMaterial m1 = createNewMaterial("M1");
        final NewMaterial m2 = createNewMaterial("M1");

        try
        {
            createServer().updateMaterials(
                    SESSION_TOKEN,
                    Collections.singletonList(new NewMaterialsWithTypes(materialType, Arrays
                            .asList(m1, m2))), false);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Following material(s) '[M1]' are duplicated.", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public final void testUpdateMaterialsIgnoreUnregistered()
    {
        prepareGetSession();
        final MaterialTypePE materialTypePE = CommonTestUtils.createMaterialType();
        final MaterialType materialType = MaterialTypeTranslator.translateSimple(materialTypePE);
        final NewMaterial m1 = createNewMaterial("M1");
        final NewMaterial m2 = createNewMaterial("M2");
        List<NewMaterialsWithTypes> newMaterials =
                Collections.singletonList(new NewMaterialsWithTypes(materialType, Arrays.asList(m1,
                        m2)));
        prepareMaterialUpdate(materialTypePE, false, newMaterials, m1);

        int updateCount = createServer().updateMaterials(SESSION_TOKEN, newMaterials, true);

        assertEquals(1, updateCount);
        context.assertIsSatisfied();
    }

    @Test
    public final void testUpdateMaterialsFailForUnregistered()
    {
        prepareGetSession();
        final MaterialTypePE materialTypePE = CommonTestUtils.createMaterialType();
        final MaterialType materialType = MaterialTypeTranslator.translateSimple(materialTypePE);
        final NewMaterial m1 = createNewMaterial("M1");
        final NewMaterial m2 = createNewMaterial("M2");
        List<NewMaterialsWithTypes> newMaterials =
                Collections.singletonList(new NewMaterialsWithTypes(materialType, Arrays.asList(m1,
                        m2)));
        prepareMaterialUpdate(materialTypePE, true, newMaterials, m1);

        try
        {
            createServer().updateMaterials(SESSION_TOKEN, newMaterials, false);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Can not update unregistered material 'M2'. "
                    + "Please use checkbox for ignoring unregistered materials.", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    protected void prepareMaterialUpdate(final MaterialTypePE materialTypePE,
            final boolean doNotUpdate, final List<NewMaterialsWithTypes> updatedMaterials,
            final NewMaterial... materialsToBeRegistered)
    {
        context.checking(new Expectations()
            {
                {
                    List<Material> existingMaterials = new ArrayList<Material>();
                    List<MaterialUpdateDTO> updates = new ArrayList<MaterialUpdateDTO>();
                    for (NewMaterial material : materialsToBeRegistered)
                    {
                        Material m = createMaterial(material);
                        existingMaterials.add(m);
                        updates.add(createUpdateDTO(m, material));
                    }
                    existingMaterials.add(createMaterial(createNewMaterial("A")));

                    one(genericBusinessObjectFactory)
                            .createMaterialLister(with(any(Session.class)));
                    will(returnValue(materialLister));
                    one(materialLister).list(with(new BaseMatcher<ListMaterialCriteria>()
                        {
                            @Override
                            public boolean matches(Object item)
                            {
                                assertTrue(item instanceof ListMaterialCriteria);
                                MaterialType materialType =
                                        ((ListMaterialCriteria) item).tryGetMaterialType();
                                assertEquals(materialTypePE.getCode(), materialType.getCode());
                                return true;
                            }

                            @Override
                            public void describeTo(Description description)
                            {
                                description.appendText(materialTypePE.getCode());
                            }
                        }), with(false));
                    will(returnValue(existingMaterials));

                    allowing(daoFactory).getEntityTypeDAO(EntityKind.MATERIAL);
                    will(returnValue(entityTypeDAO));

                    allowing(entityTypeDAO).tryToFindEntityTypeByCode(materialTypePE.getCode());
                    will(returnValue(materialTypePE));

                    if (doNotUpdate == false)
                    {
                        one(genericBusinessObjectFactory).createMaterialTable(session);
                        will(returnValue(materialTable));

                        one(materialTable).update(updates);
                        one(materialTable).save();
                    }

                    one(propertiesBatchManager).manageProperties(materialTypePE,
                            updatedMaterials.get(0).getNewEntities(), null);

                }

                private Material createMaterial(NewMaterial newMaterial)
                {
                    Material material = new Material();
                    material.setCode(newMaterial.getCode());
                    material.setId((long) newMaterial.getCode().hashCode());
                    material.setMaterialType(MaterialTypeTranslator.translateSimple(CommonTestUtils
                            .createMaterialType()));
                    return material;
                }

                private MaterialUpdateDTO createUpdateDTO(Material existingMaterial,
                        NewMaterial material)
                {
                    return new MaterialUpdateDTO(new TechId(existingMaterial.getId()), Arrays
                            .asList(material.getProperties()), existingMaterial
                            .getModificationDate());
                }
            });
    }

    @Test
    public void testEditMaterialNothingChanged() throws Exception
    {
        final TechId materialId = CommonTestUtils.TECH_ID;
        final List<IEntityProperty> properties = new ArrayList<IEntityProperty>();
        prepareGetSession();
        final Date version = new Date(1);
        final MaterialPE material = new MaterialPE();
        final Date newModificationDate = new Date(2);
        material.setModificationDate(newModificationDate);
        context.checking(new Expectations()
            {
                {
                    one(commonServer).updateMaterial(SESSION_TOKEN, materialId, properties, null,
                            version);
                    will(returnValue(newModificationDate));
                }
            });
        assertEquals(newModificationDate,
                createServer().updateMaterial(SESSION_TOKEN, materialId, properties, null, version));
        context.assertIsSatisfied();
    }

    public void testEditSampleNothingChanged() throws Exception
    {
        final TechId sampleId = CommonTestUtils.TECH_ID;
        final List<IEntityProperty> properties = new ArrayList<IEntityProperty>();
        prepareGetSession();
        final Collection<NewAttachment> attachments = Collections.<NewAttachment> emptyList();
        final Long id = 1L;
        final SamplePE sample = new SamplePE();
        sample.setId(id);
        Set<SampleRelationshipPE> newParents = new HashSet<SampleRelationshipPE>();
        sample.setParentRelationships(newParents);
        sample.setVersion(42);
        final SampleUpdatesDTO updates =
                new SampleUpdatesDTO(sampleId, properties, null, null, attachments, 0, null, null, null);
        context.checking(new Expectations()
            {
                {
                    one(genericBusinessObjectFactory).createSampleBO(session);
                    will(returnValue(sampleBO));

                    one(sampleBO).update(updates);
                    one(sampleBO).save();
                    allowing(sampleBO).getSample();
                    will(returnValue(sample));
                }
            });
        SampleUpdateResult result = createServer().updateSample(SESSION_TOKEN, updates);
        assertEquals(sample.getVersion(), result.getVersion());
        assertEquals(newParents.size(), result.getParents().size());
        context.assertIsSatisfied();
    }

    @Test
    public void testEditExperimentNothingChanged() throws Exception
    {
        final TechId experimentId = CommonTestUtils.TECH_ID;
        final ProjectIdentifier newProjectIdentifier =
                new ProjectIdentifier(GROUP_1, PROJECT_1);
        final ExperimentUpdatesDTO updates = new ExperimentUpdatesDTO();
        updates.setExperimentId(experimentId);
        updates.setProjectIdentifier(newProjectIdentifier);
        final ExperimentPE experiment = new ExperimentPE();
        final Long id = 1L;
        experiment.setId(id);
        experiment.setVersion(2);
        ArrayList<SamplePE> newSamples = new ArrayList<SamplePE>();
        experiment.setSamples(newSamples);
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    allowing(experimentDAO).getSampleCodes(with(any(ExperimentPE.class)));

                    one(genericBusinessObjectFactory).createExperimentBO(session);
                    will(returnValue(experimentBO));

                    one(experimentBO).update(updates);
                    one(experimentBO).save();
                    allowing(experimentBO).getExperiment();
                    will(returnValue(experiment));
                }
            });
        ExperimentUpdateResult result = createServer().updateExperiment(SESSION_TOKEN, updates);
        assertEquals(experiment.getVersion(), result.getVersion());
        assertEquals(newSamples.size(), result.getSamples().size());
        context.assertIsSatisfied();
    }

    @Test
    public void testBulkEditExperimentNothingChanged() throws Exception
    {
        final ExperimentBatchUpdateDetails updateDetails = new ExperimentBatchUpdateDetails();
        final UpdatedBasicExperiment updatedExperiment =
                new UpdatedBasicExperiment(EXPERIMENT_IDENTIFIER1, null, updateDetails);
        ExperimentType expType = new ExperimentType();
        expType.setCode(EXPERIMENT_TYPE);
        final List<UpdatedBasicExperiment> experiments =
                Collections.singletonList(updatedExperiment);
        UpdatedExperimentsWithType updatedExperiments =
                new UpdatedExperimentsWithType(expType, experiments);
        final ExperimentTypePE experimentTypePE = createExperimentType(EXPERIMENT_TYPE);
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getEntityTypeDAO(EntityKind.EXPERIMENT);
                    will(returnValue(entityTypeDAO));
                    one(entityTypeDAO).tryToFindEntityTypeByCode(EXPERIMENT_TYPE);
                    will(returnValue(experimentTypePE));
                    one(propertiesBatchManager).manageProperties(experimentTypePE, experiments,
                            null);
                    one(genericBusinessObjectFactory).createExperimentTable(session);
                    will(returnValue(experimentTable));
                    one(experimentTable).prepareForUpdate(
                            with(new BaseMatcher<List<ExperimentBatchUpdatesDTO>>()
                                {

                                    @Override
                                    public boolean matches(Object item)
                                    {
                                        if (item instanceof List<?>)
                                        {
                                            @SuppressWarnings("unchecked")
                                            List<ExperimentBatchUpdatesDTO> updates =
                                                    (List<ExperimentBatchUpdatesDTO>) item;
                                            if (1 == updates.size())
                                            {
                                                return true;
                                            }
                                            return false;
                                        } else
                                        {
                                            return false;
                                        }
                                    }

                                    @Override
                                    public void describeTo(Description description)
                                    {

                                    }
                                }));
                    one(experimentTable).save();
                }
            });
        createServer().updateExperiments(SESSION_TOKEN, updatedExperiments);
        context.assertIsSatisfied();
    }

    @Test
    public void testFailRegisterExperimentsWithNullParameter() throws Exception
    {
        boolean asserionError = false;
        try
        {
            createServer().registerExperiments(SESSION_TOKEN, null);
        } catch (AssertionError ex)
        {
            asserionError = true;
        }
        assertTrue(asserionError);
    }

    @Test
    public void testFailRegisterExperimentsWithNullType() throws Exception
    {
        boolean asserionError = false;
        try
        {
            createServer().registerExperiments(SESSION_TOKEN,
                    new NewExperimentsWithType(null, createNewExperiments()));
        } catch (AssertionError ex)
        {
            asserionError = true;
        }
        assertTrue(asserionError);
    }

    @Test
    public void testFailRegisterExperimentsWithNullCollection() throws Exception
    {
        boolean asserionError = false;
        try
        {
            createServer().registerExperiments(SESSION_TOKEN, new NewExperimentsWithType("", null));
        } catch (AssertionError ex)
        {
            asserionError = true;
        }
        assertTrue(asserionError);
    }

    @Test
    public void testRegisterExperimentEmptyCollection() throws Exception
    {
        prepareGetSession();
        final NewExperimentsWithType experiments =
                new NewExperimentsWithType(EXPERIMENT_TYPE, createNewExperiments());
        createServer().registerExperiments(SESSION_TOKEN, experiments);
        context.assertIsSatisfied();
    }

    @Test
    public void testRegisterExperiment() throws Exception
    {
        prepareGetSession();
        final ExperimentTypePE experimentTypePE = createExperimentType(EXPERIMENT_TYPE);
        final List<NewBasicExperiment> entities =
                createNewExperiments(new NewBasicExperiment(EXPERIMENT_IDENTIFIER1),
                        new NewBasicExperiment(EXPERIMENT_IDENTIFIER2));
        final NewExperimentsWithType experiments =
                new NewExperimentsWithType(EXPERIMENT_TYPE, entities);
        context.checking(new Expectations()
            {
                {
                    one(propertiesBatchManager).manageProperties(experimentTypePE, entities, null);
                    one(daoFactory).getEntityTypeDAO(EntityKind.EXPERIMENT);
                    will(returnValue(entityTypeDAO));
                    one(entityTypeDAO).tryToFindEntityTypeByCode(EXPERIMENT_TYPE);
                    will(returnValue(experimentTypePE));
                    one(genericBusinessObjectFactory).createExperimentTable(session);
                    will(returnValue(experimentTable));
                    one(experimentTable).add(entities, experimentTypePE);
                    one(experimentTable).save();
                }
            });
        createServer().registerExperiments(SESSION_TOKEN, experiments);
        context.assertIsSatisfied();
    }
}
