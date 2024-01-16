/*
 * Copyright ETH 2017 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.IPluginId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.IPropertyTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.ObjectNotFoundException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.plugin.IMapPluginByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.property.IMapPropertyTypeByIdExecutor;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IEntityTypePropertyTypeBO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;
import ch.systemsx.cisd.openbis.generic.shared.translator.DtoConverters;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Component
public class CreatePropertyAssignmentsExecutor
{
    @Resource(name = ComponentNames.COMMON_BUSINESS_OBJECT_FACTORY)
    protected ICommonBusinessObjectFactory businessObjectFactory;

    @Autowired
    private IMapPropertyTypeByIdExecutor mapPropertyTypeByIdExecutor;

    @Autowired
    private IMapPluginByIdExecutor mapPluginByIdExecutor;
    
    public void createPropertyAssignments(final IOperationContext context, String entityTypeCode,
            Collection<? extends PropertyAssignmentCreation> propertyAssignments, ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind entityKind)
    {
        if (propertyAssignments != null)
        {
            List<PropertyAssignmentCreation> assignmentCreations = new ArrayList<PropertyAssignmentCreation>();
            assignmentCreations.addAll(propertyAssignments);

            Collections.sort(assignmentCreations, new Comparator<PropertyAssignmentCreation>()
                {
                    @Override
                    public int compare(PropertyAssignmentCreation o1, PropertyAssignmentCreation o2)
                    {
                        int ordinal1 = o1.getOrdinal() != null ? o1.getOrdinal() : Integer.MAX_VALUE;
                        int ordinal2 = o2.getOrdinal() != null ? o2.getOrdinal() : Integer.MAX_VALUE;
                        return Integer.compare(ordinal1, ordinal2);
                    }
                });

            for (PropertyAssignmentCreation assignmentCreation : assignmentCreations)
            {
                createPropertyAssignments(context, entityTypeCode, entityKind, assignmentCreation);
            }
        }
    }

    private void createPropertyAssignments(IOperationContext context, String entityTypeCode,
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind entityKind, PropertyAssignmentCreation assignmentCreation)
    {
        NewETPTAssignment assignment = translateAssignment(context, entityTypeCode, entityKind, assignmentCreation);

        IEntityTypePropertyTypeBO propertyBO =
                businessObjectFactory.createEntityTypePropertyTypeBO(context.getSession(), DtoConverters.convertEntityKind(entityKind));
        propertyBO.createAssignment(assignment);
    }

    public NewETPTAssignment translateAssignment(IOperationContext context, String entityTypeCode,
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind entityKind, PropertyAssignmentCreation assignmentCreation)
    {
        NewETPTAssignment assignment = new NewETPTAssignment();
        assignment.setEntityKind(entityKind);

        PropertyTypePE propertyTypePE = findPropertyType(context, assignmentCreation.getPropertyTypeId());
        assignment.setPropertyTypeCode(propertyTypePE.getCode());

        assignment.setEntityTypeCode(entityTypeCode);
        assignment.setMandatory(assignmentCreation.isMandatory());
        assignment.setDefaultValue(assignmentCreation.getInitialValueForExistingEntities());
        assignment.setSection(assignmentCreation.getSection());
        if (assignmentCreation.getOrdinal() != null)
        {
            assignment.setOrdinal((long) assignmentCreation.getOrdinal() - 1);
        }

        if (assignmentCreation.getPluginId() != null)
        {
            ScriptPE pluginPE = findPlugin(context, assignmentCreation.getPluginId());

            if (false == ScriptType.DYNAMIC_PROPERTY.equals(pluginPE.getScriptType())
                    && false == ScriptType.MANAGED_PROPERTY.equals(pluginPE.getScriptType()))
            {
                throw new UserFailureException(
                        "Property assignment plugin has to be of type '" + ScriptType.DYNAMIC_PROPERTY + "' or '" + ScriptType.MANAGED_PROPERTY
                                + "'. The specified plugin with id '" + assignmentCreation.getPluginId() + "' is of type '" + pluginPE.getScriptType()
                                + "'.");
            }

            if (pluginPE.getEntityKind() != null && false == pluginPE.getEntityKind().equals(entityKind))
            {
                throw new UserFailureException("Property assignment plugin has entity kind set to '" + pluginPE.getEntityKind()
                        + "'. Expected a plugin where entity kind is either '" + entityKind + "' or null.");
            }

            assignment.setScriptName(pluginPE.getName());
            assignment.setDynamic(ScriptType.DYNAMIC_PROPERTY.equals(pluginPE.getScriptType()));
            assignment.setManaged(ScriptType.MANAGED_PROPERTY.equals(pluginPE.getScriptType()));
        }

        assignment.setShownInEditView(assignmentCreation.isShowInEditView());
        assignment.setShowRawValue(assignmentCreation.isShowRawValueInForms());
        assignment.setUnique(assignmentCreation.isUnique());
        return assignment;
    }

    private ScriptPE findPlugin(IOperationContext context, IPluginId pluginId)
    {
        Map<IPluginId, ScriptPE> pluginPEMap =
                mapPluginByIdExecutor.map(context, Arrays.asList(pluginId));
        ScriptPE pluginPE = pluginPEMap.get(pluginId);

        if (pluginPE == null)
        {
            throw new ObjectNotFoundException(pluginId);
        }

        return pluginPE;
    }

    private PropertyTypePE findPropertyType(IOperationContext context, IPropertyTypeId propertyTypeId)
    {
        Map<IPropertyTypeId, PropertyTypePE> propertyTypePEMap =
                mapPropertyTypeByIdExecutor.map(context, Arrays.asList(propertyTypeId));
        PropertyTypePE propertyTypePE = propertyTypePEMap.get(propertyTypeId);

        if (propertyTypePE == null)
        {
            throw new ObjectNotFoundException(propertyTypeId);
        }

        return propertyTypePE;
    }


}
