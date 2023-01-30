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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;

/**
 * Text field allowing to specify reason.
 * 
 * @author Izabela Adamczyk
 */
public class ReasonField extends MultilineVarcharField
{
    public static final String ID_SUFFIX = "_reason";

    public ReasonField(IMessageProvider messageProvider, boolean mandatory)
    {
        super(messageProvider.getMessage(Dict.REASON), mandatory);
        setMaxLength(GenericConstants.DESCRIPTION_2000);
    }

    public ReasonField(IMessageProvider messageProvider, boolean mandatory, String parentId)
    {
        this(messageProvider, mandatory);
        setId(parentId + ID_SUFFIX);
    }

}
