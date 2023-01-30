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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample;

import java.util.Collection;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.MultilineItemsField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.IdentifierExtractor;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * {@link MultilineItemsField} extension to specify parent samples.
 * 
 * @author Piotr Buczek
 */
final class ParentSamplesArea extends MultilineItemsField
{

    public static final String ID_SUFFIX_PARENT_SAMPLES = "_parent_samples";

    public ParentSamplesArea(IMessageProvider messageProvider, String idPrefix)
    {
        super("", false);
        setFieldLabel(messageProvider.getMessage(Dict.PARENTS));
        setEmptyText(messageProvider.getMessage(Dict.SAMPLES_LIST));
        setId(createId(idPrefix));
    }

    public static String createId(String idPrefix)
    {
        return idPrefix + ID_SUFFIX_PARENT_SAMPLES;
    }

    public void setSamples(Collection<Sample> samples)
    {
        List<String> identifiers = IdentifierExtractor.extract(samples);
        setSamples(identifiers);
    }

    // delegation to superclass methods

    // null if the area has not been modified,
    // the list of all sample codes otherwise
    public final String[] tryGetSamples()
    {
        return tryGetModifiedItemList();
    }

    public final void setSamples(List<String> codesOrIdentifiers)
    {
        setItems(codesOrIdentifiers);
    }

}
