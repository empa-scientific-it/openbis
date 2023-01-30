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

package ch.systemsx.cisd.openbis.generic.server.jython.api.v1;

/**
 * @author Kaloyan Enimanev
 */
public interface IVocabulary extends IVocabularyImmutable
{

    /**
     * Sets the vocabulary description.
     */
    public void setDescription(String description);

    /**
     * Set to <code>true</code> if the vocabulary is managed internally in openBIS.
     */
    void setManagedInternally(boolean isManagedInternally);

    /**
     * Set to <code>true</code> if the vocabulary is in the internal openBIS namespace.
     */
    void setInternalNamespace(boolean isInternalNamespace);

    void setChosenFromList(boolean isChosenFromList);

    /**
     * Sets a URL template (e.g a search query) that can display additional information for the concrete vocabulary terms.
     */
    void setUrlTemplate(String urlTemplate);

    /**
     * adds a vocabulary term to the dictionary.
     */
    void addTerm(IVocabularyTerm term);

}
