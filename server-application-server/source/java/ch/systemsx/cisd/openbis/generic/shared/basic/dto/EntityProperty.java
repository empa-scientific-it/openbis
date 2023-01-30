/*
 * Copyright 2008 ETH Zuerich, CISD
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
 * The generic entity property.
 * 
 * @author Christian Ribeaud
 */
public class EntityProperty extends GenericEntityProperty
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private VocabularyTerm vocabularyTermOrNull;

    private Material materialOrNull;

    @Override
    public Material getMaterial()
    {
        return materialOrNull;
    }

    @Override
    public void setMaterial(Material material)
    {
        this.materialOrNull = material;
    }

    @Override
    public VocabularyTerm getVocabularyTerm()
    {
        return vocabularyTermOrNull;
    }

    @Override
    public void setVocabularyTerm(VocabularyTerm vocabularyTerm)
    {
        this.vocabularyTermOrNull = vocabularyTerm;
    }

}
