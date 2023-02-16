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
package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample;

import java.util.Collection;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.CodesArea;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * Abstract {@link CodesArea} extension for samples introducing methods with convenient names.
 * 
 * @author Piotr Buczek
 */
abstract public class SamplesArea extends CodesArea<Sample>
{
    public SamplesArea(String emptyTextMsg)
    {
        super(emptyTextMsg);
    }

    // delegation to abstract class methods

    /**
     * @see #tryGetModifiedItemList()
     */
    public final String[] tryGetModifiedSampleCodes()
    {
        return tryGetModifiedItemList();
    }

    public void setSamples(Collection<Sample> samples)
    {
        setCodeProviders(samples);
    }

    public final void setSampleCodes(List<String> codes)
    {
        setItems(codes);
    }

    public final void setSampleIdentifiers(List<String> identifiers)
    {
        setItems(identifiers);
    }
}
