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

package ch.systemsx.cisd.openbis.systemtest.optimistic_locking;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;
import ch.systemsx.cisd.openbis.generic.server.util.TimeIntervalChecker;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetBatchUpdateDetails;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.PropertyBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.IObjectId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.dataset.DataSetCodeId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.AtomicEntityOperationDetailsBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.systemtest.base.builder.DataSetUpdateBuilder;

/**
 * @author Franz-Josef Elmer
 */
public class DataSetOptimisticLockingTest extends OptimisticLockingTestCase
{
    @Test
    public void testChangeFileFormatTypeViaPerformEntityOperation()
    {
        Experiment experiment = toolBox.createAndLoadExperiment(1);
        AbstractExternalData dataSet = toolBox.createAndLoadDataSet(toolBox.dataSet("DS-1", experiment));
        assertEquals("XML", ((PhysicalDataSet) dataSet).getFileFormatType().getCode());
        DataSetBatchUpdatesDTO dataSetBatchUpdates = new DataSetBatchUpdatesDTO();
        dataSetBatchUpdates.setVersion(dataSet.getVersion());
        dataSetBatchUpdates.setDatasetCode(dataSet.getCode());
        dataSetBatchUpdates.setDatasetId(new TechId(dataSet));
        dataSetBatchUpdates.setFileFormatTypeCode("HDF5");
        dataSetBatchUpdates.setProperties(Arrays.<IEntityProperty> asList());
        DataSetBatchUpdateDetails updateDetails = new DataSetBatchUpdateDetails();
        updateDetails.setFileFormatUpdateRequested(true);
        dataSetBatchUpdates.setDetails(updateDetails);
        AtomicEntityOperationDetailsBuilder builder =
                new AtomicEntityOperationDetailsBuilder().dataSetUpdate(dataSetBatchUpdates).user(
                        "test");
        TimeIntervalChecker timeIntervalChecker = toolBox.createTimeIntervalChecker();

        etlService.performEntityOperations(systemSessionToken, builder.getDetails());

        AbstractExternalData loadedDataSet = toolBox.loadDataSet(systemSessionToken, dataSet.getCode());
        assertEquals("HDF5", ((PhysicalDataSet) loadedDataSet).getFileFormatType().getCode());
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedDataSet, "test");
    }

    @Test
    public void testChangeFileFormatTypeViaUpdateDataSet()
    {
        Experiment experiment = toolBox.createAndLoadExperiment(1);
        AbstractExternalData dataSet = toolBox.createAndLoadDataSet(toolBox.dataSet("DS-1", experiment));
        assertEquals("XML", ((PhysicalDataSet) dataSet).getFileFormatType().getCode());
        DataSetUpdatesDTO dataSetUpdates =
                new DataSetUpdateBuilder(commonServer, genericServer, dataSet).create();
        dataSetUpdates.setFileFormatTypeCode("HDF5");
        String sessionToken = logIntoCommonClientService().getSessionID();
        TimeIntervalChecker timeIntervalChecker = toolBox.createTimeIntervalChecker();

        etlService.updateDataSet(sessionToken, dataSetUpdates);

        AbstractExternalData loadedDataSet = toolBox.loadDataSet(systemSessionToken, dataSet.getCode());
        assertEquals("HDF5", ((PhysicalDataSet) loadedDataSet).getFileFormatType().getCode());
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedDataSet, "test");
    }

    @Test
    public void testChangeFileFormatTypeOfAStaleDataSetViaUpdateDataSet()
    {
        Experiment experiment = toolBox.createAndLoadExperiment(1);
        AbstractExternalData dataSet = toolBox.createAndLoadDataSet(toolBox.dataSet("DS-1", experiment));
        assertEquals("XML", ((PhysicalDataSet) dataSet).getFileFormatType().getCode());
        DataSetUpdatesDTO dataSetUpdates =
                new DataSetUpdateBuilder(commonServer, genericServer, dataSet).create();
        dataSetUpdates.setFileFormatTypeCode("HDF5");
        String sessionToken = logIntoCommonClientService().getSessionID();
        etlService.updateDataSet(sessionToken, dataSetUpdates);

        try
        {
            etlService.updateDataSet(sessionToken, dataSetUpdates);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Data set has been modified in the meantime. Reopen tab to be able "
                    + "to continue with refreshed data.", ex.getMessage());
        }
    }

    @Test
    public void testChangePropertyViaPerformEntityOperation()
    {
        Experiment experiment = toolBox.createAndLoadExperiment(1);
        NewDataSet newDataSet = toolBox.dataSet("DS-1", experiment);
        newDataSet.setDataSetType(new DataSetType("HCS_IMAGE"));
        newDataSet.setDataSetKind(DataSetKind.PHYSICAL);
        newDataSet.setDataSetProperties(Arrays.asList(new NewProperty("COMMENT", "1")));
        AbstractExternalData dataSet = toolBox.createAndLoadDataSet(newDataSet);
        DataSetBatchUpdatesDTO dataSetBatchUpdates = new DataSetBatchUpdatesDTO();
        dataSetBatchUpdates.setVersion(dataSet.getVersion());
        dataSetBatchUpdates.setDatasetCode(dataSet.getCode());
        dataSetBatchUpdates.setDatasetId(new TechId(dataSet));
        dataSetBatchUpdates.setProperties(Arrays.<IEntityProperty> asList(new PropertyBuilder(
                "COMMENT").value("2").getProperty()));
        DataSetBatchUpdateDetails updateDetails = new DataSetBatchUpdateDetails();
        updateDetails.setPropertiesToUpdate(new HashSet<String>(Arrays.asList("COMMENT")));
        dataSetBatchUpdates.setDetails(updateDetails);
        AtomicEntityOperationDetailsBuilder builder =
                new AtomicEntityOperationDetailsBuilder().dataSetUpdate(dataSetBatchUpdates).user(
                        "test");
        TimeIntervalChecker timeIntervalChecker = toolBox.createTimeIntervalChecker();

        etlService.performEntityOperations(systemSessionToken, builder.getDetails());

        AbstractExternalData loadedDataSet = toolBox.loadDataSet(systemSessionToken, dataSet.getCode());
        assertEquals("[COMMENT: 2]", loadedDataSet.getProperties().toString());
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedDataSet, "test");
    }

    @Test
    public void testChangePropertyViaUpdateDataSet()
    {
        Experiment experiment = toolBox.createAndLoadExperiment(1);
        NewDataSet newDataSet = toolBox.dataSet("DS-1", experiment);
        newDataSet.setDataSetType(new DataSetType("HCS_IMAGE"));
        newDataSet.setDataSetKind(DataSetKind.PHYSICAL);
        newDataSet.setDataSetProperties(Arrays.asList(new NewProperty("COMMENT", "1")));
        AbstractExternalData dataSet = toolBox.createAndLoadDataSet(newDataSet);
        DataSetUpdatesDTO dataSetUpdates =
                new DataSetUpdateBuilder(commonServer, genericServer, dataSet).create();
        dataSetUpdates.getProperties().add(new PropertyBuilder("COMMENT").value("2").getProperty());
        String sessionToken = logIntoCommonClientService().getSessionID();
        TimeIntervalChecker timeIntervalChecker = toolBox.createTimeIntervalChecker();

        etlService.updateDataSet(sessionToken, dataSetUpdates);

        AbstractExternalData loadedDataSet = toolBox.loadDataSet(systemSessionToken, dataSet.getCode());
        assertEquals("[COMMENT: 2]", loadedDataSet.getProperties().toString());
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedDataSet, "test");
    }

    @Test
    public void testChangePropertyOfAStaleDataSetViaPerformEntityOperation()
    {
        Experiment experiment = toolBox.createAndLoadExperiment(1);
        NewDataSet newDataSet = toolBox.dataSet("DS-1", experiment);
        newDataSet.setDataSetType(new DataSetType("HCS_IMAGE"));
        newDataSet.setDataSetKind(DataSetKind.PHYSICAL);
        newDataSet.setDataSetProperties(Arrays.asList(new NewProperty("COMMENT", "1")));
        AbstractExternalData dataSet = toolBox.createAndLoadDataSet(newDataSet);
        DataSetBatchUpdatesDTO dataSetBatchUpdates = new DataSetBatchUpdatesDTO();
        dataSetBatchUpdates.setVersion(dataSet.getVersion());
        dataSetBatchUpdates.setDatasetCode(dataSet.getCode());
        dataSetBatchUpdates.setDatasetId(new TechId(dataSet));
        dataSetBatchUpdates.setProperties(Arrays.<IEntityProperty> asList(new PropertyBuilder(
                "COMMENT").value("2").getProperty()));
        DataSetBatchUpdateDetails updateDetails = new DataSetBatchUpdateDetails();
        updateDetails.setPropertiesToUpdate(new HashSet<String>(Arrays.asList("COMMENT")));
        dataSetBatchUpdates.setDetails(updateDetails);
        AtomicEntityOperationDetailsBuilder builder =
                new AtomicEntityOperationDetailsBuilder().dataSetUpdate(dataSetBatchUpdates).user(
                        "test");
        etlService.performEntityOperations(systemSessionToken, builder.getDetails());

        try
        {
            etlService.performEntityOperations(systemSessionToken, builder.getDetails());
            fail("EnvironmentFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Data set DS-1 has been updated since it was retrieved.\n"
                    + "[Current: 1, Retrieved: 0]", ex.getMessage());
        }
    }

    @Test
    public void testAddMetaProjectViaPerformEntityOperation()
    {
        Experiment experiment = toolBox.createAndLoadExperiment(1);
        AbstractExternalData dataSet = toolBox.createAndLoadDataSet(toolBox.dataSet("DS-1", experiment));
        MetaprojectUpdatesDTO metaProjectUpdate = new MetaprojectUpdatesDTO();
        metaProjectUpdate.setAddedEntities(Arrays.asList(new DataSetCodeId(dataSet.getCode())));
        metaProjectUpdate.setRemovedEntities(Arrays.<IObjectId> asList());
        metaProjectUpdate.setMetaprojectId(new TechId(1));
        AtomicEntityOperationDetailsBuilder builder =
                new AtomicEntityOperationDetailsBuilder().metaProjectUpdate(metaProjectUpdate)
                        .user("test");

        etlService.performEntityOperations(systemSessionToken, builder.getDetails());

        String sessionToken = logIntoCommonClientService().getSessionID();
        AbstractExternalData loadedDataSet = toolBox.loadDataSet(sessionToken, dataSet.getCode());
        assertEquals(dataSet.getModifier(), loadedDataSet.getModifier());
        assertEquals(dataSet.getModificationDate(), loadedDataSet.getModificationDate());
        assertEquals("/test/TEST_METAPROJECTS",
                toolBox.renderMetaProjects(loadedDataSet.getMetaprojects()));
        assertEquals(
                "",
                toolBox.renderMetaProjects(toolBox.loadDataSet(systemSessionToken,
                        dataSet.getCode()).getMetaprojects()));
    }

    @Test
    public void testAddMetaProjectViaUpdateDataSet()
    {
        Experiment experiment = toolBox.createAndLoadExperiment(1);
        AbstractExternalData dataSet = toolBox.createAndLoadDataSet(toolBox.dataSet("DS-1", experiment));
        DataSetUpdatesDTO dataSetUpdates =
                new DataSetUpdateBuilder(commonServer, genericServer, dataSet).create();
        dataSetUpdates.setMetaprojectsOrNull(new String[] { "TEST_METAPROJECTS" });
        SessionContext sessionContext = logIntoCommonClientService();
        String sessionToken = sessionContext.getSessionID();

        etlService.updateDataSet(sessionToken, dataSetUpdates);

        AbstractExternalData loadedDataSet = toolBox.loadDataSet(sessionToken, dataSet.getCode());
        assertEquals(dataSet.getModifier(), loadedDataSet.getModifier());
        assertEquals(dataSet.getModificationDate(), loadedDataSet.getModificationDate());
        assertEquals("/test/TEST_METAPROJECTS",
                toolBox.renderMetaProjects(loadedDataSet.getMetaprojects()));
        assertEquals(
                "",
                toolBox.renderMetaProjects(toolBox.loadDataSet(systemSessionToken,
                        dataSet.getCode()).getMetaprojects()));
    }

    @Test
    public void testChangeExperimentViaPerformEntityOperation()
    {
        Experiment experiment1 = toolBox.createAndLoadExperiment(1);
        AbstractExternalData dataSet = toolBox.createAndLoadDataSet(toolBox.dataSet("DS-1", experiment1));
        assertEquals(experiment1.getIdentifier(), dataSet.getExperiment().getIdentifier());
        Experiment experiment2 = toolBox.createAndLoadExperiment(2);
        DataSetBatchUpdatesDTO dataSetBatchUpdates = new DataSetBatchUpdatesDTO();
        dataSetBatchUpdates.setVersion(dataSet.getVersion());
        dataSetBatchUpdates.setDatasetCode(dataSet.getCode());
        dataSetBatchUpdates.setDatasetId(new TechId(dataSet));
        dataSetBatchUpdates.setProperties(Arrays.<IEntityProperty> asList());
        dataSetBatchUpdates.setExperimentIdentifierOrNull(new ExperimentIdentifier(experiment2));
        DataSetBatchUpdateDetails updateDetails = new DataSetBatchUpdateDetails();
        updateDetails.setExperimentUpdateRequested(true);
        dataSetBatchUpdates.setDetails(updateDetails);
        AtomicEntityOperationDetailsBuilder builder =
                new AtomicEntityOperationDetailsBuilder().dataSetUpdate(dataSetBatchUpdates).user(
                        "test");
        TimeIntervalChecker timeIntervalChecker = toolBox.createTimeIntervalChecker();

        etlService.performEntityOperations(systemSessionToken, builder.getDetails());

        AbstractExternalData loadedDataSet = toolBox.loadDataSet(dataSet.getCode());
        assertEquals(experiment2.getIdentifier(), loadedDataSet.getExperiment().getIdentifier());
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedDataSet, "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker,
                toolBox.loadExperiment(experiment1), "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker,
                toolBox.loadExperiment(experiment2), "test");
        assertEquals(0,
                etlService.listDataSetsByExperimentID(systemSessionToken, new TechId(experiment1))
                        .size());
        assertEquals(1,
                etlService.listDataSetsByExperimentID(systemSessionToken, new TechId(experiment2))
                        .size());
    }

    @Test
    public void testChangeExperimentViaUpdateDataSet()
    {
        Experiment experiment1 = toolBox.createAndLoadExperiment(1);
        AbstractExternalData dataSet = toolBox.createAndLoadDataSet(toolBox.dataSet("DS-1", experiment1));
        assertEquals(experiment1.getIdentifier(), dataSet.getExperiment().getIdentifier());
        Experiment experiment2 = toolBox.createAndLoadExperiment(2);
        DataSetUpdatesDTO dataSetUpdates =
                new DataSetUpdateBuilder(commonServer, genericServer, dataSet).toExperiment(
                        experiment2).create();
        String sessionToken = logIntoCommonClientService().getSessionID();
        TimeIntervalChecker timeIntervalChecker = toolBox.createTimeIntervalChecker();

        etlService.updateDataSet(sessionToken, dataSetUpdates);

        AbstractExternalData loadedDataSet = toolBox.loadDataSet(dataSet.getCode());
        assertEquals(experiment2.getIdentifier(), loadedDataSet.getExperiment().getIdentifier());
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedDataSet, "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker,
                toolBox.loadExperiment(experiment1), "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker,
                toolBox.loadExperiment(experiment2), "test");
        assertEquals(0,
                etlService.listDataSetsByExperimentID(systemSessionToken, new TechId(experiment1))
                        .size());
        assertEquals(1,
                etlService.listDataSetsByExperimentID(systemSessionToken, new TechId(experiment2))
                        .size());
    }

    @Test
    public void testChangeSampleViaPerformEntityOperation()
    {
        Experiment experiment1 = toolBox.createAndLoadExperiment(1);
        Sample sample1 = toolBox.createAndLoadSample(1, experiment1);
        AbstractExternalData dataSet = toolBox.createAndLoadDataSet(toolBox.dataSet("DS-1", sample1));
        assertEquals(experiment1.getIdentifier(), dataSet.getExperiment().getIdentifier());
        assertEquals(sample1.getIdentifier(), dataSet.getSampleIdentifier());
        Experiment experiment2 = toolBox.createAndLoadExperiment(2);
        Sample sample2 = toolBox.createAndLoadSample(2, experiment2);
        DataSetBatchUpdatesDTO dataSetBatchUpdates = new DataSetBatchUpdatesDTO();
        dataSetBatchUpdates.setVersion(dataSet.getVersion());
        dataSetBatchUpdates.setDatasetCode(dataSet.getCode());
        dataSetBatchUpdates.setDatasetId(new TechId(dataSet));
        dataSetBatchUpdates.setProperties(Arrays.<IEntityProperty> asList());
        dataSetBatchUpdates.setSampleIdentifierOrNull(SampleIdentifierFactory.parse(sample2));
        DataSetBatchUpdateDetails updateDetails = new DataSetBatchUpdateDetails();
        updateDetails.setSampleUpdateRequested(true);
        dataSetBatchUpdates.setDetails(updateDetails);
        AtomicEntityOperationDetailsBuilder builder =
                new AtomicEntityOperationDetailsBuilder().dataSetUpdate(dataSetBatchUpdates).user(
                        "test");
        TimeIntervalChecker timeIntervalChecker = toolBox.createTimeIntervalChecker();

        etlService.performEntityOperations(systemSessionToken, builder.getDetails());

        AbstractExternalData loadedDataSet = toolBox.loadDataSet(dataSet.getCode());
        assertEquals(experiment2.getIdentifier(), loadedDataSet.getExperiment().getIdentifier());
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedDataSet, "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker,
                toolBox.loadExperiment(experiment1), "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker,
                toolBox.loadExperiment(experiment2), "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker,
                toolBox.loadSample(sample1), "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker,
                toolBox.loadSample(sample2), "test");
        assertEquals(0,
                etlService.listDataSetsByExperimentID(systemSessionToken, new TechId(experiment1))
                        .size());
        assertEquals(1,
                etlService.listDataSetsByExperimentID(systemSessionToken, new TechId(experiment2))
                        .size());
        assertEquals(0,
                etlService.listDataSetsBySampleID(systemSessionToken, new TechId(sample1), true)
                        .size());
        assertEquals(1,
                etlService.listDataSetsBySampleID(systemSessionToken, new TechId(sample2), true)
                        .size());
    }

    @Test
    public void testChangeSampleViaUpdateDataSet()
    {
        Experiment experiment1 = toolBox.createAndLoadExperiment(1);
        Sample sample1 = toolBox.createAndLoadSample(1, experiment1);
        AbstractExternalData dataSet = toolBox.createAndLoadDataSet(toolBox.dataSet("DS-1", sample1));
        assertEquals(experiment1.getIdentifier(), dataSet.getExperiment().getIdentifier());
        assertEquals(sample1.getIdentifier(), dataSet.getSampleIdentifier());
        Experiment experiment2 = toolBox.createAndLoadExperiment(2);
        Sample sample2 = toolBox.createAndLoadSample(2, experiment2);
        DataSetUpdatesDTO dataSetUpdates =
                new DataSetUpdateBuilder(commonServer, genericServer, dataSet).toSample(sample2)
                        .create();
        String sessionToken = logIntoCommonClientService().getSessionID();
        TimeIntervalChecker timeIntervalChecker = toolBox.createTimeIntervalChecker();

        etlService.updateDataSet(sessionToken, dataSetUpdates);

        AbstractExternalData loadedDataSet = toolBox.loadDataSet(dataSet.getCode());
        assertEquals(experiment2.getIdentifier(), loadedDataSet.getExperiment().getIdentifier());
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedDataSet, "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker,
                toolBox.loadExperiment(experiment1), "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker,
                toolBox.loadExperiment(experiment2), "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker,
                toolBox.loadSample(sample1), "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker,
                toolBox.loadSample(sample2), "test");
        assertEquals(0,
                etlService.listDataSetsByExperimentID(systemSessionToken, new TechId(experiment1))
                        .size());
        assertEquals(1,
                etlService.listDataSetsByExperimentID(systemSessionToken, new TechId(experiment2))
                        .size());
        assertEquals(0,
                etlService.listDataSetsBySampleID(systemSessionToken, new TechId(sample1), true)
                        .size());
        assertEquals(1,
                etlService.listDataSetsBySampleID(systemSessionToken, new TechId(sample2), true)
                        .size());
    }

    @Test
    public void testCreateChildDataSets()
    {
        Experiment experiment1 = toolBox.createAndLoadExperiment(1);
        AbstractExternalData dataSet = toolBox.createAndLoadDataSet(toolBox.dataSet("DS-1", experiment1));
        Experiment experiment2 = toolBox.createAndLoadExperiment(2);
        NewDataSet child = toolBox.dataSet("DS-1-C", experiment2);
        child.setParentDataSetCodes(Arrays.asList(dataSet.getCode()));
        NewDataSet grandChild = toolBox.dataSet("DS-1-GC", experiment2);
        grandChild.setParentDataSetCodes(Arrays.asList(child.getCode()));
        AtomicEntityOperationDetailsBuilder builder =
                new AtomicEntityOperationDetailsBuilder().user("test").dataSet(child)
                        .dataSet(grandChild);
        TimeIntervalChecker timeIntervalChecker = toolBox.createTimeIntervalChecker();

        etlService.performEntityOperations(systemSessionToken, builder.getDetails());

        AbstractExternalData loadedDataSet = toolBox.loadDataSet(dataSet.getCode());
        AbstractExternalData loadedChild = toolBox.loadDataSet(child.getCode());
        AbstractExternalData loadedGrandChild = toolBox.loadDataSet(grandChild.getCode());
        assertEquals(experiment1.getIdentifier(), loadedDataSet.getExperiment().getIdentifier());
        assertEquals("[DS-1-C]", toolBox.extractCodes(loadedDataSet.getChildren()).toString());
        assertEquals(experiment2.getIdentifier(), loadedChild.getExperiment().getIdentifier());
        assertEquals("[DS-1]", toolBox.extractCodes(loadedChild.getParents()).toString());
        assertEquals("[DS-1-GC]", toolBox.extractCodes(loadedChild.getChildren()).toString());
        assertEquals(experiment2.getIdentifier(), loadedGrandChild.getExperiment().getIdentifier());
        assertEquals("[DS-1-C]", toolBox.extractCodes(loadedGrandChild.getParents()).toString());
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedDataSet, "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedChild, "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedGrandChild,
                "test");
    }

    @Test
    public void testChangeParentViaPerformEntityOperation()
    {
        Experiment experiment1 = toolBox.createAndLoadExperiment(1);
        AbstractExternalData dataSet1 = toolBox.createAndLoadDataSet(toolBox.dataSet("DS-1", experiment1));
        AbstractExternalData dataSet2 = toolBox.createAndLoadDataSet(toolBox.dataSet("DS-2", experiment1));
        Experiment experiment2 = toolBox.createAndLoadExperiment(2);
        NewDataSet newDataSet = toolBox.dataSet("DS-3", experiment2);
        newDataSet.setParentDataSetCodes(Arrays.asList(dataSet1.getCode()));
        AbstractExternalData child = toolBox.createAndLoadDataSet(newDataSet);
        DataSetBatchUpdatesDTO dataSetBatchUpdates = new DataSetBatchUpdatesDTO();
        dataSetBatchUpdates.setVersion(child.getVersion());
        dataSetBatchUpdates.setDatasetCode(child.getCode());
        dataSetBatchUpdates.setDatasetId(new TechId(child));
        dataSetBatchUpdates.setProperties(Arrays.<IEntityProperty> asList());
        dataSetBatchUpdates.setModifiedParentDatasetCodesOrNull(new String[]
        { dataSet2.getCode() });
        DataSetBatchUpdateDetails updateDetails = new DataSetBatchUpdateDetails();
        updateDetails.setParentsUpdateRequested(true);
        dataSetBatchUpdates.setDetails(updateDetails);
        AtomicEntityOperationDetailsBuilder builder =
                new AtomicEntityOperationDetailsBuilder().dataSetUpdate(dataSetBatchUpdates).user(
                        "test");
        TimeIntervalChecker timeIntervalChecker = toolBox.createTimeIntervalChecker();

        etlService.performEntityOperations(systemSessionToken, builder.getDetails());

        AbstractExternalData loadedDataSet1 = toolBox.loadDataSet(dataSet1.getCode());
        AbstractExternalData loadedDataSet2 = toolBox.loadDataSet(dataSet2.getCode());
        AbstractExternalData loadedChild = toolBox.loadDataSet(child.getCode());
        assertEquals(experiment1.getIdentifier(), loadedDataSet1.getExperiment().getIdentifier());
        assertEquals("[]", toolBox.extractCodes(loadedDataSet1.getChildren()).toString());
        assertEquals("[DS-3]", toolBox.extractCodes(loadedDataSet2.getChildren()).toString());
        assertEquals("[DS-2]", toolBox.extractCodes(loadedChild.getParents()).toString());
        assertEquals(experiment1.getIdentifier(), loadedDataSet2.getExperiment().getIdentifier());
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedDataSet1, "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedDataSet2, "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedChild, "test");
    }

    @Test
    public void testChangeParentViaUpdateDataSet()
    {
        Experiment experiment1 = toolBox.createAndLoadExperiment(1);
        AbstractExternalData dataSet1 = toolBox.createAndLoadDataSet(toolBox.dataSet("DS-1", experiment1));
        AbstractExternalData dataSet2 = toolBox.createAndLoadDataSet(toolBox.dataSet("DS-2", experiment1));
        Experiment experiment2 = toolBox.createAndLoadExperiment(2);
        NewDataSet newDataSet = toolBox.dataSet("DS-3", experiment2);
        newDataSet.setParentDataSetCodes(Arrays.asList(dataSet1.getCode()));
        AbstractExternalData child = toolBox.createAndLoadDataSet(newDataSet);
        DataSetUpdatesDTO dataSetUpdates =
                new DataSetUpdateBuilder(commonServer, genericServer, child).create();
        dataSetUpdates.setModifiedParentDatasetCodesOrNull(new String[]
        { dataSet2.getCode() });
        String sessionToken = logIntoCommonClientService().getSessionID();
        TimeIntervalChecker timeIntervalChecker = toolBox.createTimeIntervalChecker();

        etlService.updateDataSet(sessionToken, dataSetUpdates);

        AbstractExternalData loadedDataSet1 = toolBox.loadDataSet(dataSet1.getCode());
        AbstractExternalData loadedDataSet2 = toolBox.loadDataSet(dataSet2.getCode());
        AbstractExternalData loadedChild = toolBox.loadDataSet(child.getCode());
        assertEquals(experiment1.getIdentifier(), loadedDataSet1.getExperiment().getIdentifier());
        assertEquals("[]", toolBox.extractCodes(loadedDataSet1.getChildren()).toString());
        assertEquals("[DS-3]", toolBox.extractCodes(loadedDataSet2.getChildren()).toString());
        assertEquals("[DS-2]", toolBox.extractCodes(loadedChild.getParents()).toString());
        assertEquals(experiment1.getIdentifier(), loadedDataSet2.getExperiment().getIdentifier());
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedDataSet1, "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedDataSet2, "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedChild, "test");
    }

    @Test
    public void testCreateContainerAndContainedDataSets()
    {
        Experiment experiment = toolBox.createAndLoadExperiment(1);
        NewContainerDataSet containerDataSet = toolBox.containerDataSet("DS-1", experiment);
        NewDataSet containedDataSet = toolBox.dataSet("DS-2", experiment);
        containerDataSet.setContainedDataSetCodes(Arrays.asList(containedDataSet.getCode()));
        AtomicEntityOperationDetailsBuilder builder =
                new AtomicEntityOperationDetailsBuilder().user("test").dataSet(containerDataSet)
                        .dataSet(containedDataSet);
        TimeIntervalChecker timeIntervalChecker = toolBox.createTimeIntervalChecker();

        etlService.performEntityOperations(systemSessionToken, builder.getDetails());

        ContainerDataSet loadedContainerDataSet =
                (ContainerDataSet) toolBox.loadDataSet(containerDataSet.getCode());
        AbstractExternalData loadedContainedDataSet = toolBox.loadDataSet(containedDataSet.getCode());
        assertEquals("[DS-2]", toolBox.extractCodes(loadedContainerDataSet.getContainedDataSets())
                .toString());
        assertEquals("DS-1", loadedContainedDataSet.tryGetContainer().getCode());
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedContainerDataSet,
                "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedContainedDataSet,
                "test");
    }

    @Test
    public void testMoveContainedDataSetToAnotherContainerViaUpdateDataSet()
    {
        Experiment experiment = toolBox.createAndLoadExperiment(1);
        NewContainerDataSet containerDataSet1 = toolBox.containerDataSet("DS-1", experiment);
        AbstractExternalData containedDataSet = toolBox.createAndLoadDataSet(toolBox.dataSet("DS-3", experiment));
        containerDataSet1.setContainedDataSetCodes(Arrays.asList(containedDataSet.getCode()));
        toolBox.createAndLoadDataSet(containerDataSet1);
        NewContainerDataSet containerDataSet2 = toolBox.containerDataSet("DS-2", experiment);
        toolBox.createAndLoadDataSet(containerDataSet2);
        AbstractExternalData dataSet = toolBox.loadDataSet(containedDataSet.getCode());
        assertEquals("DS-1", dataSet.tryGetContainer().getCode());
        DataSetBatchUpdatesDTO dataSetBatchUpdates = new DataSetBatchUpdatesDTO();
        dataSetBatchUpdates.setVersion(dataSet.getVersion());
        dataSetBatchUpdates.setDatasetCode(dataSet.getCode());
        dataSetBatchUpdates.setDatasetId(new TechId(dataSet));
        dataSetBatchUpdates.setProperties(Arrays.<IEntityProperty> asList());
        dataSetBatchUpdates.setExperimentIdentifierOrNull(new ExperimentIdentifier(experiment));
        dataSetBatchUpdates.setModifiedContainerDatasetCodeOrNull(containerDataSet2.getCode());
        dataSetBatchUpdates.setFileFormatTypeCode("XML");
        DataSetBatchUpdateDetails updateDetails = new DataSetBatchUpdateDetails();
        updateDetails.setContainerUpdateRequested(true);
        dataSetBatchUpdates.setDetails(updateDetails);
        String sessionToken = logIntoCommonClientService().getSessionID();
        TimeIntervalChecker timeIntervalChecker = toolBox.createTimeIntervalChecker();

        etlService.updateDataSet(sessionToken, dataSetBatchUpdates);

        ContainerDataSet loadedContainerDataSet1 =
                (ContainerDataSet) toolBox.loadDataSet(containerDataSet1.getCode());
        ContainerDataSet loadedContainerDataSet2 =
                (ContainerDataSet) toolBox.loadDataSet(containerDataSet2.getCode());
        AbstractExternalData loadedContainedDataSet = toolBox.loadDataSet(containedDataSet.getCode());
        assertEquals("[]", toolBox.extractCodes(loadedContainerDataSet1.getContainedDataSets())
                .toString());
        assertEquals("[DS-3]", toolBox.extractCodes(loadedContainerDataSet2.getContainedDataSets())
                .toString());
        assertEquals("[DS-2]", extractCodes(loadedContainedDataSet.getContainerDataSets()).toString());
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker,
                loadedContainerDataSet1, "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker,
                loadedContainerDataSet2, "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedContainedDataSet,
                "test");
    }

    @Test
    public void testMoveContainedDataSetToAnotherContainerViaPerformEntityOperation()
    {
        Experiment experiment = toolBox.createAndLoadExperiment(1);
        NewContainerDataSet containerDataSet1 = toolBox.containerDataSet("DS-1", experiment);
        AbstractExternalData containedDataSet = toolBox.createAndLoadDataSet(toolBox.dataSet("DS-3", experiment));
        containerDataSet1.setContainedDataSetCodes(Arrays.asList(containedDataSet.getCode()));
        toolBox.createAndLoadDataSet(containerDataSet1);
        NewContainerDataSet containerDataSet2 = toolBox.containerDataSet("DS-2", experiment);
        toolBox.createAndLoadDataSet(containerDataSet2);
        AbstractExternalData dataSet = toolBox.loadDataSet(containedDataSet.getCode());
        assertEquals("DS-1", dataSet.tryGetContainer().getCode());
        DataSetBatchUpdatesDTO dataSetBatchUpdates = new DataSetBatchUpdatesDTO();
        dataSetBatchUpdates.setVersion(dataSet.getVersion());
        dataSetBatchUpdates.setDatasetCode(dataSet.getCode());
        dataSetBatchUpdates.setDatasetId(new TechId(dataSet));
        dataSetBatchUpdates.setProperties(Arrays.<IEntityProperty> asList());
        dataSetBatchUpdates.setFileFormatTypeCode("XML");
        dataSetBatchUpdates.setModifiedContainerDatasetCodeOrNull(containerDataSet2.getCode());
        DataSetBatchUpdateDetails updateDetails = new DataSetBatchUpdateDetails();
        updateDetails.setContainerUpdateRequested(true);
        dataSetBatchUpdates.setDetails(updateDetails);
        AtomicEntityOperationDetailsBuilder builder =
                new AtomicEntityOperationDetailsBuilder().dataSetUpdate(dataSetBatchUpdates).user(
                        "test");
        TimeIntervalChecker timeIntervalChecker = toolBox.createTimeIntervalChecker();

        etlService.performEntityOperations(systemSessionToken, builder.getDetails());

        ContainerDataSet loadedContainerDataSet1 =
                (ContainerDataSet) toolBox.loadDataSet(containerDataSet1.getCode());
        ContainerDataSet loadedContainerDataSet2 =
                (ContainerDataSet) toolBox.loadDataSet(containerDataSet2.getCode());
        AbstractExternalData loadedContainedDataSet = toolBox.loadDataSet(containedDataSet.getCode());
        assertEquals("[]", toolBox.extractCodes(loadedContainerDataSet1.getContainedDataSets())
                .toString());
        assertEquals("[DS-3]", toolBox.extractCodes(loadedContainerDataSet2.getContainedDataSets())
                .toString());
        assertEquals("[DS-2]", extractCodes(loadedContainedDataSet.getContainerDataSets()).toString());
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker,
                loadedContainerDataSet1, "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker,
                loadedContainerDataSet2, "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedContainedDataSet,
                "test");
    }

    @Test
    public void testReplaceContainedDataSetsViaPerformEntityOperation()
    {
        Experiment experiment = toolBox.createAndLoadExperiment(1);
        NewContainerDataSet containerDataSet = toolBox.containerDataSet("DS-CONT", experiment);
        AbstractExternalData dataSet1 = toolBox.createAndLoadDataSet(toolBox.dataSet("DS-1", experiment));
        containerDataSet.setContainedDataSetCodes(Arrays.asList(dataSet1.getCode()));
        ContainerDataSet loadedContainerDataSet =
                (ContainerDataSet) toolBox.createAndLoadDataSet(containerDataSet);
        assertEquals("[DS-1]", toolBox.extractCodes(loadedContainerDataSet.getContainedDataSets())
                .toString());
        NewDataSet dataSet2 = toolBox.dataSet("DS-2", experiment);
        assertEquals(containerDataSet.getCode(), toolBox.loadDataSet(dataSet1.getCode())
                .tryGetContainer().getCode());
        toolBox.createAndLoadDataSet(dataSet2);
        DataSetBatchUpdatesDTO dataSetBatchUpdates = new DataSetBatchUpdatesDTO();
        dataSetBatchUpdates.setVersion(loadedContainerDataSet.getVersion());
        dataSetBatchUpdates.setDatasetCode(loadedContainerDataSet.getCode());
        dataSetBatchUpdates.setDatasetId(new TechId(loadedContainerDataSet));
        dataSetBatchUpdates.setProperties(Arrays.<IEntityProperty> asList());
        dataSetBatchUpdates.setModifiedContainedDatasetCodesOrNull(new String[]
        { dataSet2.getCode() });
        DataSetBatchUpdateDetails updateDetails = new DataSetBatchUpdateDetails();
        updateDetails.setContainerUpdateRequested(true);
        dataSetBatchUpdates.setDetails(updateDetails);
        AtomicEntityOperationDetailsBuilder builder =
                new AtomicEntityOperationDetailsBuilder().dataSetUpdate(dataSetBatchUpdates).user(
                        "test");
        TimeIntervalChecker timeIntervalChecker = toolBox.createTimeIntervalChecker();

        etlService.performEntityOperations(systemSessionToken, builder.getDetails());

        loadedContainerDataSet = (ContainerDataSet) toolBox.loadDataSet(containerDataSet.getCode());
        AbstractExternalData loadedDataSet1 = toolBox.loadDataSet(dataSet1.getCode());
        AbstractExternalData loadedDataSet2 = toolBox.loadDataSet(dataSet2.getCode());
        assertEquals("[DS-2]", toolBox.extractCodes(loadedContainerDataSet.getContainedDataSets())
                .toString());
        assertEquals(null, loadedDataSet1.tryGetContainer());
        assertEquals("DS-CONT", loadedDataSet2.tryGetContainer().getCode());
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedContainerDataSet,
                "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedDataSet1, "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedDataSet2, "test");
    }

    @Test
    public void testReplaceContainedDataSetsViaUpdateDataSet()
    {
        Experiment experiment = toolBox.createAndLoadExperiment(1);
        NewContainerDataSet containerDataSet = toolBox.containerDataSet("DS-CONT", experiment);
        AbstractExternalData dataSet1 = toolBox.createAndLoadDataSet(toolBox.dataSet("DS-1", experiment));
        containerDataSet.setContainedDataSetCodes(Arrays.asList(dataSet1.getCode()));
        ContainerDataSet loadedContainerDataSet =
                (ContainerDataSet) toolBox.createAndLoadDataSet(containerDataSet);
        assertEquals("[DS-1]", toolBox.extractCodes(loadedContainerDataSet.getContainedDataSets())
                .toString());
        NewDataSet dataSet2 = toolBox.dataSet("DS-2", experiment);
        assertEquals(containerDataSet.getCode(), toolBox.loadDataSet(dataSet1.getCode())
                .tryGetContainer().getCode());
        toolBox.createAndLoadDataSet(dataSet2);
        DataSetBatchUpdatesDTO dataSetBatchUpdates = new DataSetBatchUpdatesDTO();
        dataSetBatchUpdates.setVersion(loadedContainerDataSet.getVersion());
        dataSetBatchUpdates.setDatasetCode(loadedContainerDataSet.getCode());
        dataSetBatchUpdates.setDatasetId(new TechId(loadedContainerDataSet));
        dataSetBatchUpdates.setProperties(Arrays.<IEntityProperty> asList());
        dataSetBatchUpdates.setExperimentIdentifierOrNull(new ExperimentIdentifier(experiment));
        dataSetBatchUpdates.setModifiedContainedDatasetCodesOrNull(new String[]
        { dataSet2.getCode() });
        DataSetBatchUpdateDetails updateDetails = new DataSetBatchUpdateDetails();
        updateDetails.setContainerUpdateRequested(true);
        dataSetBatchUpdates.setDetails(updateDetails);
        String sessionToken = logIntoCommonClientService().getSessionID();
        TimeIntervalChecker timeIntervalChecker = toolBox.createTimeIntervalChecker();

        etlService.updateDataSet(sessionToken, dataSetBatchUpdates);

        loadedContainerDataSet = (ContainerDataSet) toolBox.loadDataSet(containerDataSet.getCode());
        AbstractExternalData loadedDataSet1 = toolBox.loadDataSet(dataSet1.getCode());
        AbstractExternalData loadedDataSet2 = toolBox.loadDataSet(dataSet2.getCode());
        assertEquals("[DS-2]", toolBox.extractCodes(loadedContainerDataSet.getContainedDataSets())
                .toString());
        assertEquals(null, loadedDataSet1.tryGetContainer());
        assertEquals("DS-CONT", loadedDataSet2.tryGetContainer().getCode());
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedContainerDataSet,
                "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedDataSet1, "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedDataSet2, "test");
    }

    private List<String> extractCodes(List<? extends AbstractExternalData> dataSets)
    {
        List<String> result = new ArrayList<String>();
        for (AbstractExternalData dataSet : dataSets)
        {
            result.add(dataSet.getCode());
        }
        Collections.sort(result);
        return result;
    }

}
