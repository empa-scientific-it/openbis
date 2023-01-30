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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import java.util.Collection;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.MultilineItemsField;
import ch.systemsx.cisd.openbis.generic.shared.basic.ICodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;

/**
 * A text area to specify list of codes. Codes are specified separated by commas, spaces or new lines.
 * 
 * @author Piotr Buczek
 */
abstract public class CodesArea<T extends ICodeHolder> extends MultilineItemsField
{
    public CodesArea(String emptyTextMsg)
    {
        super("", false);
        setEmptyText(emptyTextMsg);
    }

    public final void setCodeProviders(Collection<T> codeProviders)
    {
        List<String> codes = Code.extractCodes(codeProviders);
        setItems(codes);
    }

}
