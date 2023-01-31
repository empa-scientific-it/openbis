/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2;

/**
 * Read-only interface to an existing vocabulary term.
 * 
 * @author Jakub Straszewski
 */
public interface IVocabularyTermImmutable
{
    /**
     * Return the term's code.
     */
    String getCode();

    /**
     * Return the term's description.
     */
    String getDescription();

    /**
     * Return the term's label.
     */
    String getLabel();

    /**
     * Return an associated URL specifying additional information for the vocabulary term.
     */
    String getUrl();

    /**
     * Return the position of the term in the context of a vocabulary.
     */
    Long getOrdinal();

}
