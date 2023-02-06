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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.update;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IObjectUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.id.ISemanticAnnotationId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.semanticannotation.update.SemanticAnnotationUpdate")
public class SemanticAnnotationUpdate implements IUpdate, IObjectUpdate<ISemanticAnnotationId>
{

    private static final long serialVersionUID = 1L;

    @JsonProperty
    private ISemanticAnnotationId semanticAnnotationId;

    @JsonProperty
    private FieldUpdateValue<String> predicateOntologyId = new FieldUpdateValue<String>();

    @JsonProperty
    private FieldUpdateValue<String> predicateOntologyVersion = new FieldUpdateValue<String>();

    @JsonProperty
    private FieldUpdateValue<String> predicateAccessionId = new FieldUpdateValue<String>();

    @JsonProperty
    private FieldUpdateValue<String> descriptorOntologyId = new FieldUpdateValue<String>();

    @JsonProperty
    private FieldUpdateValue<String> descriptorOntologyVersion = new FieldUpdateValue<String>();

    @JsonProperty
    private FieldUpdateValue<String> descriptorAccessionId = new FieldUpdateValue<String>();

    @Override
    @JsonIgnore
    public ISemanticAnnotationId getObjectId()
    {
        return getSemanticAnnotationId();
    }

    @JsonIgnore
    public ISemanticAnnotationId getSemanticAnnotationId()
    {
        return semanticAnnotationId;
    }

    @JsonIgnore
    public void setSemanticAnnotationId(ISemanticAnnotationId semanticAnnotationId)
    {
        this.semanticAnnotationId = semanticAnnotationId;
    }

    @JsonIgnore
    public void setPredicateOntologyId(String predicateOntologyId)
    {
        this.predicateOntologyId.setValue(predicateOntologyId);
    }

    @JsonIgnore
    public FieldUpdateValue<String> getPredicateOntologyId()
    {
        return predicateOntologyId;
    }

    @JsonIgnore
    public void setPredicateOntologyVersion(String predicateOntologyVersion)
    {
        this.predicateOntologyVersion.setValue(predicateOntologyVersion);
    }

    @JsonIgnore
    public FieldUpdateValue<String> getPredicateOntologyVersion()
    {
        return predicateOntologyVersion;
    }

    @JsonIgnore
    public void setPredicateAccessionId(String predicateAccessionId)
    {
        this.predicateAccessionId.setValue(predicateAccessionId);
    }

    @JsonIgnore
    public FieldUpdateValue<String> getPredicateAccessionId()
    {
        return predicateAccessionId;
    }

    @JsonIgnore
    public void setDescriptorOntologyId(String descriptorOntologyId)
    {
        this.descriptorOntologyId.setValue(descriptorOntologyId);
    }

    @JsonIgnore
    public FieldUpdateValue<String> getDescriptorOntologyId()
    {
        return descriptorOntologyId;
    }

    @JsonIgnore
    public void setDescriptorOntologyVersion(String descriptorOntologyVersion)
    {
        this.descriptorOntologyVersion.setValue(descriptorOntologyVersion);
    }

    @JsonIgnore
    public FieldUpdateValue<String> getDescriptorOntologyVersion()
    {
        return descriptorOntologyVersion;
    }

    @JsonIgnore
    public void setDescriptorAccessionId(String descriptorAccessionId)
    {
        this.descriptorAccessionId.setValue(descriptorAccessionId);
    }

    @JsonIgnore
    public FieldUpdateValue<String> getDescriptorAccessionId()
    {
        return descriptorAccessionId;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("annotationId", semanticAnnotationId).toString();
    }

}
