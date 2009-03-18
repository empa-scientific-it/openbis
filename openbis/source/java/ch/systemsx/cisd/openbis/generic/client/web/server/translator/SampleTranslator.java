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

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * A {@link Sample} &lt;---&gt; {@link SamplePE} translator.
 * 
 * @author Franz-Josef Elmer
 */
public final class SampleTranslator
{
    private SampleTranslator()
    {
        // Can not be instantiated.
    }

    public final static Sample translate(final SamplePE samplePE)
    {
        if (samplePE == null)
        {
            return null;
        }
        final int containerDep = samplePE.getSampleType().getContainerHierarchyDepth();
        final int generatedFromDep = samplePE.getSampleType().getGeneratedFromHierarchyDepth();
        return translate(samplePE, containerDep, generatedFromDep, true);

    }

    private final static Sample translate(final SamplePE samplePE, final int containerDep,
            final int generatedFromDep, final boolean withDetails)
    {
        final Sample result = new Sample();
        result.setCode(StringEscapeUtils.escapeHtml(samplePE.getCode()));
        result.setModificationDate(samplePE.getModificationDate());
        result.setId(samplePE.getId());
        result.setIdentifier(StringEscapeUtils.escapeHtml(samplePE.getSampleIdentifier().toString()));
        if (withDetails)
        {
            result.setSampleType(SampleTypeTranslator.translate(samplePE.getSampleType()));
            result.setGroup(GroupTranslator.translate(samplePE.getGroup()));
            result.setDatabaseInstance(DatabaseInstanceTranslator.translate(samplePE
                    .getDatabaseInstance()));
            result.setRegistrator(PersonTranslator.translate(samplePE.getRegistrator()));
            result.setRegistrationDate(samplePE.getRegistrationDate());
            result.setProperties(SamplePropertyTranslator.translate(samplePE.getProperties()));
            result.setValidProcedure(ProcedureTranslator.translate(samplePE.getValidProcedure()));
        }
        if (containerDep > 0 && samplePE.getContainer() != null)
        {
            result.setContainer(SampleTranslator.translate(samplePE.getContainer(),
                    containerDep - 1, 0, false));
        }
        if (generatedFromDep > 0 && samplePE.getGeneratedFrom() != null)
        {
            result.setGeneratedFrom(SampleTranslator.translate(samplePE.getGeneratedFrom(), 0,
                    generatedFromDep - 1, false));
        }
        result.setInvalidation(InvalidationTranslator.translate(samplePE.getInvalidation()));
        return result;
    }

}
