/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedUiDescription;

import java.io.Serializable;

/**
 * An {@link IEntityProperty} implementation for managed properties.
 * 
 * @author Piotr Buczek
 */
public class ManagedEntityProperty implements IEntityProperty, IManagedProperty
{

    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private IEntityProperty entityProperty;

    // NOTE: defaults are set for testing - scripts should override them

    private boolean ownTab = true;

    private IManagedUiDescription uiDescription = new ManagedUiDescription();

    public ManagedEntityProperty(IEntityProperty entityProperty)
    {
        this.entityProperty = entityProperty;
    }

    //
    // IManagedProperty
    //

    @Override
    public boolean isOwnTab()
    {
        return ownTab;
    }

    @Override
    public void setOwnTab(boolean ownTab)
    {
        this.ownTab = ownTab;
    }

    @Override
    public IManagedUiDescription getUiDescription()
    {
        return uiDescription;
    }

    @Override
    public String getPropertyTypeCode()
    {
        return entityProperty.getPropertyType().getCode();
    }

    @Override
    public boolean isSpecialValue()
    {
        return ManagedProperty.isSpecialValue(this.getValue());
    }

    //
    // IEntityProperty not delegated methods
    //

    @Override
    public boolean isManaged()
    {
        return true;
    }

    @Override
    public boolean isScriptable()
    {
        return true;
    }

    @Override
    public boolean isDynamic()
    {
        return false;
    }

    //
    // IEntityProperty delegated methods
    //

    @Override
    public String tryGetAsString()
    {
        return entityProperty.tryGetAsString();
    }

    @Override
    public String tryGetOriginalValue()
    {
        return entityProperty.tryGetOriginalValue();
    }

    @Override
    public Material getMaterial()
    {
        return entityProperty.getMaterial();
    }

    @Override
    public void setMaterial(Material material)
    {
        entityProperty.setMaterial(material);
    }

    public Sample getSample()
    {
        return entityProperty.getSample();
    }

    public void setSample(Sample sample)
    {
        entityProperty.setSample(sample);
    }

    @Override
    public VocabularyTerm getVocabularyTerm()
    {
        return entityProperty.getVocabularyTerm();
    }

    @Override
    public void setVocabularyTerm(VocabularyTerm vocabularyTerm)
    {
        entityProperty.setVocabularyTerm(vocabularyTerm);
    }

    @Override
    public String getStringValue()
    {
        return (String) entityProperty.getValue();
    }

    @Override
    public void setValue(String value)
    {
        entityProperty.setValue(value);
    }

    @Override
    public Serializable getValue()
    {
        return entityProperty.getValue();
    }

    @Override
    public void setValue(Serializable value)
    {
        entityProperty.setValue(value);
    }

    @Override
    public PropertyType getPropertyType()
    {
        return entityProperty.getPropertyType();
    }

    @Override
    public void setPropertyType(PropertyType propertyType)
    {
        entityProperty.setPropertyType(propertyType);
    }

    @Override
    public void setOrdinal(Long ordinal)
    {
        entityProperty.setOrdinal(ordinal);
    }

    @Override
    public Long getOrdinal()
    {
        return entityProperty.getOrdinal();
    }

    @Override
    public int compareTo(IEntityProperty o)
    {
        return entityProperty.compareTo(o);
    }

    //
    // For serialization
    //

    @Override
    public String toString()
    {
        return entityProperty.toString();
    }

    public ManagedEntityProperty()
    {
    }

}
