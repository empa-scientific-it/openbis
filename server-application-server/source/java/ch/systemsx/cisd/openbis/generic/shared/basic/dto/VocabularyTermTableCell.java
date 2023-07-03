/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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

import java.util.List;
import java.util.ArrayList;

/**
 * Table cell for a {@link VocabularyTerm}.
 *
 * @author Piotr Buczek
 */
public class VocabularyTermTableCell implements ISerializableComparable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    //    private VocabularyTerm vocabularyTerm;
    private List<VocabularyTerm> vocabularyTerm = new ArrayList<VocabularyTerm>();

    public VocabularyTermTableCell(VocabularyTerm term)
    {
        if (term == null)
        {
            throw new IllegalArgumentException("Unspecified vocabulary term");
        }

        //        vocabularyTerm = term;
        vocabularyTerm.add(term);
    }

    @Override
    public int compareTo(ISerializableComparable o)
    {
        return getVocabularyTerm().toString().compareTo(o.toString());
    }

    //    public VocabularyTerm getVocabularyTerm()
    public List<VocabularyTerm> getVocabularyTerm()
    {
        return vocabularyTerm;
    }

    @Override
    public int hashCode()
    {
        return getVocabularyTerm().hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof VocabularyTermTableCell == false)
        {
            return false;
        }
        VocabularyTermTableCell other = (VocabularyTermTableCell) obj;
        return this.getVocabularyTerm().equals(other.getVocabularyTerm());
    }

    public void append(ISerializableComparable valueOrNull)
    {
        if (valueOrNull instanceof VocabularyTermTableCell)
        {
            VocabularyTermTableCell vt = (VocabularyTermTableCell) valueOrNull;
            vocabularyTerm.addAll(vt.vocabularyTerm);
        }
    }

    @Override
    public String toString()
    {
        return getVocabularyTerm().toString();
    }

    // ---------------------------

    // GWT only
    @SuppressWarnings("unused")
    private VocabularyTermTableCell()
    {
    }
}
