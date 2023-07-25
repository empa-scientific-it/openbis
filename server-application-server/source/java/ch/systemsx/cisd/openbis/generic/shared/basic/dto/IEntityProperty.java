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

import java.io.Serializable;

/**
 * An interface for entity properties.
 * 
 * @author Bernd Rinn
 */
public interface IEntityProperty extends Serializable, Comparable<IEntityProperty>
{
    public static final IEntityProperty[] EMPTY_ARRAY = new IEntityProperty[0];

    /**
     * Returns a string representation of whatever value this property represents. Vocabulary terms will be represented as their CODE, material values
     * will be represented as "CODE (TYPE_CODE)".
     */
    public String tryGetAsString();

    public String tryGetOriginalValue();

    public Material getMaterial();

    public void setMaterial(Material material);

    public Sample getSample();
    
    public void setSample(Sample sample);
    
    public VocabularyTerm getVocabularyTerm();

    public void setVocabularyTerm(VocabularyTerm vocabularyTerm);

    public String getStringValue();

    public void setValue(final String value);

    public Serializable getValue();

    public void setValue(final Serializable value);

    public PropertyType getPropertyType();

    public void setPropertyType(final PropertyType propertyType);

    void setOrdinal(Long ordinal);

    Long getOrdinal();

    boolean isManaged();

    boolean isDynamic();

    boolean isScriptable();

}
