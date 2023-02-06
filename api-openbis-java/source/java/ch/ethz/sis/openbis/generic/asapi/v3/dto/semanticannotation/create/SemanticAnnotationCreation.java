/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.create;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.ICreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.IObjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.IPropertyAssignmentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.IPropertyTypeId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.semanticannotation.create.SemanticAnnotationCreation")
public class SemanticAnnotationCreation implements ICreation, IObjectCreation
{
    private static final long serialVersionUID = 1L;

    private IEntityTypeId entityTypeId;

    private IPropertyTypeId propertyTypeId;

    private IPropertyAssignmentId propertyAssignmentId;

    private String predicateOntologyId;

    private String predicateOntologyVersion;

    private String predicateAccessionId;

    private String descriptorOntologyId;

    private String descriptorOntologyVersion;

    private String descriptorAccessionId;

    public IEntityTypeId getEntityTypeId()
    {
        return entityTypeId;
    }

    public void setEntityTypeId(IEntityTypeId entityTypeId)
    {
        this.entityTypeId = entityTypeId;
    }

    public IPropertyTypeId getPropertyTypeId()
    {
        return propertyTypeId;
    }

    public void setPropertyTypeId(IPropertyTypeId propertyTypeId)
    {
        this.propertyTypeId = propertyTypeId;
    }

    public IPropertyAssignmentId getPropertyAssignmentId()
    {
        return propertyAssignmentId;
    }

    public void setPropertyAssignmentId(IPropertyAssignmentId propertyAssignmentId)
    {
        this.propertyAssignmentId = propertyAssignmentId;
    }

    public String getPredicateOntologyId()
    {
        return predicateOntologyId;
    }

    public void setPredicateOntologyId(String predicateOntologyId)
    {
        this.predicateOntologyId = predicateOntologyId;
    }

    public String getPredicateOntologyVersion()
    {
        return predicateOntologyVersion;
    }

    public void setPredicateOntologyVersion(String predicateOntologyVersion)
    {
        this.predicateOntologyVersion = predicateOntologyVersion;
    }

    public String getPredicateAccessionId()
    {
        return predicateAccessionId;
    }

    public void setPredicateAccessionId(String predicateAccessionId)
    {
        this.predicateAccessionId = predicateAccessionId;
    }

    public String getDescriptorOntologyId()
    {
        return descriptorOntologyId;
    }

    public void setDescriptorOntologyId(String descriptorOntologyId)
    {
        this.descriptorOntologyId = descriptorOntologyId;
    }

    public String getDescriptorOntologyVersion()
    {
        return descriptorOntologyVersion;
    }

    public void setDescriptorOntologyVersion(String descriptorOntologyVersion)
    {
        this.descriptorOntologyVersion = descriptorOntologyVersion;
    }

    public String getDescriptorAccessionId()
    {
        return descriptorAccessionId;
    }

    public void setDescriptorAccessionId(String descriptorAccessionId)
    {
        this.descriptorAccessionId = descriptorAccessionId;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("entityTypeId", entityTypeId).append("propertyTypeId", propertyTypeId)
                .append("propertyAssignmentId", propertyAssignmentId).append("predicateOntologyId", predicateOntologyId)
                .append("predicateOntologyVersion", predicateOntologyVersion).append("predicateAccessionId", predicateAccessionId)
                .append("descriptorOntologyId", descriptorOntologyId).append("descriptorOntologyVersion", descriptorOntologyVersion)
                .append("descriptorAccessionId", descriptorAccessionId).toString();
    }

}
