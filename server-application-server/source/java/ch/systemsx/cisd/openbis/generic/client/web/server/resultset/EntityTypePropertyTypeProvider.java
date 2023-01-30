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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.CommonGridColumnIDs.MODIFICATION_DATE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyTypeAssignmentGridColumnIDs.ASSIGNED_TO;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyTypeAssignmentGridColumnIDs.DATA_TYPE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyTypeAssignmentGridColumnIDs.DESCRIPTION;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyTypeAssignmentGridColumnIDs.IS_DYNAMIC;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyTypeAssignmentGridColumnIDs.IS_MANAGED;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyTypeAssignmentGridColumnIDs.IS_MANDATORY;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyTypeAssignmentGridColumnIDs.IS_SHOWN_IN_EDITOR_VIEW;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyTypeAssignmentGridColumnIDs.LABEL;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyTypeAssignmentGridColumnIDs.ORDINAL;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyTypeAssignmentGridColumnIDs.PROPERTY_TYPE_CODE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyTypeAssignmentGridColumnIDs.SCRIPT;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyTypeAssignmentGridColumnIDs.SECTION;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyTypeAssignmentGridColumnIDs.SHOW_RAW_VALUE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyTypeAssignmentGridColumnIDs.TYPE_OF;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.ReferenceIdentityMap;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.IPropertyAssignmentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.IPropertyTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyAssignmentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.PropertyAssignmentSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.EntityKindConverter;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.SimpleYesNoRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;

/**
 * Provider of instances of {@link EntityTypePropertyType}.
 * 
 * @author Franz-Josef Elmer
 */
public class EntityTypePropertyTypeProvider extends
        AbstractCommonTableModelProvider<EntityTypePropertyType<?>>
{

    private IApplicationServerApi applicationServerApi;

    protected final EntityType entity;

    public EntityTypePropertyTypeProvider(ICommonServer commonServer, IApplicationServerApi applicationServerApi, String sessionToken,
            EntityType entity)
    {
        super(commonServer, sessionToken);
        this.applicationServerApi = applicationServerApi;
        this.entity = entity;
    }

    @Override
    protected TypedTableModel<EntityTypePropertyType<?>> createTableModel()
    {
        List<EntityTypePropertyType<?>> etpts = commonServer.listEntityTypePropertyTypes(sessionToken);

        Map<EntityTypePropertyType<?>, PropertyAssignment> assignmentsMap = createAssignmentsMap(etpts);
        SemanticAnnotationProvider annotationProvider = new SemanticAnnotationProvider();

        TypedTableModelBuilder<EntityTypePropertyType<?>> builder =
                new TypedTableModelBuilder<EntityTypePropertyType<?>>();
        builder.addColumn(ORDINAL).withDefaultWidth(100);
        builder.addColumn(SECTION);
        builder.addColumn(PROPERTY_TYPE_CODE).withDefaultWidth(200);
        builder.addColumn(LABEL).hideByDefault();
        builder.addColumn(DESCRIPTION).hideByDefault();
        builder.addColumn(MODIFICATION_DATE).withDefaultWidth(300).hideByDefault();
        if (entity == null)
        {
            builder.addColumn(ASSIGNED_TO).withDefaultWidth(200);
            builder.addColumn(TYPE_OF);
        }
        builder.addColumn(IS_MANDATORY);
        builder.addColumn(DATA_TYPE).withDefaultWidth(200);
        builder.addColumn(IS_DYNAMIC);
        builder.addColumn(IS_MANAGED);
        builder.addColumn(IS_SHOWN_IN_EDITOR_VIEW);
        builder.addColumn(SHOW_RAW_VALUE);
        builder.addColumn(SCRIPT);

        annotationProvider.addMoreColumns(builder, true);

        for (EntityTypePropertyType<?> etpt : etpts)
        {
            if (entity == null || entity.equals(etpt.getEntityType()))
            {
                builder.addRow(etpt);
                PropertyType propertyType = etpt.getPropertyType();
                builder.column(ORDINAL).addInteger(etpt.getOrdinal());
                builder.column(SECTION).addString(etpt.getSection());
                builder.column(PROPERTY_TYPE_CODE).addString(propertyType.getCode());
                builder.column(LABEL).addString(propertyType.getLabel());
                builder.column(DESCRIPTION).addString(propertyType.getDescription());
                builder.column(MODIFICATION_DATE).addDate(propertyType.getModificationDate());
                if (entity == null)
                {
                    builder.column(ASSIGNED_TO).addString(etpt.getEntityType().getCode());
                    builder.column(TYPE_OF).addString(etpt.getEntityKind().getDescription());
                }
                builder.column(IS_MANDATORY).addString(
                        SimpleYesNoRenderer.render(etpt.isMandatory()));
                builder.column(DATA_TYPE).addString(PropertyTypeProvider.renderDataType(propertyType));
                builder.column(IS_DYNAMIC).addString(SimpleYesNoRenderer.render(etpt.isDynamic()));
                builder.column(IS_MANAGED).addString(SimpleYesNoRenderer.render(etpt.isManaged()));
                builder.column(IS_SHOWN_IN_EDITOR_VIEW).addString(
                        SimpleYesNoRenderer.render(etpt.isShownInEditView()));
                builder.column(SHOW_RAW_VALUE).addString(
                        SimpleYesNoRenderer.render(etpt.getShowRawValue()));
                Script script = etpt.getScript();
                if (script != null)
                {
                    builder.column(SCRIPT).addString(script.getName());
                }

                PropertyAssignment assignment = assignmentsMap.get(etpt);
                if (assignment != null)
                {
                    annotationProvider.addMoreCells(builder, assignment.getSemanticAnnotations(), assignment.isSemanticAnnotationsInherited());
                }
            }
        }
        return builder.getModel();
    }

    protected Map<EntityTypePropertyType<?>, PropertyAssignment> createAssignmentsMap(Collection<EntityTypePropertyType<?>> etpts)
    {
        Map<IPropertyAssignmentId, EntityTypePropertyType<?>> idToEtptMap = new HashMap<IPropertyAssignmentId, EntityTypePropertyType<?>>();

        for (EntityTypePropertyType<?> etpt : etpts)
        {
            IEntityTypeId entityTypeId = new EntityTypePermId(etpt.getEntityType().getCode(),
                    EntityKindConverter.convert(etpt.getEntityKind()));
            IPropertyTypeId propertyTypeId = new PropertyTypePermId(etpt.getPropertyType().getCode());
            idToEtptMap.put(new PropertyAssignmentPermId(entityTypeId, propertyTypeId), etpt);
        }

        PropertyAssignmentSearchCriteria criteria = new PropertyAssignmentSearchCriteria();
        criteria.withIds().thatIn(idToEtptMap.keySet());

        PropertyAssignmentFetchOptions fo = new PropertyAssignmentFetchOptions();
        fo.withSemanticAnnotations();

        SearchResult<PropertyAssignment> result = applicationServerApi.searchPropertyAssignments(sessionToken, criteria, fo);
        Map<EntityTypePropertyType<?>, PropertyAssignment> map =
                new ReferenceIdentityMap<EntityTypePropertyType<?>, PropertyAssignment>();

        for (PropertyAssignment assignment : result.getObjects())
        {
            EntityTypePropertyType<?> etpt = idToEtptMap.get(assignment.getPermId());
            map.put(etpt, assignment);
        }

        return map;
    }

}
