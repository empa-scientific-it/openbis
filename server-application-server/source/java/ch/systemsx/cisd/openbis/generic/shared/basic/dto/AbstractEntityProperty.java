/*
 * Copyright ETH 2009 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.renderer.VocabularyPropertyColRenderer;

import java.io.Serializable;

/**
 * The abstract base implementation of {@link IEntityProperty}, only featuring a {@link PropertyType}.
 * <p>
 * All getters (except {@link #getPropertyType()} will return <code>null</code>, all setters (except {@link #setPropertyType(PropertyType)} will throw
 * an {@link UnsupportedOperationException}.
 * 
 * @author Bernd Rinn
 */
public abstract class AbstractEntityProperty implements IEntityProperty
{

    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private PropertyType propertyType;

    private Long ordinal;

    private boolean scriptable;

    private boolean dynamic;

    @Override
    public PropertyType getPropertyType()
    {
        return propertyType;
    }

    @Override
    public void setPropertyType(PropertyType propertyType)
    {
        this.propertyType = propertyType;
    }

    @Override
    public String tryGetAsString()
    {
        if (propertyType == null)
        {
            return null;
        }
        DataType dataType = propertyType.getDataType();
        if (dataType == null)
        {
            return getStringValue();
        }
        switch (dataType.getCode())
        {
            case CONTROLLEDVOCABULARY:
                VocabularyTerm vocabularyTerm = getVocabularyTerm();
                return (vocabularyTerm != null) ? vocabularyTerm.getCode() : getArrayAsString();
            case MATERIAL:
                Material material = getMaterial();
                return (material != null) ? MaterialIdentifier.print(material.getCode(), material
                        .getMaterialType().getCode()) : getStringValue();
            case SAMPLE:
                Sample sample = getSample();
                return (sample != null) ? sample.getPermId() : getArrayAsString();
            case DATE:
                String timestamp = getStringValue();
                return timestamp != null ? timestamp.split(" ")[0] : null;
            default:
                return getStringValue();
        }
    }

    protected String getArrayAsString() {
        Serializable value = getValue();
        if(value != null && value.getClass().isArray()) {
            StringBuilder builder = new StringBuilder();
            for (Serializable term : (Serializable[]) value)
            {
                if (builder.length() > 0)
                {
                    builder.append(", ");
                }
                builder.append(term);
            }
            return builder.toString();
        } else {
            return getStringValue();
        }
    }

    @Override
    public String tryGetOriginalValue()
    {
        return tryGetAsString();
    }

    @Override
    public String getStringValue()
    {
        return null;
    }

    @Override
    public void setValue(String value)
    {
    }

    @Override
    public void setValue(Serializable value)
    {
    }

    @Override
    public Serializable getValue()
    {
        return null;
    }

    @Override
    public Material getMaterial()
    {
        return null;
    }

    @Override
    public void setMaterial(Material material)
    {
    }

    @Override
    public Sample getSample()
    {
        return null;
    }

    @Override
    public void setSample(Sample sample)
    {
    }

    @Override
    public VocabularyTerm getVocabularyTerm()
    {
        return null;
    }

    @Override
    public void setVocabularyTerm(VocabularyTerm vocabularyTerm)
    {
    }

    @Override
    public void setOrdinal(Long ordinal)
    {
        this.ordinal = ordinal;
    }

    @Override
    public Long getOrdinal()
    {
        return ordinal;
    }

    @Override
    public boolean isManaged()
    {
        return false;
    }

    public void setScriptable(boolean scriptable)
    {
        this.scriptable = scriptable;
    }

    @Override
    public boolean isScriptable()
    {
        return scriptable;
    }

    @Override
    public boolean isDynamic()
    {
        return dynamic;
    }

    public void setDynamic(boolean dynamic)
    {
        this.dynamic = dynamic;
    }

    //
    // Object
    //

    @Override
    public String toString()
    {
        return propertyType + ": " + tryGetAsString();
    }

    //
    // Comparable
    //

    @Override
    public int compareTo(IEntityProperty o)
    {
        Long thisOrdinal = this.getOrdinal();
        Long otherOrdinal = o.getOrdinal();
        if (thisOrdinal != null && otherOrdinal != null)
        {
            return thisOrdinal.compareTo(otherOrdinal);
        }
        PropertyType thisPropertyType = this.getPropertyType();
        PropertyType otherPropertyType = o.getPropertyType();
        if (thisPropertyType.getLabel().equals(otherPropertyType.getLabel()))
        {
            return thisPropertyType.getCode().compareTo(otherPropertyType.getCode());
        } else
        {
            return thisPropertyType.getLabel().compareTo(otherPropertyType.getLabel());
        }
    }
}
