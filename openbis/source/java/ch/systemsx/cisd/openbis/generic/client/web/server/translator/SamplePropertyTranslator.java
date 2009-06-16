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

package ch.systemsx.cisd.openbis.generic.client.web.server.translator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePropertyTypePE;

/**
 * A {@link SampleProperty} &lt;---&gt; {@link SamplePropertyPE} translator.
 * 
 * @author Franz-Josef Elmer
 */
public final class SamplePropertyTranslator
{
    private SamplePropertyTranslator()
    {
        // Can not be instantiated.
    }

    public final static SampleProperty translate(final SamplePropertyPE samplePropertyPE)
    {
        final SampleProperty result = new SampleProperty();
        result.setValue(StringEscapeUtils.escapeHtml(samplePropertyPE.tryGetUntypedValue()));
        result.setVocabularyTerm(VocabularyTermTranslator.translate(samplePropertyPE
                .getVocabularyTerm()));
        result
                .setEntityTypePropertyType(SampleTypePropertyTypeTranslator
                        .translate((SampleTypePropertyTypePE) samplePropertyPE
                                .getEntityTypePropertyType()));
        return result;

    }

    public final static List<SampleProperty> translate(final Set<SamplePropertyPE> set)
    {
        if (set == null)
        {
            return null;
        }
        final List<SampleProperty> result = new ArrayList<SampleProperty>();
        for (final SamplePropertyPE samplePropertyPE : set)
        {
            result.add(translate(samplePropertyPE));
        }
        return result;
    }
}
