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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * A text area to specify data sets contained in a virtual data set container.
 * 
 * @author Piotr Buczek
 */
public final class DataSetsContainedArea extends DataSetsArea
{

    public static final String ID_SUFFIX_CONTAINED = "_contained";

    public DataSetsContainedArea(IMessageProvider messageProvider, String idPrefix)
    {
        super(messageProvider.getMessage(Dict.CONTAINED_DATA_SETS_EMPTY));
        this.setFieldLabel(messageProvider.getMessage(Dict.CONTAINED_DATA_SETS));
        setId(createId(idPrefix));
    }

    public static String createId(String idPrefix)
    {
        return idPrefix + ID_SUFFIX_CONTAINED;
    }

}
