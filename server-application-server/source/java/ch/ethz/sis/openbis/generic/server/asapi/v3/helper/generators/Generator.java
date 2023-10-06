/*
 * Copyright ETH 2014 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.generators;

import java.io.FileNotFoundException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.entity.AbstractEntity;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.Attachment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.fetchoptions.AttachmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.Relationship;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.EmptyFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IDataSetsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IEntityType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IEntityTypeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IExperimentsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IIdentifierHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IMaterialsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IOwnerHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IParentChildrenHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IProjectHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IProjectsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertyTypeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ISamplesHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationExecutionError;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationExecutionProgress;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.ArchivingStatus;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.Complete;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.FileFormatType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.LinkedData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.LocatorType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.PhysicalData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.StorageFormat;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.FileFormatTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.LinkedDataFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.LocatorTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.PhysicalDataFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.StorageFormatFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.DataStore;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.fetchoptions.DataStoreFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.fetchoptions.DeletedObjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.fetchoptions.DeletionFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.fetchoptions.EntityTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.EntityType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.EventType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.fetchoptions.EventFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.id.IEventId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.ExternalDms;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.fetchoptions.ExternalDmsFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.fetchoptions.GlobalSearchObjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchObjectKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.HistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.fetchoptions.HistoryEntryFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.MaterialType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.ObjectKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.OperationKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.fetchoptions.ObjectKindModificationFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.IOperationExecutionNotification;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionAvailability;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionDetails;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionState;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionSummary;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.fetchoptions.OperationExecutionDetailsFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.fetchoptions.OperationExecutionFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.fetchoptions.OperationExecutionNotificationFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.fetchoptions.OperationExecutionSummaryFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.id.OperationExecutionPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.fetchoptions.PersonalAccessTokenFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.id.PersonalAccessTokenPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyAssignmentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.QueryType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.fetchoptions.QueryDatabaseFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.fetchoptions.QueryFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.QueryDatabaseName;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.RoleLevel;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.fetchoptions.SemanticAnnotationFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.id.SemanticAnnotationPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.fetchoptions.CustomASServiceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id.CustomASServiceCode;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.fetchoptions.SessionInformationFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.fetchoptions.TagFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.Vocabulary;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyTermFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyTermPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.generators.DtoGenerator.DTOField;

public class Generator extends AbstractGenerator
{

    private static DtoGenerator createSampleGenerator()
    {
        DtoGenerator gen = new DtoGenerator("sample", "Sample", SampleFetchOptions.class);

        gen.addExtendsClass("AbstractEntity<Sample>");
        addPermId(gen, SamplePermId.class);
        gen.addSimpleField(SampleIdentifier.class, "identifier").withInterface(IIdentifierHolder.class);
        addCode(gen);

        gen.addSimpleField(boolean.class, "frozen");
        gen.addSimpleField(boolean.class, "frozenForComponents");
        gen.addSimpleField(boolean.class, "frozenForChildren");
        gen.addSimpleField(boolean.class, "frozenForParents");
        gen.addSimpleField(boolean.class, "frozenForDataSets");

        DTOField fetchOptionField = gen.getFetchOptionsField();
        fetchOptionField.isInherited = true;
        fetchOptionField.withCustomSetter("public void setFetchOptions(SampleFetchOptions fetchOptions)\n" +
                "    {\n" +
                "        super.setFetchOptions(fetchOptions);\n" +
                "    }\n");

        fetchOptionField.withCustomGetter("@JsonIgnore\n" +
                "    @Override\n" +
                "    public SampleFetchOptions getFetchOptions()\n" +
                "    {\n" +
                "        return (SampleFetchOptions) super.getFetchOptions();\n" +
                "    }\n");

        addRegistrationDate(gen);
        addModificationDate(gen);

        gen.addFetchedField(SampleType.class, "type", "Sample type", SampleTypeFetchOptions.class).withInterface(IEntityTypeHolder.class);
        gen.addFetchedField(Project.class, "project", "Project", ProjectFetchOptions.class).withInterface(IProjectHolder.class);
        addSpace(gen);
        addExperiment(gen);
        addProperties(gen);

        gen.addClassForImport(AbstractEntity.class);
        gen.addClassForImport(ISampleId.class);
        gen.addClassForImport(Relationship.class);

        gen.addPluralFetchedField("List<Sample>", List.class.getName(), "parents", "Parents", SampleFetchOptions.class)
                .withInterfaceReflexive(IParentChildrenHolder.class);
        DTOField parentsRelationships =
                gen.addPluralFetchedField("Map<SamplePermId, Relationship>", Map.class.getName(), "parentsRelationships", "Parents",
                        SampleFetchOptions.class);
        parentsRelationships.fetchOptionsFieldName = "parents";
        gen.addAdditionalMethod("@JsonIgnore\n"
                + "    public Relationship getParentRelationship(ISampleId parentId)\n"
                + "    {\n"
                + "        Map<SamplePermId, Relationship> relationships = getParentsRelationships();\n"
                + "        return relationships == null ? new Relationship() : relationships.get(parentId);\n"
                + "    }");

        gen.addPluralFetchedField("List<Sample>", List.class.getName(), "children", "Children", SampleFetchOptions.class)
                .withInterfaceReflexive(IParentChildrenHolder.class);
        DTOField childrenRelationships =
                gen.addPluralFetchedField("Map<SamplePermId, Relationship>", Map.class.getName(), "childrenRelationships", "Children",
                        SampleFetchOptions.class);
        childrenRelationships.fetchOptionsFieldName = "children";
        gen.addAdditionalMethod("@JsonIgnore\n"
                + "    public Relationship getChildRelationship(ISampleId childId)\n"
                + "    {\n"
                + "        Map<SamplePermId, Relationship> relationships = getChildrenRelationships();\n"
                + "        return relationships == null ? new Relationship() : relationships.get(childId);\n"
                + "    }");

        gen.addFetchedField(Sample.class, "container", "Container sample", SampleFetchOptions.class);
        gen.addPluralFetchedField("List<Sample>", List.class.getName(), "components", "Component samples", SampleFetchOptions.class);
        gen.addPluralFetchedField("List<DataSet>", List.class.getName(), "dataSets", "Data sets", DataSetFetchOptions.class)
                .withInterface(IDataSetsHolder.class);
        gen.addClassForImport(DataSet.class);
        gen.addPluralFetchedField("List<HistoryEntry>", List.class.getName(), "history", "History", HistoryEntryFetchOptions.class);
        gen.addPluralFetchedField("List<HistoryEntry>", List.class.getName(), "propertiesHistory", "Properties history",
                HistoryEntryFetchOptions.class);
        gen.addPluralFetchedField("List<HistoryEntry>", List.class.getName(), "spaceHistory", "Space history",
                HistoryEntryFetchOptions.class);
        gen.addPluralFetchedField("List<HistoryEntry>", List.class.getName(), "projectHistory", "Project history",
                HistoryEntryFetchOptions.class);
        gen.addPluralFetchedField("List<HistoryEntry>", List.class.getName(), "experimentHistory", "Experiment history",
                HistoryEntryFetchOptions.class);
        gen.addPluralFetchedField("List<HistoryEntry>", List.class.getName(), "parentsHistory", "Parents history",
                HistoryEntryFetchOptions.class);
        gen.addPluralFetchedField("List<HistoryEntry>", List.class.getName(), "childrenHistory", "Children history",
                HistoryEntryFetchOptions.class);
        gen.addPluralFetchedField("List<HistoryEntry>", List.class.getName(), "containerHistory", "Container history",
                HistoryEntryFetchOptions.class);
        gen.addPluralFetchedField("List<HistoryEntry>", List.class.getName(), "componentsHistory", "Components history",
                HistoryEntryFetchOptions.class);
        gen.addPluralFetchedField("List<HistoryEntry>", List.class.getName(), "dataSetsHistory", "Data sets history",
                HistoryEntryFetchOptions.class);
        gen.addPluralFetchedField("List<HistoryEntry>", List.class.getName(), "unknownHistory", "Unknown history",
                HistoryEntryFetchOptions.class);
        gen.addClassForImport(HistoryEntry.class);

        addTags(gen);
        addRegistrator(gen);
        addModifier(gen);
        addAttachments(gen);
        addMetaData(gen);

        gen.setToStringMethod("\"Sample \" + permId");

        return gen;
    }

    private static DtoGenerator createSampleTypeGenerator()
    {
        // type.getContainerHierarchyDepth();
        // type.getGeneratedFromHierarchyDepth();
        // the two fields are ignored as they are hints for old api translations

        // potentially missing fields:

        // type.getValidationScript();

        DtoGenerator gen = new DtoGenerator("sample", "SampleType", SampleTypeFetchOptions.class);
        gen.addImplementedInterface(IEntityType.class);

        addPermId(gen, EntityTypePermId.class);
        addCode(gen);
        addDescription(gen);
        gen.addBooleanField("listable");
        gen.addBooleanField("subcodeUnique");
        gen.addBooleanField("autoGeneratedCode");
        gen.addBooleanField("showContainer");
        gen.addBooleanField("showParents");
        gen.addBooleanField("showParentMetadata");

        gen.addStringField("generatedCodePrefix");
        addModificationDate(gen);
        addMetaData(gen);

        gen.setToStringMethod("\"SampleType \" + code");
        addPropertyAssignments(gen);
        addSemanticAnnotations(gen);

        return gen;
    }

    private static DtoGenerator createAttachmentGenerator()
    {
        DtoGenerator gen = new DtoGenerator("attachment", "Attachment", AttachmentFetchOptions.class);

        gen.addStringField("fileName");
        gen.addStringField("title");
        addDescription(gen);
        gen.addStringField("permlink");
        gen.addStringField("latestVersionPermlink");
        gen.addSimpleField(Integer.class, "version");
        addRegistrationDate(gen);

        addRegistrator(gen);
        gen.addFetchedField(Attachment.class, "previousVersion", "Previous version of attachment ", AttachmentFetchOptions.class);

        gen.addFetchedField(byte[].class, "content", "Content", EmptyFetchOptions.class);

        gen.setToStringMethod("\"Attachment \" + fileName + \":\" + version");

        return gen;
    }

    private static DtoGenerator createExperimentGenerator()
    {
        DtoGenerator gen = new DtoGenerator("experiment", "Experiment", ExperimentFetchOptions.class);
        gen.addExtendsClass("AbstractEntity<Experiment>");

        addPermId(gen, ExperimentPermId.class);
        gen.addSimpleField(ExperimentIdentifier.class, "identifier").withInterface(IIdentifierHolder.class);
        addCode(gen);

        DTOField fetchOptionField = gen.getFetchOptionsField();
        fetchOptionField.isInherited = true;
        fetchOptionField.withCustomSetter("public void setFetchOptions(ExperimentFetchOptions fetchOptions)\n" +
                "    {\n" +
                "        super.setFetchOptions(fetchOptions);\n" +
                "    }\n");

        fetchOptionField.withCustomGetter("@JsonIgnore\n" +
                "    @Override\n" +
                "    public ExperimentFetchOptions getFetchOptions()\n" +
                "    {\n" +
                "        return (ExperimentFetchOptions) super.getFetchOptions();\n" +
                "    }\n");
        gen.addClassForImport(AbstractEntity.class);

        gen.addSimpleField(boolean.class, "frozen");
        gen.addSimpleField(boolean.class, "frozenForDataSets");
        gen.addSimpleField(boolean.class, "frozenForSamples");

        addRegistrationDate(gen);
        addModificationDate(gen);

        gen.addFetchedField(ExperimentType.class, "type", "Experiment type", ExperimentTypeFetchOptions.class).withInterface(IEntityTypeHolder.class);
        gen.addFetchedField(Project.class, "project", "Project", ProjectFetchOptions.class).withInterface(IProjectHolder.class);

        gen.addPluralFetchedField("List<DataSet>", List.class.getName(), "dataSets", "Data sets", DataSetFetchOptions.class)
                .withInterface(IDataSetsHolder.class);
        gen.addClassForImport(DataSet.class);

        gen.addPluralFetchedField("List<Sample>", List.class.getName(), "samples", "Samples", SampleFetchOptions.class)
                .withInterface(ISamplesHolder.class);
        gen.addClassForImport(Sample.class);

        gen.addPluralFetchedField("List<HistoryEntry>", List.class.getName(), "history", "History", HistoryEntryFetchOptions.class);
        gen.addPluralFetchedField("List<HistoryEntry>", List.class.getName(), "propertiesHistory", "Properties history",
                HistoryEntryFetchOptions.class);
        gen.addPluralFetchedField("List<HistoryEntry>", List.class.getName(), "projectHistory", "Project history",
                HistoryEntryFetchOptions.class);
        gen.addPluralFetchedField("List<HistoryEntry>", List.class.getName(), "samplesHistory", "Samples history",
                HistoryEntryFetchOptions.class);
        gen.addPluralFetchedField("List<HistoryEntry>", List.class.getName(), "dataSetsHistory", "Data sets history",
                HistoryEntryFetchOptions.class);
        gen.addPluralFetchedField("List<HistoryEntry>", List.class.getName(), "unknownHistory", "Unknown history",
                HistoryEntryFetchOptions.class);
        gen.addClassForImport(HistoryEntry.class);

        addProperties(gen);
        addTags(gen);
        addRegistrator(gen);
        addModifier(gen);
        addAttachments(gen);
        addMetaData(gen);

        gen.setToStringMethod("\"Experiment \" + permId");

        return gen;
    }

    private static DtoGenerator createExperimentTypeGenerator()
    {
        DtoGenerator gen = new DtoGenerator("experiment", "ExperimentType", ExperimentTypeFetchOptions.class);
        gen.addImplementedInterface(IEntityType.class);

        addPermId(gen, EntityTypePermId.class);
        addCode(gen);
        addDescription(gen);
        addModificationDate(gen);
        addMetaData(gen);

        gen.setToStringMethod("\"ExperimentType \" + code");
        addPropertyAssignments(gen);

        // TODO add validation script
        return gen;
    }

    private static DtoGenerator createDataSetGenerator()
    {
        DtoGenerator gen = new DtoGenerator("dataset", "DataSet", DataSetFetchOptions.class);

        gen.addExtendsClass("AbstractEntity<DataSet>");
        addPermId(gen, DataSetPermId.class);
        addCode(gen);

        DTOField fetchOptionField = gen.getFetchOptionsField();
        fetchOptionField.isInherited = true;
        fetchOptionField.withCustomSetter("public void setFetchOptions(DataSetFetchOptions fetchOptions)\n" +
                "    {\n" +
                "        super.setFetchOptions(fetchOptions);\n" +
                "    }\n");

        fetchOptionField.withCustomGetter("@JsonIgnore\n" +
                "    @Override\n" +
                "    public DataSetFetchOptions getFetchOptions()\n" +
                "    {\n" +
                "        return (DataSetFetchOptions) super.getFetchOptions();\n" +
                "    }\n");
        gen.addClassForImport(AbstractEntity.class);

        gen.addSimpleField(boolean.class, "frozen");
        gen.addSimpleField(boolean.class, "frozenForChildren");
        gen.addSimpleField(boolean.class, "frozenForParents");
        gen.addSimpleField(boolean.class, "frozenForComponents");
        gen.addSimpleField(boolean.class, "frozenForContainers");

        gen.addFetchedField(DataSetType.class, "type", "Data Set type", DataSetTypeFetchOptions.class).withInterface(IEntityTypeHolder.class);
        gen.addSimpleField(DataSetKind.class, "kind");

        gen.addFetchedField(DataStore.class, "dataStore", "Data store", DataStoreFetchOptions.class);
        gen.addBooleanField("measured");
        gen.addBooleanField("postRegistered");
        gen.addFetchedField(PhysicalData.class, "physicalData", "Physical data", PhysicalDataFetchOptions.class);
        gen.addFetchedField(LinkedData.class, "linkedData", "Linked data", LinkedDataFetchOptions.class);

        addExperiment(gen);
        addSample(gen);
        addProperties(gen);
        addMetaData(gen);

        gen.addPluralFetchedField("List<DataSet>", List.class.getName(), "parents", "Parents", DataSetFetchOptions.class)
                .withInterfaceReflexive(IParentChildrenHolder.class);
        gen.addPluralFetchedField("List<DataSet>", List.class.getName(), "children", "Children", DataSetFetchOptions.class)
                .withInterfaceReflexive(IParentChildrenHolder.class);
        gen.addPluralFetchedField("List<DataSet>", List.class.getName(), "containers", "Container data sets", DataSetFetchOptions.class);
        gen.addPluralFetchedField("List<DataSet>", List.class.getName(), "components", "Component data sets", DataSetFetchOptions.class);
        addTags(gen);

        gen.addPluralFetchedField("List<HistoryEntry>", List.class.getName(), "history", "History", HistoryEntryFetchOptions.class);
        gen.addPluralFetchedField("List<HistoryEntry>", List.class.getName(), "propertiesHistory", "Properties history",
                HistoryEntryFetchOptions.class);
        gen.addPluralFetchedField("List<HistoryEntry>", List.class.getName(), "experimentHistory", "Experiment history",
                HistoryEntryFetchOptions.class);
        gen.addPluralFetchedField("List<HistoryEntry>", List.class.getName(), "sampleHistory", "Sample history", HistoryEntryFetchOptions.class);
        gen.addPluralFetchedField("List<HistoryEntry>", List.class.getName(), "parentsHistory", "Parents history", HistoryEntryFetchOptions.class);
        gen.addPluralFetchedField("List<HistoryEntry>", List.class.getName(), "childrenHistory", "Children history", HistoryEntryFetchOptions.class);
        gen.addPluralFetchedField("List<HistoryEntry>", List.class.getName(), "containersHistory", "Containers history",
                HistoryEntryFetchOptions.class);
        gen.addPluralFetchedField("List<HistoryEntry>", List.class.getName(), "componentsHistory", "Components history",
                HistoryEntryFetchOptions.class);
        gen.addPluralFetchedField("List<HistoryEntry>", List.class.getName(), "contentCopiesHistory", "Content copies history",
                HistoryEntryFetchOptions.class);
        gen.addPluralFetchedField("List<HistoryEntry>", List.class.getName(), "unknownHistory", "Unknown history",
                HistoryEntryFetchOptions.class);

        gen.addClassForImport(HistoryEntry.class);

        // add data set type
        // add data store
        addModificationDate(gen);
        addModifier(gen);
        addRegistrationDate(gen);
        addRegistrator(gen);

        gen.addSimpleField(String.class, "dataProducer");
        gen.addDateField("dataProductionDate");

        gen.addDateField("accessDate");

        gen.setToStringMethod("\"DataSet \" + code");

        return gen;
    }

    private static DtoGenerator createDataSetTypeGenerator()
    {
        DtoGenerator gen = new DtoGenerator("dataset", "DataSetType", DataSetTypeFetchOptions.class);
        gen.addImplementedInterface(IEntityType.class);

        addPermId(gen, EntityTypePermId.class);
        addCode(gen);
        addDescription(gen);
        gen.addStringField("mainDataSetPattern");
        gen.addStringField("mainDataSetPath");
        gen.addBooleanField("disallowDeletion");
        addModificationDate(gen);
        addMetaData(gen);

        gen.setToStringMethod("\"DataSetType \" + code");
        addPropertyAssignments(gen);

        // TODO add validation script
        return gen;
    }

    private static DtoGenerator createPhysicalDataGenerator()
    {
        DtoGenerator gen = new DtoGenerator("dataset", "PhysicalData", PhysicalDataFetchOptions.class);

        gen.addStringField("shareId");
        gen.addStringField("location");
        gen.addSimpleField(Long.class, "size");
        gen.addFetchedField(StorageFormat.class, "storageFormat", "Storage format", StorageFormatFetchOptions.class);
        gen.addFetchedField(FileFormatType.class, "fileFormatType", "File Format Type", FileFormatTypeFetchOptions.class).deprecated();
        gen.addFetchedField(LocatorType.class, "locatorType", "Locator Type", LocatorTypeFetchOptions.class);
        gen.addSimpleField(Complete.class, "complete");
        gen.addSimpleField(ArchivingStatus.class, "status");
        gen.addBooleanField("presentInArchive");
        gen.addBooleanField("storageConfirmation");
        gen.addSimpleField(Integer.class, "speedHint");
        gen.addBooleanField("archivingRequested");

        gen.setToStringMethod("\"PhysicalData \" + location");

        return gen;
    }

    private static DtoGenerator createLinkedDataGenerator()
    {
        DtoGenerator gen = new DtoGenerator("dataset", "LinkedData", LinkedDataFetchOptions.class);

        gen.addStringField("externalCode");
        gen.addFetchedField(ExternalDms.class, "externalDms", "External data management system", ExternalDmsFetchOptions.class);

        gen.setToStringMethod("\"LinkedData \" + externalCode");

        return gen;
    }

    private static DtoGenerator createFileFormatType()
    {
        DtoGenerator gen = new DtoGenerator("dataset", "FileFormatType", FileFormatTypeFetchOptions.class).deprecated();
        addCode(gen);
        gen.addStringField("description");

        gen.setToStringMethod("\"FileFormatType \" + code");

        return gen;
    }

    private static DtoGenerator createStorageFormat()
    {
        DtoGenerator gen = new DtoGenerator("dataset", "StorageFormat", StorageFormatFetchOptions.class);

        addCode(gen);
        gen.addStringField("description");

        gen.setToStringMethod("\"StorageFormat \" + code");

        return gen;
    }

    private static DtoGenerator createLocatorType()
    {
        DtoGenerator gen = new DtoGenerator("dataset", "LocatorType", LocatorTypeFetchOptions.class);

        addCode(gen);
        gen.addStringField("description");

        gen.setToStringMethod("\"LocatorType \" + code");

        return gen;
    }

    private static DtoGenerator createDeletion()
    {
        DtoGenerator gen = new DtoGenerator("deletion", "Deletion", DeletionFetchOptions.class);
        gen.addSimpleField(IDeletionId.class, "id");
        gen.addStringField("reason");
        gen.addPluralFetchedField("List<DeletedObject>", List.class.getName(), "deletedObjects", "Deleted objects", DeletedObjectFetchOptions.class);
        gen.addSimpleField(Date.class, "deletionDate");

        gen.setToStringMethod("\"Deletion \" + id");

        return gen;
    }

    private static DtoGenerator createEvent()
    {
        DtoGenerator gen = new DtoGenerator("event", "Event", EventFetchOptions.class);
        gen.addSimpleField(IEventId.class, "id");
        gen.addSimpleField(EventType.class, "eventType");
        gen.addSimpleField(EntityType.class, "entityType");
        gen.addStringField("entitySpace");
        gen.addSimpleField(ISpaceId.class, "entitySpaceId");
        gen.addStringField("entityProject");
        gen.addSimpleField(IProjectId.class, "entityProjectId");
        gen.addStringField("entityRegistrator");
        gen.addSimpleField(Date.class, "entityRegistrationDate");
        gen.addStringField("identifier");
        gen.addStringField("description");
        gen.addStringField("reason");
        gen.addStringField("content");
        addRegistrator(gen);
        addRegistrationDate(gen);

        gen.setToStringMethod("\"Event \" + id");

        return gen;
    }

    private static DtoGenerator createVocabulary()
    {
        DtoGenerator gen = new DtoGenerator("vocabulary", "Vocabulary", VocabularyFetchOptions.class);

        addCode(gen);
        addDescription(gen);
        addRegistrationDate(gen);
        addRegistrator(gen);
        addModificationDate(gen);

        gen.setToStringMethod("\"Vocabulary \" + code");

        return gen;
    }

    private static DtoGenerator createVocabularyTerm()
    {
        DtoGenerator gen = new DtoGenerator("vocabulary", "VocabularyTerm", VocabularyTermFetchOptions.class);

        addPermId(gen, VocabularyTermPermId.class);
        addCode(gen);
        gen.addStringField("label");
        addDescription(gen);
        gen.addSimpleField(Long.class, "ordinal");
        gen.addBooleanField("official");
        gen.addFetchedField(Vocabulary.class, "vocabulary", "Vocabulary", VocabularyFetchOptions.class);
        addRegistrationDate(gen);
        addRegistrator(gen);
        addModificationDate(gen);

        gen.setToStringMethod("\"VocabularyTerm \" + code");

        return gen;
    }

    private static DtoGenerator createPersonGenerator()
    {
        DtoGenerator gen = new DtoGenerator("person", "Person", PersonFetchOptions.class);

        addPermId(gen, PersonPermId.class);
        gen.addStringField("userId");
        gen.addStringField("firstName");
        gen.addStringField("lastName");
        gen.addStringField("email");
        addRegistrationDate(gen);
        gen.addBooleanField("active");

        addSpace(gen);
        addRegistrator(gen);

        gen.setToStringMethod("\"Person \" + userId");

        return gen;
    }

    private static DtoGenerator createProjectGenerator()
    {
        DtoGenerator gen = new DtoGenerator("project", "Project", ProjectFetchOptions.class);

        addPermId(gen, ProjectPermId.class);
        gen.addSimpleField(ProjectIdentifier.class, "identifier").withInterface(IIdentifierHolder.class);
        addCode(gen);
        addDescription(gen);

        gen.addSimpleField(boolean.class, "frozen");
        gen.addSimpleField(boolean.class, "frozenForExperiments");
        gen.addSimpleField(boolean.class, "frozenForSamples");

        addRegistrationDate(gen);
        addModificationDate(gen);

        gen.addPluralFetchedField("List<Experiment>", List.class.getName(), "experiments", "Experiments", ExperimentFetchOptions.class)
                .withInterface(IExperimentsHolder.class);
        gen.addClassForImport(Experiment.class);
        gen.addPluralFetchedField("List<Sample>", List.class.getName(), "samples", "Samples", SampleFetchOptions.class);
        gen.addClassForImport(Sample.class);
        gen.addPluralFetchedField("List<HistoryEntry>", List.class.getName(), "history", "History", HistoryEntryFetchOptions.class);
        gen.addPluralFetchedField("List<HistoryEntry>", List.class.getName(), "spaceHistory", "Space history", HistoryEntryFetchOptions.class);
        gen.addPluralFetchedField("List<HistoryEntry>", List.class.getName(), "experimentsHistory", "Experiments history",
                HistoryEntryFetchOptions.class);
        gen.addPluralFetchedField("List<HistoryEntry>", List.class.getName(), "samplesHistory", "Samples history", HistoryEntryFetchOptions.class);
        gen.addPluralFetchedField("List<HistoryEntry>", List.class.getName(), "unknownHistory", "Unknown history", HistoryEntryFetchOptions.class);
        gen.addClassForImport(HistoryEntry.class);

        addSpace(gen);
        addRegistrator(gen);
        addModifier(gen);

        gen.addFetchedField(Person.class, "leader", "Leader", PersonFetchOptions.class);
        addAttachments(gen);

        gen.setToStringMethod("\"Project \" + permId");

        return gen;
    }

    private static DtoGenerator createSpaceGenerator()
    {
        DtoGenerator gen = new DtoGenerator("space", "Space", SpaceFetchOptions.class);

        addPermId(gen, SpacePermId.class);
        addCode(gen);
        addDescription(gen);
        addRegistrationDate(gen);
        addModificationDate(gen);
        addRegistrator(gen);
        gen.addPluralFetchedField("List<Sample>", List.class.getName(), "samples", "Samples", SampleFetchOptions.class)
                .withInterface(ISamplesHolder.class);
        gen.addClassForImport(Sample.class);
        gen.addPluralFetchedField("List<Project>", List.class.getName(), "projects", "Projects", ProjectFetchOptions.class)
                .withInterface(IProjectsHolder.class);
        gen.addClassForImport(Project.class);

        gen.setToStringMethod("\"Space \" + permId");

        return gen;
    }

    private static DtoGenerator createTagGenerator()
    {
        DtoGenerator gen = new DtoGenerator("tag", "Tag", TagFetchOptions.class);

        addPermId(gen, TagPermId.class);
        addCode(gen);
        addDescription(gen);
        gen.addSimpleField(Boolean.class, "private", "isPrivate");

        gen.addPluralFetchedField("List<Experiment>", List.class.getName(), "experiments", "Experiments", ExperimentFetchOptions.class)
                .withInterface(IExperimentsHolder.class);
        gen.addClassForImport(Experiment.class);

        gen.addPluralFetchedField("List<Sample>", List.class.getName(), "samples", "Samples", SampleFetchOptions.class)
                .withInterface(ISamplesHolder.class);
        gen.addClassForImport(Sample.class);

        gen.addPluralFetchedField("List<DataSet>", List.class.getName(), "dataSets", "Data sets", DataSetFetchOptions.class)
                .withInterface(IDataSetsHolder.class);
        gen.addClassForImport(DataSet.class);

        gen.addPluralFetchedField("List<Material>", List.class.getName(), "materials", "Materials", MaterialFetchOptions.class)
                .withInterface(IMaterialsHolder.class);
        gen.addClassForImport(Material.class);

        addRegistrationDate(gen);
        gen.addFetchedField(Person.class, "owner", "Owner", PersonFetchOptions.class).withInterface(IOwnerHolder.class);

        gen.setToStringMethod("\"Tag \" + code");

        return gen;
    }

    private static DtoGenerator createMaterialGenerator()
    {
        DtoGenerator gen = new DtoGenerator("material", "Material", MaterialFetchOptions.class);

        addPermId(gen, MaterialPermId.class);
        addCode(gen);
        gen.addFetchedField(MaterialType.class, "type", "Material type", MaterialTypeFetchOptions.class).withInterface(IEntityTypeHolder.class);

        gen.addPluralFetchedField("List<HistoryEntry>", List.class.getName(), "history", "History", HistoryEntryFetchOptions.class);
        gen.addClassForImport(HistoryEntry.class);

        addRegistrationDate(gen);
        addRegistrator(gen);
        addModificationDate(gen);
        addProperties(gen);
        addTags(gen);

        gen.setToStringMethod("\"Material \" + code");

        return gen;
    }

    private static DtoGenerator createMaterialTypeGenerator()
    {
        DtoGenerator gen = new DtoGenerator("material", "MaterialType", MaterialTypeFetchOptions.class);
        gen.addImplementedInterface(IEntityType.class);

        addPermId(gen, EntityTypePermId.class);
        addCode(gen);
        addDescription(gen);
        addModificationDate(gen);

        gen.setToStringMethod("\"MaterialType \" + code");
        addPropertyAssignments(gen);

        return gen;
    }

    private static DtoGenerator createHistoryEntryGenerator()
    {
        DtoGenerator gen = new DtoGenerator("history", "HistoryEntry", HistoryEntryFetchOptions.class);
        gen.addSimpleField(Date.class, "validFrom");
        gen.addSimpleField(Date.class, "validTo");
        gen.addFetchedField(Person.class, "author", "Author", PersonFetchOptions.class);

        gen.setToStringMethod("\"HistoryEntry from: \" + validFrom + \", to: \" + validTo");

        return gen;
    }

    private static DtoGenerator createDataStoreGenerator()
    {
        DtoGenerator gen = new DtoGenerator("datastore", "DataStore", DataStoreFetchOptions.class);
        addCode(gen);
        gen.addStringField("downloadUrl");
        gen.addStringField("remoteUrl");
        addRegistrationDate(gen);
        addModificationDate(gen);

        gen.setToStringMethod("\"DataStore code: \" + code");

        return gen;
    }

    private static DtoGenerator createExternalDmsGenerator()
    {
        DtoGenerator gen = new DtoGenerator("externaldms", "ExternalDms", ExternalDmsFetchOptions.class);
        addCode(gen);
        gen.addStringField("label");
        gen.addStringField("urlTemplate");
        gen.addBooleanField("openbis");

        gen.setToStringMethod("\"ExternalDms code: \" + code");

        return gen;
    }

    private static DtoGenerator createCustomASServiceGenerator()
    {
        DtoGenerator gen = new DtoGenerator("service", "CustomASService", CustomASServiceFetchOptions.class);
        gen.addSimpleField(CustomASServiceCode.class, "code");
        gen.addStringField("label");
        gen.addStringField("description");

        gen.setToStringMethod("\"CustomASService code: \" + code");

        return gen;
    }

    private static DtoGenerator createObjectKindModificationGenerator()
    {
        DtoGenerator gen = new DtoGenerator("objectkindmodification", "ObjectKindModification", ObjectKindModificationFetchOptions.class);
        gen.addSimpleField(ObjectKind.class, "objectKind");
        gen.addSimpleField(OperationKind.class, "operationKind");
        gen.addDateField("lastModificationTimeStamp");

        gen.setToStringMethod("\"Last \" + operationKind + \" operation of an object of kind \" + objectKind "
                + "+ \" occured at \" +  lastModificationTimeStamp");

        return gen;
    }

    private static DtoGenerator createGlobalSearchObject()
    {
        DtoGenerator gen = new DtoGenerator("global", "GlobalSearchObject", GlobalSearchObjectFetchOptions.class);

        gen.addSimpleField(GlobalSearchObjectKind.class, "objectKind");
        gen.addSimpleField(IObjectId.class, "objectPermId");
        gen.addSimpleField(IObjectId.class, "objectIdentifier");
        gen.addStringField("match");
        gen.addSimpleField(double.class, "score");

        gen.addFetchedField(Experiment.class, "experiment", "Experiment", ExperimentFetchOptions.class);
        gen.addFetchedField(Sample.class, "sample", "Sample", SampleFetchOptions.class);
        gen.addFetchedField(DataSet.class, "dataSet", "Data Set", DataSetFetchOptions.class);
        gen.addFetchedField(Material.class, "material", "Material", MaterialFetchOptions.class);
        // gen.addFetchedField(Project.class, "project", "Project", ProjectFetchOptions.class);
        // gen.addFetchedField(Space.class, "space", "Space", SpaceFetchOptions.class);

        gen.setToStringMethod("\"GlobalSearchObject kind: \" + objectKind + \", permId: \" + objectPermId + \", identifier: \" + objectIdentifier");

        return gen;
    }

    private static DtoGenerator createPropertyAssignmentGenerator()
    {
        DtoGenerator gen = new DtoGenerator("property", "PropertyAssignment", PropertyAssignmentFetchOptions.class);

        addPermId(gen, PropertyAssignmentPermId.class);
        gen.addStringField("section");
        gen.addSimpleField(Integer.class, "ordinal");
        gen.addFetchedField(IEntityType.class, "entityType", "Entity type", EntityTypeFetchOptions.class);
        gen.addFetchedField(PropertyType.class, "propertyType", "Property type", PropertyTypeFetchOptions.class)
                .withInterface(IPropertyTypeHolder.class);
        gen.addBooleanField("mandatory");
        gen.addBooleanField("showInEditView");
        gen.addBooleanField("showRawValueInForms");
        addSemanticAnnotations(gen);
        DTOField inherited = gen.addFetchedField(Boolean.class, "semanticAnnotationsInherited", "Semantic annotations",
                SemanticAnnotationFetchOptions.class, "semanticAnnotations");
        inherited.plural = true;
        addRegistrator(gen);
        addRegistrationDate(gen);
        gen.setToStringMethod(
                "\"PropertyAssignment entity type: \" + (entityType != null ? entityType.getCode() : null) + \", property type: \" + (propertyType != null ? propertyType.getCode() : null) + \", mandatory: \" + mandatory");

        return gen;
    }

    private static DtoGenerator createPropertyTypeGenerator()
    {
        DtoGenerator gen = new DtoGenerator("property", "PropertyType", PropertyTypeFetchOptions.class);

        addCode(gen);
        addPermId(gen, PropertyTypePermId.class);
        gen.addStringField("label");
        addDescription(gen);
        gen.addBooleanField("managedInternally");
        gen.addSimpleField(DataType.class, "dataType");
        gen.addFetchedField(Vocabulary.class, "vocabulary", "Vocabulary", VocabularyFetchOptions.class);
        gen.addFetchedField(MaterialType.class, "materialType", "Material type", MaterialTypeFetchOptions.class);
        gen.addStringField("schema");
        gen.addStringField("transformation");
        addSemanticAnnotations(gen);
        addRegistrator(gen);
        addRegistrationDate(gen);
        gen.setToStringMethod("\"PropertyType \" + code");

        return gen;
    }

    private static DtoGenerator createOperationExecution()
    {
        DtoGenerator gen = new DtoGenerator("operation", "OperationExecution", OperationExecutionFetchOptions.class);

        addPermId(gen, OperationExecutionPermId.class);
        addCode(gen);
        gen.addSimpleField(OperationExecutionState.class, "state");
        gen.addFetchedField(Person.class, "owner", "Owner", PersonFetchOptions.class);
        addDescription(gen);
        gen.addFetchedField(IOperationExecutionNotification.class, "notification", "Notification", OperationExecutionNotificationFetchOptions.class);
        gen.addSimpleField(OperationExecutionAvailability.class, "availability");
        gen.addSimpleField(Integer.class, "availabilityTime");

        gen.addFetchedField(OperationExecutionSummary.class, "summary", "Summary", OperationExecutionSummaryFetchOptions.class);
        gen.addSimpleField(OperationExecutionAvailability.class, "summaryAvailability");
        gen.addSimpleField(Integer.class, "summaryAvailabilityTime");

        gen.addFetchedField(OperationExecutionDetails.class, "details", "Details", OperationExecutionDetailsFetchOptions.class);
        gen.addSimpleField(OperationExecutionAvailability.class, "detailsAvailability");
        gen.addSimpleField(Integer.class, "detailsAvailabilityTime");

        gen.addSimpleField(Date.class, "creationDate");
        gen.addSimpleField(Date.class, "startDate");
        gen.addSimpleField(Date.class, "finishDate");

        gen.setToStringMethod("\"OperationExecution code: \" + code");

        return gen;
    }

    private static DtoGenerator createOperationExecutionSummary()
    {
        DtoGenerator gen =
                new DtoGenerator("operation", "OperationExecutionSummary", OperationExecutionSummaryFetchOptions.class);

        gen.addPluralFetchedField("List<String>", List.class.getName(), "operations", "Operations", EmptyFetchOptions.class);
        gen.addFetchedField(String.class, "progress", "Progress", EmptyFetchOptions.class);
        gen.addFetchedField(String.class, "error", "Error", EmptyFetchOptions.class);
        gen.addPluralFetchedField("List<String>", List.class.getName(), "results", "Results", EmptyFetchOptions.class);
        gen.setToStringMethod("\"OperationExecutionSummary\"");

        return gen;
    }

    private static DtoGenerator createOperationExecutionDetails()
    {
        DtoGenerator gen =
                new DtoGenerator("operation", "OperationExecutionDetails", OperationExecutionDetailsFetchOptions.class);

        gen.addClassForImport(IOperation.class);
        gen.addClassForImport(IOperationResult.class);

        gen.addPluralFetchedField("List<? extends IOperation>", List.class.getName(), "operations", "Operations", EmptyFetchOptions.class);
        gen.addFetchedField(IOperationExecutionProgress.class, "progress", "Progress", EmptyFetchOptions.class);
        gen.addFetchedField(IOperationExecutionError.class, "error", "Error", EmptyFetchOptions.class);
        gen.addPluralFetchedField("List<? extends IOperationResult>", List.class.getName(), "results", "Results", EmptyFetchOptions.class);
        gen.setToStringMethod("\"OperationExecutionDetails\"");

        return gen;
    }

    private static DtoGenerator createSemanticAnnotation()
    {
        DtoGenerator gen =
                new DtoGenerator("semanticannotation", "SemanticAnnotation", SemanticAnnotationFetchOptions.class);

        gen.addFetchedField(IEntityType.class, "entityType", "Entity type", EntityTypeFetchOptions.class);
        gen.addFetchedField(PropertyType.class, "propertyType", "Property type", PropertyTypeFetchOptions.class);
        gen.addFetchedField(PropertyAssignment.class, "propertyAssignment", "Property assignment", PropertyAssignmentFetchOptions.class);
        addPermId(gen, SemanticAnnotationPermId.class);
        gen.addSimpleField(String.class, "predicateOntologyId");
        gen.addSimpleField(String.class, "predicateOntologyVersion");
        gen.addSimpleField(String.class, "predicateAccessionId");
        gen.addSimpleField(String.class, "descriptorOntologyId");
        gen.addSimpleField(String.class, "descriptorOntologyVersion");
        gen.addSimpleField(String.class, "descriptorAccessionId");
        gen.addSimpleField(Date.class, "creationDate");
        gen.setToStringMethod("\"SemanticAnnotation \" + permId");

        return gen;
    }

    private static DtoGenerator createQuery()
    {
        DtoGenerator gen = new DtoGenerator("query", "Query", QueryFetchOptions.class);

        gen.addSimpleField(String.class, "name");
        addDescription(gen);
        gen.addSimpleField(String.class, "database");
        gen.addSimpleField(QueryType.class, "queryType");
        gen.addSimpleField(String.class, "entityTypeCodePattern");
        gen.addSimpleField(String.class, "sql");
        gen.addSimpleField(boolean.class, "public");
        addRegistrationDate(gen);
        addRegistrator(gen);
        addModificationDate(gen);

        gen.setToStringMethod("\"Query \" + name");

        return gen;
    }

    private static DtoGenerator createQueryDatabase()
    {
        DtoGenerator gen = new DtoGenerator("query", "QueryDatabase", QueryDatabaseFetchOptions.class);

        addPermId(gen, QueryDatabaseName.class);
        gen.addSimpleField(String.class, "name");
        gen.addSimpleField(String.class, "label");
        addSpace(gen);
        gen.addSimpleField(Role.class, "creatorMinimalRole");
        gen.addSimpleField(RoleLevel.class, "creatorMinimalRoleLevel");

        gen.setToStringMethod("\"QueryDatabase \" + name");

        return gen;
    }

    private static DtoGenerator createPersonalAccessToken()
    {
        DtoGenerator gen = new DtoGenerator("pat", "PersonalAccessToken", PersonalAccessTokenFetchOptions.class);

        addPermId(gen, PersonalAccessTokenPermId.class);
        gen.addSimpleField(String.class, "hash");
        gen.addSimpleField(String.class, "sessionName");
        gen.addDateField("validFromDate");
        gen.addDateField("validToDate");
        gen.addFetchedField(Person.class, "owner", "Owner", PersonFetchOptions.class).withInterface(IOwnerHolder.class);
        addRegistrator(gen);
        addModifier(gen);

        addRegistrationDate(gen);
        addModificationDate(gen);
        gen.addDateField("accessDate");

        gen.setToStringMethod("\"Personal Access Token \" + permId");

        return gen;
    }

    private static DtoGenerator createSessionInformation()
    {
        DtoGenerator gen = new DtoGenerator("session", "SessionInformation", SessionInformationFetchOptions.class);

        gen.addSimpleField(String.class, "sessionToken");
        gen.addSimpleField(String.class, "userName");
        gen.addSimpleField(String.class, "homeGroupCode");
        gen.addSimpleField(boolean.class, "personalAccessTokenSession");
        gen.addSimpleField(String.class, "personalAccessTokenSessionName");
        gen.addFetchedField(Person.class, "person", "Person", PersonFetchOptions.class);
        gen.addFetchedField(Person.class, "creatorPerson", "CreatorPerson", PersonFetchOptions.class);

        gen.setToStringMethod("\"SessionInformation[userName=\" + userName + \"]\"");

        return gen;
    }

    public static void main(String[] args) throws FileNotFoundException
    {
        List<DtoGenerator> list = new LinkedList<DtoGenerator>();
        list.add(createSessionInformation());
        list.add(createPersonalAccessToken());
        list.add(createDataSetGenerator());
        list.add(createDataSetTypeGenerator());
        list.add(createAttachmentGenerator());
        list.add(createExperimentGenerator());
        list.add(createExperimentTypeGenerator());
        list.add(createPersonGenerator());
        list.add(createProjectGenerator());
        list.add(createSampleGenerator());
        list.add(createSampleTypeGenerator());
        list.add(createSpaceGenerator());
        list.add(createTagGenerator());
        list.add(createMaterialGenerator());
        list.add(createMaterialTypeGenerator());
        list.add(createVocabularyTerm());
        list.add(createVocabulary());
        list.add(createLocatorType());
        list.add(createFileFormatType());
        list.add(createStorageFormat());
        list.add(createPhysicalDataGenerator());
        list.add(createLinkedDataGenerator());
        list.add(createHistoryEntryGenerator());
        list.add(createDeletion());
        list.add(createDataStoreGenerator());
        list.add(createExternalDmsGenerator());
        list.add(createCustomASServiceGenerator());
        list.add(createObjectKindModificationGenerator());
        list.add(createGlobalSearchObject());
        list.add(createPropertyAssignmentGenerator());
        list.add(createPropertyTypeGenerator());
        list.add(createOperationExecution());
        list.add(createOperationExecutionSummary());
        list.add(createOperationExecutionDetails());
        list.add(createSemanticAnnotation());
        list.add(createQuery());
        list.add(createQueryDatabase());
        list.add(createEvent());

        for (DtoGenerator gen : list)
        {
            System.out.print("Generating api classes for " + gen + "...");
            gen.generateDTO();
            gen.generateFetchOptions();
            // gen.generateDTOJS();
            // gen.generateFetchOptionsJS();
            System.out.println("done");
        }
    }
}
