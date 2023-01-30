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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import com.extjs.gxt.ui.client.widget.MessageBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetHolder;

public class ShowResultSetCutInfo<T extends IResultSetHolder<?>> implements IOnSuccessAction<T>
{

    private final IViewContext<?> viewContext;

    public ShowResultSetCutInfo(IViewContext<?> viewContext)
    {
        this.viewContext = viewContext;
    }

    @Override
    public void execute(T result)
    {
        if (result != null
                && result.getResultSet().getTotalLength() == viewContext.getModel()
                        .getApplicationInfo().getMaxResults())
        {

            MessageBox.info(viewContext.getMessage(Dict.MORE_RESULTS_FOUND_TITLE),
                    viewContext.getMessage(Dict.MORE_RESULTS_FOUND_MESSAGE), null);

        }
    }
}
