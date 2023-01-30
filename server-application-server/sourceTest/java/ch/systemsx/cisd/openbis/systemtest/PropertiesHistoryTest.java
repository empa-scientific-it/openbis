/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.text.StringEscapeUtils;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DataSetUpdates;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListEntityHistoryCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleUpdates;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityHistory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.MaterialBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.PropertyBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.VocabularyTermBuilder;

/**
 * @author Franz-Josef Elmer
 */
public class PropertiesHistoryTest extends SystemTestCase
{

    @Test
    public void testUpdateExperimentProperties()
    {
        TechId id = new TechId(2);
        SessionContext session = logIntoCommonClientService();
        commonClientService.assignPropertyType(new NewETPTAssignment(EntityKind.EXPERIMENT,
                "BACTERIUM", "SIRNA_HCS", false, "BACTERIUM-X", null, 1L, false, false, null, true,
                false));
        Experiment experiment = commonClientService.getExperimentInfo(id);

        ExperimentUpdates updates = new ExperimentUpdates();
        updates.setExperimentId(id);
        updates.setSamplesSessionKey(SESSION_KEY);
        updates.setAttachmentSessionKey("");
        updates.setVersion(experiment.getVersion());
        updates.setProjectIdentifier(experiment.getProject().getIdentifier());
        IEntityProperty p1 = new PropertyBuilder("DESCRIPTION").value("hello world").getProperty();
        IEntityProperty p2 =
                new PropertyBuilder("GENDER").value(new VocabularyTermBuilder("female").getTerm())
                        .getProperty();
        IEntityProperty p3 =
                new PropertyBuilder("BACTERIUM").value(
                        new MaterialBuilder().code("BACTERIUM-Y").type("BACTERIUM").getMaterial())
                        .getProperty();
        updates.setProperties(Arrays.asList(p1, p2, p3));

        genericClientService.updateExperiment(updates);

        List<PropertyHistory> history = getExperimentPropertiesHistory(id.getId());
        assertEquals(
                "[BACTERIUM: material:BACTERIUM-X [BACTERIUM]<a:2>, DESCRIPTION: A simple experiment<a:1>, GENDER: term:MALE [GENDER]<a:1>]",
                history.toString());

        ListEntityHistoryCriteria criteria = new ListEntityHistoryCriteria();
        criteria.setEntityKind(EntityKind.EXPERIMENT);
        criteria.setEntityID(id);
        TypedTableResultSet<EntityHistory> propertyHistory =
                commonClientService.listEntityHistory(criteria);

        List<TableModelRowWithObject<EntityHistory>> sortedHistory =
                getSortedHistory(propertyHistory);

        assertEquals("[BACTERIUM, bacterium, , BACTERIUM-X [BACTERIUM], " + renderUser(session)
                + "]", sortedHistory.get(0).getValues().subList(0, 5).toString());
        assertEquals("[DESCRIPTION, Description, , A simple experiment, System User]",
                sortedHistory.get(1).getValues().subList(0, 5).toString());
        assertEquals("[GENDER, Gender, , MALE [GENDER], System User]", sortedHistory.get(2)
                .getValues().subList(0, 5).toString());
        assertEquals(3, sortedHistory.size());
        assertValidDates(sortedHistory.get(0));
        assertValidDates(sortedHistory.get(1));
        assertValidDates(sortedHistory.get(2));
    }

    @Test
    public void testDeleteExperimentProperties()
    {
        TechId id = new TechId(2);
        logIntoCommonClientService();
        commonClientService.assignPropertyType(new NewETPTAssignment(EntityKind.EXPERIMENT,
                "BACTERIUM", "SIRNA_HCS", false, "BACTERIUM-X", null, 1L, false, false, null, true,
                false));
        Date modificationDate =
                getETPT(EntityKind.EXPERIMENT, "DESCRIPTION", "SIRNA_HCS").getModificationDate();
        commonClientService.updatePropertyTypeAssignment(new NewETPTAssignment(
                EntityKind.EXPERIMENT, "DESCRIPTION", "SIRNA_HCS", false, null, null, 1L, false,
                false, modificationDate, null, true, false));
        Experiment experiment = commonClientService.getExperimentInfo(id);
        assertEquals(3, experiment.getProperties().size());

        ExperimentUpdates updates = new ExperimentUpdates();
        updates.setExperimentId(id);
        updates.setSamplesSessionKey(SESSION_KEY);
        updates.setAttachmentSessionKey("");
        updates.setVersion(experiment.getVersion());
        updates.setProjectIdentifier(experiment.getProject().getIdentifier());
        IEntityProperty p1 = new PropertyBuilder("DESCRIPTION").value((String) null).getProperty();
        IEntityProperty p2 =
                new PropertyBuilder("GENDER").value((VocabularyTerm) null).getProperty();
        IEntityProperty p3 = new PropertyBuilder("BACTERIUM").value((Material) null).getProperty();
        updates.setProperties(Arrays.asList(p1, p2, p3));

        genericClientService.updateExperiment(updates);

        assertEquals(0, commonClientService.getExperimentInfo(id).getProperties().size());
        List<PropertyHistory> history = getExperimentPropertiesHistory(id.getId());
        assertEquals(
                "[BACTERIUM: material:BACTERIUM-X [BACTERIUM]<a:2>, DESCRIPTION: A simple experiment<a:1>, GENDER: term:MALE [GENDER]<a:1>]",
                history.toString());
    }

    @Test
    public void testUpdateSampleProperties()
    {
        TechId id = new TechId(1042);
        SessionContext session = logIntoCommonClientService();
        commonClientService.assignPropertyType(new NewETPTAssignment(EntityKind.SAMPLE, "GENDER",
                "CELL_PLATE", false, "male", null, 1L, false, false, null, true, false));
        Sample sample = genericClientService.getSampleInfo(id);

        SampleUpdates updates = new SampleUpdates();
        updates.setSampleId(id);
        updates.setSessionKey(SESSION_KEY);
        updates.setVersion(sample.getVersion());
        updates.setSampleIdentifier(sample.getIdentifier());
        updates.setExperimentIdentifierOrNull(new ExperimentIdentifier(sample.getExperiment()
                .getIdentifier()));
        IEntityProperty p1 = new PropertyBuilder("COMMENT").value("hello world").getProperty();
        IEntityProperty p2 =
                new PropertyBuilder("GENDER").value(new VocabularyTermBuilder("female").getTerm())
                        .getProperty();
        IEntityProperty p3 =
                new PropertyBuilder("BACTERIUM").value(
                        new MaterialBuilder().code("BACTERIUM-Y").type("BACTERIUM").getMaterial())
                        .getProperty();
        updates.setProperties(Arrays.asList(p1, p2, p3));

        genericClientService.updateSample(updates);

        List<PropertyHistory> history = getSamplePropertiesHistory(id.getId());
        assertEquals(
                "[BACTERIUM: material:BACTERIUM-X [BACTERIUM]<a:1>, COMMENT: very advanced stuff<a:1>, "
                        + "GENDER: term:MALE [GENDER]<a:2>]", history.toString());

        ListEntityHistoryCriteria criteria = new ListEntityHistoryCriteria();
        criteria.setEntityKind(EntityKind.SAMPLE);
        criteria.setEntityID(id);
        TypedTableResultSet<EntityHistory> propertyHistory =
                commonClientService.listEntityHistory(criteria);

        List<TableModelRowWithObject<EntityHistory>> sortedHistory =
                getSortedHistory(propertyHistory);
        assertEquals("[BACTERIUM, bacterium, , BACTERIUM-X [BACTERIUM], System User]",
                sortedHistory.get(0).getValues().subList(0, 5).toString());
        assertEquals("[COMMENT, Comment, , very advanced stuff, System User]", sortedHistory.get(1)
                .getValues().subList(0, 5).toString());
        assertEquals("[GENDER, Gender, , MALE [GENDER], " + renderUser(session) + "]",
                sortedHistory.get(2).getValues().subList(0, 5).toString());
        assertEquals(3, sortedHistory.size());
        assertValidDates(sortedHistory.get(0));
        assertValidDates(sortedHistory.get(1));
        assertValidDates(sortedHistory.get(2));
    }

    @Test
    public void testDeleteSampleProperties()
    {
        TechId id = new TechId(1042);
        logIntoCommonClientService();
        commonClientService.assignPropertyType(new NewETPTAssignment(EntityKind.SAMPLE, "GENDER",
                "CELL_PLATE", false, "male", null, 1L, false, false, null, true, false));
        Sample sample = genericClientService.getSampleInfo(id);
        assertEquals(6, sample.getProperties().size());

        SampleUpdates updates = new SampleUpdates();
        updates.setSampleId(id);
        updates.setSessionKey(SESSION_KEY);
        updates.setVersion(sample.getVersion());
        updates.setSampleIdentifier(sample.getIdentifier());
        updates.setExperimentIdentifierOrNull(new ExperimentIdentifier(sample.getExperiment()
                .getIdentifier()));
        IEntityProperty p1 = new PropertyBuilder("COMMENT").value((String) null).getProperty();
        IEntityProperty p2 =
                new PropertyBuilder("GENDER").value((VocabularyTerm) null).getProperty();
        IEntityProperty p3 = new PropertyBuilder("BACTERIUM").value((Material) null).getProperty();
        updates.setProperties(Arrays.asList(p1, p2, p3));

        genericClientService.updateSample(updates);

        assertEquals(3, genericClientService.getSampleInfo(id).getProperties().size());
        List<PropertyHistory> history = getSamplePropertiesHistory(id.getId());
        assertEquals(
                "[BACTERIUM: material:BACTERIUM-X [BACTERIUM]<a:1>, COMMENT: very advanced stuff<a:1>, GENDER: term:MALE [GENDER]<a:2>]",
                history.toString());
    }

    @Test
    public void testUpdateDataSetProperties()
    {
        TechId id = new TechId(5);
        logIntoCommonClientService();
        AbstractExternalData dataSet = genericClientService.getDataSetInfo(id);

        DataSetUpdates updates = new DataSetUpdates();
        updates.setDatasetId(id);
        updates.setVersion(dataSet.getVersion());
        updates.setExperimentIdentifierOrNull(dataSet.getExperiment().getIdentifier());
        updates.setFileFormatTypeCode(((PhysicalDataSet) dataSet).getFileFormatType().getCode());
        IEntityProperty p1 = new PropertyBuilder("COMMENT").value("hello world").getProperty();
        IEntityProperty p2 =
                new PropertyBuilder("GENDER").value(new VocabularyTermBuilder("male").getTerm())
                        .getProperty();
        IEntityProperty p3 =
                new PropertyBuilder("BACTERIUM").value(
                        new MaterialBuilder().code("BACTERIUM-Y").type("BACTERIUM").getMaterial())
                        .getProperty();
        updates.setProperties(Arrays.asList(p1, p2, p3));

        genericClientService.updateDataSet(updates);

        List<PropertyHistory> history = getDataSetPropertiesHistory(id.getId());
        assertEquals(
                "[BACTERIUM: material:BACTERIUM1 [BACTERIUM]<a:1>, COMMENT: no comment<a:1>, GENDER: term:FEMALE [GENDER]<a:1>]",
                history.toString());

        ListEntityHistoryCriteria criteria = new ListEntityHistoryCriteria();
        criteria.setEntityKind(EntityKind.DATA_SET);
        criteria.setEntityID(id);
        TypedTableResultSet<EntityHistory> propertyHistory =
                commonClientService.listEntityHistory(criteria);

        List<TableModelRowWithObject<EntityHistory>> sortedHistory =
                getSortedHistory(propertyHistory);
        assertEquals("[BACTERIUM, bacterium, , BACTERIUM1 [BACTERIUM], System User]", sortedHistory
                .get(0).getValues().subList(0, 5).toString());
        assertEquals("[COMMENT, Comment, , no comment, System User]", sortedHistory.get(1)
                .getValues().subList(0, 5).toString());
        assertEquals("[GENDER, Gender, , FEMALE [GENDER], System User]", sortedHistory.get(2)
                .getValues().subList(0, 5).toString());
        assertEquals(3, sortedHistory.size());
        assertValidDates(sortedHistory.get(0));
        assertValidDates(sortedHistory.get(1));
        assertValidDates(sortedHistory.get(2));
    }

    @Test
    public void testDeleteDataSetProperties()
    {
        TechId id = new TechId(5);
        logIntoCommonClientService();
        Date modificationDate =
                getETPT(EntityKind.DATA_SET, "COMMENT", "HCS_IMAGE").getModificationDate();
        commonClientService.updatePropertyTypeAssignment(new NewETPTAssignment(EntityKind.DATA_SET,
                "COMMENT", "HCS_IMAGE", false, null, null, 1L, false, false, modificationDate,
                null, true, false));
        AbstractExternalData dataSet = genericClientService.getDataSetInfo(id);
        assertEquals(4, dataSet.getProperties().size());

        DataSetUpdates updates = new DataSetUpdates();
        updates.setDatasetId(id);
        updates.setVersion(dataSet.getVersion());
        updates.setExperimentIdentifierOrNull(dataSet.getExperiment().getIdentifier());
        updates.setFileFormatTypeCode(((PhysicalDataSet) dataSet).getFileFormatType().getCode());
        IEntityProperty p1 = new PropertyBuilder("COMMENT").value((String) null).getProperty();
        IEntityProperty p2 =
                new PropertyBuilder("GENDER").value((VocabularyTerm) null).getProperty();
        IEntityProperty p3 = new PropertyBuilder("BACTERIUM").value((Material) null).getProperty();
        updates.setProperties(Arrays.asList(p1, p2, p3));

        genericClientService.updateDataSet(updates);

        assertEquals(1, genericClientService.getDataSetInfo(id).getProperties().size());
        List<PropertyHistory> history = getDataSetPropertiesHistory(id.getId());
        assertEquals(
                "[BACTERIUM: material:BACTERIUM1 [BACTERIUM]<a:1>, COMMENT: no comment<a:1>, GENDER: term:FEMALE [GENDER]<a:1>]",
                history.toString());
    }

    @Test
    public void testListMaterialPropertiesHistory()
    {
        String sessionToken = logIntoCommonClientService().getSessionID();
        TechId id = new TechId(34);
        commonServer.updateMaterialProperties(sessionToken, id, Arrays.asList(new PropertyUpdates(
                "DESCRIPTION", "new description"), new PropertyUpdates("ORGANISM", "HUMAN")));
        ListEntityHistoryCriteria criteria = new ListEntityHistoryCriteria();
        criteria.setEntityKind(EntityKind.MATERIAL);
        criteria.setEntityID(id);

        TypedTableResultSet<EntityHistory> propertyHistory =
                commonClientService.listEntityHistory(criteria);

        List<TableModelRowWithObject<EntityHistory>> sortedHistory =
                getSortedHistory(propertyHistory);
        assertEquals("[DESCRIPTION, Description, , test bacterium 1, System User]", sortedHistory
                .get(0).getValues().subList(0, 5).toString());
        assertEquals("[ORGANISM, Organism, , FLY [ORGANISM], System User]", sortedHistory.get(1)
                .getValues().subList(0, 5).toString());
        assertEquals(2, sortedHistory.size());
        assertValidDates(sortedHistory.get(0));
        assertValidDates(sortedHistory.get(1));
    }

    private void assertValidDates(TableModelRowWithObject<EntityHistory> row)
    {
        Date validFromDate = row.getObjectOrNull().getValidFromDate();
        Date validUntilDate = row.getObjectOrNull().getValidUntilDate();
        assertTrue("valid from date:" + validFromDate + " valid until date:" + validUntilDate,
                validFromDate.getTime() <= validUntilDate.getTime());
    }

    private List<TableModelRowWithObject<EntityHistory>> getSortedHistory(
            TypedTableResultSet<EntityHistory> propertyHistory)
    {
        GridRowModels<TableModelRowWithObject<EntityHistory>> list =
                propertyHistory.getResultSet().getList();
        List<TableModelRowWithObject<EntityHistory>> sortedHistory =
                new ArrayList<TableModelRowWithObject<EntityHistory>>();
        for (GridRowModel<TableModelRowWithObject<EntityHistory>> gridRowModel : list)
        {
            sortedHistory.add(gridRowModel.getOriginalObject());
        }
        Collections.sort(sortedHistory, new Comparator<TableModelRowWithObject<EntityHistory>>()
            {

                @Override
                public int compare(TableModelRowWithObject<EntityHistory> o1,
                        TableModelRowWithObject<EntityHistory> o2)
                {
                    return o1.getObjectOrNull().getPropertyType().getCode()
                            .compareTo(o2.getObjectOrNull().getPropertyType().getCode());
                }
            });
        return sortedHistory;
    }

    private String renderUser(SessionContext session)
    {
        Person person = session.getUser().getUserPersonObject();
        return StringEscapeUtils.escapeHtml4(person.getLastName() + ", " + person.getFirstName());
    }

}
