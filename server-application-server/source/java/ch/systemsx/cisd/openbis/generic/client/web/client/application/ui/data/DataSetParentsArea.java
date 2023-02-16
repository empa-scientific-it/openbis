/*
 * Copyright ETH 2009 - 2023 Zürich, Scientific IT Services
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
 * A text area to specify parents of a data set.
 * 
 * @author Piotr Buczek
 */
public final class DataSetParentsArea extends DataSetsArea
{

    public static final String ID_SUFFIX_PARENTS = "_parents";

    public DataSetParentsArea(IMessageProvider messageProvider, String idPrefix)
    {
        super(messageProvider.getMessage(Dict.PARENTS_EMPTY));
        this.setFieldLabel(messageProvider.getMessage(Dict.PARENTS));
        setId(createId(idPrefix));
    }

    public static String createId(String idPrefix)
    {
        return idPrefix + ID_SUFFIX_PARENTS;
    }

}
