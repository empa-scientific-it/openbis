/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Vocabulary term perm id.
 * 
 * @author pkupczyk
 */
@JsonObject("as.dto.vocabulary.id.VocabularyTermPermId")
public class VocabularyTermPermId implements IVocabularyTermId, Serializable
{

    private static final long serialVersionUID = 1L;

    private String code;

    private String vocabularyCode;

    /**
     * @param code Vocabulary term code, e.g. "MY_TERM".
     * @param vocabularyCode Vocabulary code, e.g. "MY_VOCABULARY"
     */
    public VocabularyTermPermId(String code, String vocabularyCode)
    {
        setCode(code != null ? code.toUpperCase() : null);
        setVocabularyCode(vocabularyCode != null ? vocabularyCode.toUpperCase() : null);
    }

    //
    // JSON-RPC
    //

    @SuppressWarnings("unused")
    private VocabularyTermPermId()
    {
        super();
    }

    public String getVocabularyCode()
    {
        return vocabularyCode;
    }

    private void setVocabularyCode(String vocabularyCode)
    {
        if (vocabularyCode == null)
        {
            throw new IllegalArgumentException("Vocabulary code cannot be null");
        }
        this.vocabularyCode = vocabularyCode;
    }

    public String getCode()
    {
        return code;
    }

    private void setCode(String code)
    {
        if (code == null)
        {
            throw new IllegalArgumentException("Code cannot be null");
        }
        this.code = code;
    }

    @JsonIgnore
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((vocabularyCode == null) ? 0 : vocabularyCode.hashCode());
        result = prime * result + ((code == null) ? 0 : code.hashCode());
        return result;
    }

    @JsonIgnore
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        VocabularyTermPermId other = (VocabularyTermPermId) obj;
        if (vocabularyCode == null)
        {
            if (other.vocabularyCode != null)
            {
                return false;
            }
        } else if (!vocabularyCode.equals(other.vocabularyCode))
        {
            return false;
        }
        if (code == null)
        {
            if (other.code != null)
            {
                return false;
            }
        } else if (!code.equals(other.code))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return getCode() + " (" + getVocabularyCode() + ")";
    }

}
