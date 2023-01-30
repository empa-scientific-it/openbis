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

import java.util.List;

/**
 * Read-only interface to a vocabulary.
 * 
 * @author Kaloyan Enimanev
 */
public interface IVocabularyImmutable
{

    /**
     * Returns the vocabulary code.
     */
    public String getCode();

    /**
     * Returns the vocabulary description.
     */
    public String getDescription();

    /**
     * Return <code>true</code> if the vocabulary is managed internally in openBIS.
     */
    boolean isManagedInternally();

    /**
     * Return <code>true</code> if the vocabulary is in the internal openBIS namespace.
     */
    boolean isInternalNamespace();

    boolean isChosenFromList();

    /**
     * Returns a URL template (e.g a search query) that can display additional information for the concrete vocabulary terms. Can return null.
     */
    String getUrlTemplate();

    /**
     * Return a list with all terms within the vocabulary.
     */
    List<IVocabularyTermImmutable> getTerms();

}
