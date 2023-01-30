/*
 * Copyright 2011 ETH Zuerich, CISD
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

import java.util.ArrayList;
import java.util.List;

import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;

/**
 * Utility methods used in the ***ProviderTest classes.
 * 
 * @author Franz-Josef Elmer
 * @author Kaloyan Enimanev
 */
public abstract class AbstractProviderTest extends AssertJUnit
{

    protected static final String SESSION_TOKEN = "token";

    protected Mockery context;

    protected ICommonServer server;

    @BeforeMethod
    public final void setUp()
    {
        context = new Mockery();
        server = context.mock(ICommonServer.class);
    }

    @AfterMethod
    public final void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    protected List<String> getHeaderIDs(TypedTableModel<?> tableModel)
    {
        List<String> result = new ArrayList<String>();
        List<TableModelColumnHeader> headers = tableModel.getHeader();
        for (TableModelColumnHeader header : headers)
        {
            result.add(header.getId());
        }
        return result;
    }

    protected List<DataTypeCode> getHeaderDataTypes(TypedTableModel<?> tableModel)
    {
        List<DataTypeCode> result = new ArrayList<DataTypeCode>();
        List<TableModelColumnHeader> headers = tableModel.getHeader();
        for (TableModelColumnHeader header : headers)
        {
            result.add(header.getDataType());
        }
        return result;
    }

    protected List<EntityKind> getHeaderEntityKinds(TypedTableModel<?> tableModel)
    {
        List<EntityKind> result = new ArrayList<EntityKind>();
        List<TableModelColumnHeader> headers = tableModel.getHeader();
        for (TableModelColumnHeader header : headers)
        {
            result.add(header.tryGetEntityKind());
        }
        return result;
    }

}
