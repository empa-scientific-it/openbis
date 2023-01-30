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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.SimpleYesNoRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETNewPTAssigments;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewPTNewAssigment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;

/**
 * Provider of instances of {@link EntityTypePropertyType} from a browser based data feed.
 * 
 * @author Juan Fuentes
 */
public class EntityTypePropertyTypeBrowserProvider extends EntityTypePropertyTypeProvider
{
    private final List<NewPTNewAssigment> propertyTypesAsgs;

    public EntityTypePropertyTypeBrowserProvider(IApplicationServerApi applicationServerApi, String sessionToken, EntityType entity,
            List<NewPTNewAssigment> propertyTypesAsgs)
    {
        super(null, applicationServerApi, sessionToken, entity);
        this.propertyTypesAsgs = propertyTypesAsgs;
    }

    @Override
    protected TypedTableModel<EntityTypePropertyType<?>> createTableModel()
    {
        SemanticAnnotationProvider annotationProvider = new SemanticAnnotationProvider();

        TypedTableModelBuilder<EntityTypePropertyType<?>> builder = new TypedTableModelBuilder<EntityTypePropertyType<?>>();
        builder.addColumn(ORDINAL).withDefaultWidth(100);
        builder.addColumn(SECTION);
        builder.addColumn(PROPERTY_TYPE_CODE).withDefaultWidth(200);
        builder.addColumn(LABEL).hideByDefault();
        builder.addColumn(DESCRIPTION).hideByDefault();
        builder.addColumn(MODIFICATION_DATE).withDefaultWidth(300).hideByDefault();
        builder.addColumn(IS_MANDATORY);
        builder.addColumn(DATA_TYPE).withDefaultWidth(200);
        builder.addColumn(IS_DYNAMIC);
        builder.addColumn(IS_MANAGED);
        builder.addColumn(IS_SHOWN_IN_EDITOR_VIEW);
        builder.addColumn(SHOW_RAW_VALUE);
        builder.addColumn(SCRIPT);

        annotationProvider.addMoreColumns(builder, true);

        Collection<EntityTypePropertyType<?>> etpts = new ArrayList<EntityTypePropertyType<?>>();
        for (NewPTNewAssigment propertyTypeAsg : propertyTypesAsgs)
        {
            EntityTypePropertyType<?> etpt = NewETNewPTAssigments.getEntityTypePropertyType(entity, propertyTypeAsg);
            etpts.add(etpt);
        }

        Map<EntityTypePropertyType<?>, PropertyAssignment> assignmentsMap = createAssignmentsMap(etpts);

        for (EntityTypePropertyType<?> etpt : etpts)
        {
            //
            // Create Row
            //
            builder.addRow(etpt);
            PropertyType propertyType = etpt.getPropertyType();
            builder.column(ORDINAL).addInteger(etpt.getOrdinal());
            builder.column(SECTION).addString(etpt.getSection());
            builder.column(PROPERTY_TYPE_CODE).addString(propertyType.getCode());
            builder.column(LABEL).addString(propertyType.getLabel());
            builder.column(DESCRIPTION).addString(propertyType.getDescription());
            builder.column(MODIFICATION_DATE).addDate(propertyType.getModificationDate());
            builder.column(IS_MANDATORY).addString(SimpleYesNoRenderer.render(etpt.isMandatory()));
            builder.column(DATA_TYPE).addString(PropertyTypeProvider.renderDataType(propertyType));
            builder.column(IS_DYNAMIC).addString(SimpleYesNoRenderer.render(etpt.isDynamic()));
            builder.column(IS_MANAGED).addString(SimpleYesNoRenderer.render(etpt.isManaged()));
            builder.column(IS_SHOWN_IN_EDITOR_VIEW).addString(SimpleYesNoRenderer.render(etpt.isShownInEditView()));
            builder.column(SHOW_RAW_VALUE).addString(SimpleYesNoRenderer.render(etpt.getShowRawValue()));
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

        return builder.getModel();
    }

}
