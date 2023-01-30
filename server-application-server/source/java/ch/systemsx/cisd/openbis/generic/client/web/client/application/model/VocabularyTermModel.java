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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.renderer.VocabularyPropertyColRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;

/**
 * A {@link ModelData} implementation for {@link VocabularyTerm}.
 * 
 * @author Izabela Adamczyk
 */
public class VocabularyTermModel extends SimplifiedBaseModel implements
        Comparable<VocabularyTermModel>
{
    private static final String ORDINAL = "ordinal";

    private static final String IS_OFFICIAL = "is_official";

    public static final String DISPLAY_FIELD = "display_field";

    private static final long serialVersionUID = 1L;

    public VocabularyTermModel(VocabularyTerm term)
    {
        set(ModelDataPropertyNames.CODE, term.getCode());
        set(ORDINAL, term.getOrdinal());
        set(IS_OFFICIAL, term.isOfficial());
        set(ModelDataPropertyNames.CODE_WITH_LABEL, term.getCodeOrLabel());
        set(DISPLAY_FIELD, generateDisplayField(term));
        set(ModelDataPropertyNames.TOOLTIP, VocabularyPropertyColRenderer.renderAsTooltip(term));
        set(ModelDataPropertyNames.OBJECT, term);
    }

    public String generateDisplayField(VocabularyTerm term)
    {
        final Element span = DOM.createSpan();
        if (false == term.isOfficial())
        {
            span.setAttribute("style", "color: grey; font-style:italic");
        }
        span.setInnerHTML(term.getCodeOrLabel());
        return DOM.toString(span);
    }

    public static final List<VocabularyTermModel> convert(List<VocabularyTerm> terms)
    {
        final ArrayList<VocabularyTermModel> list = new ArrayList<VocabularyTermModel>();
        for (VocabularyTerm t : terms)
        {
            list.add(new VocabularyTermModel(t));
        }
        Collections.sort(list);
        return list;
    }

    public VocabularyTerm getTerm()
    {
        return (VocabularyTerm) get(ModelDataPropertyNames.OBJECT);
    }

    //
    // Comparable
    //
    @Override
    public int compareTo(VocabularyTermModel o)
    {
        if (isOfficial() == o.isOfficial())
        {
            return getValueToCompare().compareTo(o.getValueToCompare());
        } else
        {
            return isOfficial() ? -1 : 1;
        }
    }

    /** @return value that will be used to compare Vocabulary Terms and display them in order */
    private Long getValueToCompare()
    {
        return get(ORDINAL);
    }

    private Boolean isOfficial()
    {
        return get(IS_OFFICIAL);
    }
}
