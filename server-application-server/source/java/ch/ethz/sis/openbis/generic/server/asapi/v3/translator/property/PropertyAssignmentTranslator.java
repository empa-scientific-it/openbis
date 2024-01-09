/*
 * Copyright ETH 2016 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.property;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IEntityType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.IPropertyTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyAssignmentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.SemanticAnnotation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectRelationRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset.IDataSetTypeTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.experiment.IExperimentTypeTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.material.IMaterialTypeTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.person.IPersonTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.plugin.IPluginTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.sample.ISampleTypeTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.sample.SampleQuery;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.semanticannotation.ISemanticAnnotationTranslator;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.lemnik.eodsql.QueryTool;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class PropertyAssignmentTranslator implements IPropertyAssignmentTranslator
{

    @Autowired
    private IPropertyTypeTranslator propertyTypeTranslator;

    @Autowired
    private IMaterialTypeTranslator materialTypeTranslator;

    @Autowired
    private IExperimentTypeTranslator experimentTypeTranslator;

    @Autowired
    private ISampleTypeTranslator sampleTypeTranslator;

    @Autowired
    private IDataSetTypeTranslator dataSetTypeTranslator;

    @Autowired
    private ISemanticAnnotationTranslator annotationTranslator;

    @Autowired
    private IPersonTranslator personTranslator;

    @Autowired
    private IPluginTranslator pluginTranslator;
    
    @Override
    public Map<Long, PropertyAssignment> getIdToAssignmentMap(TranslationContext context, Collection<PropertyAssignmentRecord> assignmentRecords,
            PropertyAssignmentFetchOptions assignmentFetchOptions)
    {
        Map<PropertyAssignmentKey, PropertyAssignment> keyToAssignmentMap = getKeyToAssignmentMap(context, assignmentRecords, assignmentFetchOptions);
        Map<Long, PropertyAssignment> idToAssignmentMap = new HashMap<Long, PropertyAssignment>();

        for (Map.Entry<PropertyAssignmentKey, PropertyAssignment> entry : keyToAssignmentMap.entrySet())
        {
            idToAssignmentMap.put(entry.getKey().getId(), entry.getValue());
        }

        return idToAssignmentMap;
    }

    @Override
    public Map<PropertyAssignmentKey, PropertyAssignment> getKeyToAssignmentMap(TranslationContext context,
            Collection<PropertyAssignmentRecord> assignmentRecords,
            PropertyAssignmentFetchOptions assignmentFetchOptions)
    {
        Map<PropertyAssignmentKey, PropertyAssignment> assignments = new HashMap<>();

        for (PropertyAssignmentRecord assignmentRecord : assignmentRecords)
        {
            EntityKind entityKind = EntityKind.valueOf(assignmentRecord.kind_code);
            IEntityTypeId entityTypeId = new EntityTypePermId(assignmentRecord.type_code, entityKind);
            IPropertyTypeId propertyTypeId =
                    new PropertyTypePermId(CodeConverter.tryToBusinessLayer(assignmentRecord.prty_code, assignmentRecord.prty_is_managed_internally));

            PropertyAssignment assignment = new PropertyAssignment();
            assignment.setPermId(new PropertyAssignmentPermId(entityTypeId, propertyTypeId));
            assignment.setSection(assignmentRecord.section);
            assignment.setOrdinal(assignmentRecord.ordinal);
            assignment.setMandatory(assignmentRecord.is_mandatory);
            assignment.setShowInEditView(assignmentRecord.is_shown_edit);
            assignment.setShowRawValueInForms(assignmentRecord.show_raw_value);
            assignment.setSemanticAnnotationsInherited(false);
            assignment.setRegistrationDate(assignmentRecord.registration_timestamp);
            assignment.setUnique(assignmentRecord.is_unique);
            assignment.setFetchOptions(assignmentFetchOptions);

            PropertyAssignmentKey key = new PropertyAssignmentKey(assignmentRecord.id, entityKind);
            assignments.put(key, assignment);
        }

        if (assignmentFetchOptions.getSortBy() != null && (assignmentFetchOptions.getSortBy().getCode() != null
                || assignmentFetchOptions.getSortBy().getLabel() != null))
        {
            assignmentFetchOptions.withPropertyType();
        }

        if (assignmentFetchOptions.hasEntityType())
        {
            setEntityTypes(context, assignments, assignmentRecords, assignmentFetchOptions, EntityKind.MATERIAL);
            setEntityTypes(context, assignments, assignmentRecords, assignmentFetchOptions, EntityKind.EXPERIMENT);
            setEntityTypes(context, assignments, assignmentRecords, assignmentFetchOptions, EntityKind.SAMPLE);
            setEntityTypes(context, assignments, assignmentRecords, assignmentFetchOptions, EntityKind.DATA_SET);
        }

        if (assignmentFetchOptions.hasPropertyType())
        {
            Map<Long, List<PropertyAssignment>> assignmentsByPropertyTypeId = getAssignments(assignments, assignmentRecords, r -> r.prty_id);
            Map<Long, PropertyType> propertyTypeMap =
                    propertyTypeTranslator.translate(context, assignmentsByPropertyTypeId.keySet(), assignmentFetchOptions.withPropertyType());

            for (Map.Entry<Long, List<PropertyAssignment>> entry : assignmentsByPropertyTypeId.entrySet())
            {
                PropertyType propertyType = propertyTypeMap.get(entry.getKey());
                for (PropertyAssignment assignment : entry.getValue())
                {
                    assignment.setPropertyType(propertyType);
                }
            }
        }

        if (assignmentFetchOptions.hasSemanticAnnotations())
        {
            setSemanticAnnotations(context, assignments, assignmentRecords, assignmentFetchOptions, EntityKind.SAMPLE);
            // potentially more entity kinds in the future
        }

        if (assignmentFetchOptions.hasRegistrator())
        {
            Map<Long, List<PropertyAssignment>> assignmentsByRegistatorId = getAssignments(assignments, assignmentRecords, r -> r.pers_id_registerer);
            Map<Long, Person> registratorMap =
                    personTranslator.translate(context, assignmentsByRegistatorId.keySet(), assignmentFetchOptions.withRegistrator());

            for (Map.Entry<Long, List<PropertyAssignment>> entry : assignmentsByRegistatorId.entrySet())
            {
                Person registrator = registratorMap.get(entry.getKey());
                for (PropertyAssignment assignment : entry.getValue())
                {
                    assignment.setRegistrator(registrator);
                }
            }
        }
        
        if (assignmentFetchOptions.hasPlugin())
        {
            Map<Long, List<PropertyAssignment>> assignmentsByPluginId = getAssignments(assignments, assignmentRecords, r -> r.script_id);
            Map<Long, Plugin> registratorMap =
                    pluginTranslator.translate(context, assignmentsByPluginId.keySet(), assignmentFetchOptions.withPlugin());

            for (Map.Entry<Long, List<PropertyAssignment>> entry : assignmentsByPluginId.entrySet())
            {
                Plugin plugin = registratorMap.get(entry.getKey());
                for (PropertyAssignment assignment : entry.getValue())
                {
                    assignment.setPlugin(plugin);
                }
            }
        }

        return assignments;
    }

    private Map<Long, List<PropertyAssignment>> getAssignments(Map<PropertyAssignmentKey, PropertyAssignment> assignments,
            Collection<PropertyAssignmentRecord> assignmentRecords, Function<PropertyAssignmentRecord, Long> extractor)
    {
        Map<Long, List<PropertyAssignment>> map = new HashMap<Long, List<PropertyAssignment>>();

        for (PropertyAssignmentRecord assignmentRecord : assignmentRecords)
        {
            EntityKind entityKind = EntityKind.valueOf(assignmentRecord.kind_code);
            PropertyAssignmentKey key = new PropertyAssignmentKey(assignmentRecord.id, entityKind);
            PropertyAssignment assignment = assignments.get(key);
            Long id = extractor.apply(assignmentRecord);
            if (id != null)
            {
                List<PropertyAssignment> list = map.get(id);
                
                if (list == null)
                {
                    list = new ArrayList<PropertyAssignment>();
                    map.put(id, list);
                }
                
                list.add(assignment);
            }
        }

        return map;
    }

    private void setEntityTypes(TranslationContext context, Map<PropertyAssignmentKey, PropertyAssignment> assignments,
            Collection<PropertyAssignmentRecord> assignmentRecords, PropertyAssignmentFetchOptions assignmentFetchOptions, EntityKind entityKind)
    {
        Map<Long, List<PropertyAssignment>> assignmentsByEntityTypeId = new HashMap<Long, List<PropertyAssignment>>();

        for (PropertyAssignmentRecord assignmentRecord : assignmentRecords)
        {
            if (entityKind.equals(EntityKind.valueOf(assignmentRecord.kind_code)))
            {
                PropertyAssignmentKey key = new PropertyAssignmentKey(assignmentRecord.id, entityKind);
                PropertyAssignment assignment = assignments.get(key);
                List<PropertyAssignment> list = assignmentsByEntityTypeId.get(assignmentRecord.type_id);

                if (list == null)
                {
                    list = new ArrayList<PropertyAssignment>();
                    assignmentsByEntityTypeId.put(assignmentRecord.type_id, list);
                }

                list.add(assignment);
            }
        }

        Map<Long, ? extends IEntityType> entityTypeMap = null;

        if (entityKind.equals(EntityKind.MATERIAL))
        {
            MaterialTypeFetchOptions materialTypeFetchOptions = new MaterialTypeFetchOptions();
            if (assignmentFetchOptions.withEntityType().hasPropertyAssignments())
            {
                materialTypeFetchOptions.withPropertyAssignments();
            }
            entityTypeMap = materialTypeTranslator.translate(context, assignmentsByEntityTypeId.keySet(), materialTypeFetchOptions);
        } else if (entityKind.equals(EntityKind.EXPERIMENT))
        {
            ExperimentTypeFetchOptions experimentTypeFetchOptions = new ExperimentTypeFetchOptions();
            if (assignmentFetchOptions.withEntityType().hasPropertyAssignments())
            {
                experimentTypeFetchOptions.withPropertyAssignments();
            }
            entityTypeMap = experimentTypeTranslator.translate(context, assignmentsByEntityTypeId.keySet(), experimentTypeFetchOptions);
        } else if (entityKind.equals(EntityKind.SAMPLE))
        {
            SampleTypeFetchOptions sampleTypeFetchOptions = new SampleTypeFetchOptions();
            if (assignmentFetchOptions.withEntityType().hasPropertyAssignments())
            {
                sampleTypeFetchOptions.withPropertyAssignments();
            }
            entityTypeMap = sampleTypeTranslator.translate(context, assignmentsByEntityTypeId.keySet(), sampleTypeFetchOptions);
        } else if (entityKind.equals(EntityKind.DATA_SET))
        {
            DataSetTypeFetchOptions dataSetTypeFetchOptions = new DataSetTypeFetchOptions();
            if (assignmentFetchOptions.withEntityType().hasPropertyAssignments())
            {
                dataSetTypeFetchOptions.withPropertyAssignments();
            }
            entityTypeMap = dataSetTypeTranslator.translate(context, assignmentsByEntityTypeId.keySet(), dataSetTypeFetchOptions);
        } else
        {
            throw new IllegalArgumentException("Unsupported enity kind: " + entityKind);
        }

        for (Map.Entry<Long, List<PropertyAssignment>> entry : assignmentsByEntityTypeId.entrySet())
        {
            IEntityType entityType = entityTypeMap.get(entry.getKey());
            for (PropertyAssignment assignment : entry.getValue())
            {
                assignment.setEntityType(entityType);
            }
        }
    }

    private void setSemanticAnnotations(TranslationContext context, Map<PropertyAssignmentKey, PropertyAssignment> assignments,
            Collection<PropertyAssignmentRecord> assignmentRecords, PropertyAssignmentFetchOptions assignmentFetchOptions, EntityKind entityKind)
    {
        Collection<Long> propertyAssignmentIds = new HashSet<Long>();

        for (PropertyAssignmentRecord assignmentRecord : assignmentRecords)
        {
            if (entityKind.equals(EntityKind.valueOf(assignmentRecord.kind_code)))
            {
                propertyAssignmentIds.add(assignmentRecord.id);
            }
        }

        List<ObjectRelationRecord> assignmentToAnnotationRecords = null;

        if (entityKind.equals(EntityKind.SAMPLE))
        {
            SampleQuery sampleQuery = QueryTool.getManagedQuery(SampleQuery.class);
            assignmentToAnnotationRecords = sampleQuery.getPropertyAssignmentAnnotationIds(new LongOpenHashSet(propertyAssignmentIds));
        }

        if (assignmentToAnnotationRecords != null)
        {
            Collection<Long> annotationIds = new HashSet<Long>();

            for (ObjectRelationRecord assignmentToAnnotationRecord : assignmentToAnnotationRecords)
            {
                annotationIds.add(assignmentToAnnotationRecord.relatedId);
            }

            Map<Long, SemanticAnnotation> annotations =
                    annotationTranslator.translate(context, annotationIds, assignmentFetchOptions.withSemanticAnnotations());

            for (ObjectRelationRecord assignmentToAnnotationRecord : assignmentToAnnotationRecords)
            {
                PropertyAssignmentKey key = new PropertyAssignmentKey(assignmentToAnnotationRecord.objectId, entityKind);
                PropertyAssignment assignment = assignments.get(key);
                SemanticAnnotation annotation = annotations.get(assignmentToAnnotationRecord.relatedId);

                List<SemanticAnnotation> assignmentAnnotations = assignment.getSemanticAnnotations();
                if (assignmentAnnotations == null)
                {
                    assignmentAnnotations = new ArrayList<SemanticAnnotation>();
                    assignment.setSemanticAnnotations(assignmentAnnotations);
                }
                assignmentAnnotations.add(annotation);
            }
        }

        setSemanticAnnotationsFromPropertyTypeIfMissing(context, assignments, assignmentRecords, assignmentFetchOptions, EntityKind.SAMPLE);
    }

    private void setSemanticAnnotationsFromPropertyTypeIfMissing(TranslationContext context,
            Map<PropertyAssignmentKey, PropertyAssignment> assignments,
            Collection<PropertyAssignmentRecord> assignmentRecords, PropertyAssignmentFetchOptions assignmentFetchOptions, EntityKind entityKind)
    {
        Collection<Long> propertyTypeIds = new HashSet<Long>();

        for (PropertyAssignmentRecord assignmentRecord : assignmentRecords)
        {
            if (entityKind.equals(EntityKind.valueOf(assignmentRecord.kind_code)))
            {
                propertyTypeIds.add(assignmentRecord.prty_id);
            }
        }

        PropertyTypeFetchOptions propertyTypeFetchOptions = new PropertyTypeFetchOptions();
        propertyTypeFetchOptions.withSemanticAnnotations();

        Map<Long, PropertyType> propertyTypes = propertyTypeTranslator.translate(context, propertyTypeIds, propertyTypeFetchOptions);

        for (PropertyAssignmentRecord assignmentRecord : assignmentRecords)
        {
            if (entityKind.equals(EntityKind.valueOf(assignmentRecord.kind_code)))
            {
                PropertyAssignmentKey key = new PropertyAssignmentKey(assignmentRecord.id, entityKind);
                PropertyAssignment assignment = assignments.get(key);

                if (assignment.getSemanticAnnotations() == null || assignment.getSemanticAnnotations().isEmpty())
                {
                    PropertyType propertyType = propertyTypes.get(assignmentRecord.prty_id);
                    assignment.setSemanticAnnotations(propertyType.getSemanticAnnotations());
                    assignment.setSemanticAnnotationsInherited(true);
                }
            }
        }
    }

}
