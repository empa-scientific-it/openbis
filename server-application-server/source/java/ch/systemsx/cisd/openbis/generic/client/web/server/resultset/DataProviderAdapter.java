/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;

/**
 * Adapter which turns a {@link ITableModelProvider} into a {@link IOriginalDataProvider}.
 * 
 * @author Franz-Josef Elmer
 */
public final class DataProviderAdapter<T extends Serializable> implements
        IOriginalDataProvider<TableModelRowWithObject<T>>
{
    private final ITableModelProvider<T> provider;

    private TypedTableModel<T> tableModel;

    public DataProviderAdapter(ITableModelProvider<T> provider)
    {
        this.provider = provider;
    }

    @Override
    public List<TableModelRowWithObject<T>> getOriginalData(int maxSize)
            throws UserFailureException
    {
        tableModel = provider.getTableModel(maxSize);
        return tableModel.getRows();
    }

    @Override
    public List<TableModelColumnHeader> getHeaders()
    {
        return tableModel == null ? Collections.<TableModelColumnHeader> emptyList() : tableModel
                .getHeader();
    }

}
