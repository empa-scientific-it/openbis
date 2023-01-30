/*
 * Copyright 2009 ETH Zuerich, CISD
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
            return getValue();
        }
        switch (dataType.getCode())
        {
            case CONTROLLEDVOCABULARY:
                VocabularyTerm vocabularyTerm = getVocabularyTerm();
                return (vocabularyTerm != null) ? vocabularyTerm.getCode() : getValue();
            case MATERIAL:
                Material material = getMaterial();
                return (material != null) ? MaterialIdentifier.print(material.getCode(), material
                        .getMaterialType().getCode()) : getValue();
            case SAMPLE:
                Sample sample = getSample();
                return (sample != null) ? sample.getPermId() : getValue();
            case DATE:
                String timestamp = getValue();
                return timestamp != null ? timestamp.split(" ")[0] : null;
            default:
                return getValue();
        }
    }

    @Override
    public String tryGetOriginalValue()
    {
        return tryGetAsString();
    }

    @Override
    public String getValue()
    {
        return null;
    }

    @Override
    public void setValue(String value)
    {
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
