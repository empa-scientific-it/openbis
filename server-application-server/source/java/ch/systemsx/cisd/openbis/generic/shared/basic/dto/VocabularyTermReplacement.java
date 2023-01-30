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

import java.io.Serializable;

/**
 * Wrapper of a {@link VocabularyTerm} and its replacement.
 * 
 * @author Franz-Josef Elmer
 */
public class VocabularyTermReplacement implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private VocabularyTerm term;

    private String replacementCode;

    public final VocabularyTerm getTerm()
    {
        return term;
    }

    public final void setTerm(VocabularyTerm term)
    {
        this.term = term;
    }

    public final String getReplacementCode()
    {
        return replacementCode;
    }

    public final void setReplacementCode(String replacementCode)
    {
        this.replacementCode = replacementCode;
    }

    @Override
    public String toString()
    {
        return term + " -> [" + replacementCode + "]";
    }
}
