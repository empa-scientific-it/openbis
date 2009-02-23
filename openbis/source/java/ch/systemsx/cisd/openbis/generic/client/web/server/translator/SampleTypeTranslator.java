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

import org.apache.commons.lang.StringEscapeUtils;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;

/**
 * A {@link SampleType} &lt;---&gt; {@link SampleTypePE} translator.
 * 
 * @author Franz-Josef Elmer
 */
public class SampleTypeTranslator
{
    private SampleTypeTranslator()
    {
        // Can not be instantiated.
    }

    public static SampleType translate(final SampleTypePE sampleTypePE)
    {
        final SampleType result = new SampleType();
        result.setCode(sampleTypePE.getCode());
        result.setListable(sampleTypePE.isListable());
        result.setDescription(StringEscapeUtils.escapeHtml(sampleTypePE.getDescription()));
        result.setGeneratedFromHierarchyDepth(sampleTypePE.getGeneratedFromHierarchyDepth());
        result.setPartOfHierarchyDepth(sampleTypePE.getContainerHierarchyDepth());
        result.setSampleTypePropertyTypes(SampleTypePropertyTypeTranslator.translate(sampleTypePE
                .getSampleTypePropertyTypes(), result));
        result.setDatabaseInstance(DatabaseInstanceTranslator.translate(sampleTypePE
                .getDatabaseInstance()));
        return result;

    }

    public static SampleTypePE translate(final SampleType sampleType)
    {
        final SampleTypePE result = new SampleTypePE();
        result.setCode(sampleType.getCode());
        return result;
    }
}
