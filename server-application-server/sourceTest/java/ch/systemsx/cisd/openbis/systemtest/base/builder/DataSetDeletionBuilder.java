/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest.base.builder;

import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.server.ICommonServerForInternalUse;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletionType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;

/**
 * A builder that puts a data set into the trash.
 * 
 * @author cramakri
 */
public class DataSetDeletionBuilder extends UpdateBuilder<List<String>>
{
    private String dataSetCode;

    public DataSetDeletionBuilder(ICommonServerForInternalUse commonServer,
            IGenericServer genericServer, AbstractExternalData data)
    {
        super(commonServer, genericServer);
        this.dataSetCode = data.getCode();
    }

    @Override
    public List<String> create()
    {
        return Collections.singletonList(dataSetCode);
    }

    @Override
    public void perform()
    {
        commonServer.deleteDataSets(this.sessionToken, this.create(), "Test", DeletionType.TRASH,
                true);
    }
}