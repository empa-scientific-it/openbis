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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.model.renderer;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.AbstractPropertyColDef;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.MultilineHTML;

/**
 * An {@link AbstractPropertyColRenderer} which renders value preserving newlines.
 * 
 * @author Piotr Buczek
 */
class MultilineVarcharPropertyColRenderer<T> extends AbstractPropertyColRenderer<T>
{

    public MultilineVarcharPropertyColRenderer(AbstractPropertyColDef<T> colDef)
    {
        super(colDef);
    }

    @Override
    protected String renderValue(String value)
    {
        return (new MultilineHTML(value)).toString();
    }

}
