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
public interface IVocabularyTerm extends IVocabularyTermImmutable
{
    /**
     * Set the description of the vocabulary term.
     */
    void setDescription(String description);

    /**
     * Set the label of the vocabulary term.
     */
    void setLabel(String label);

    /**
     * Set position of the term in the context of its vocabulary.
     */
    void setOrdinal(Long ordinal);
}
