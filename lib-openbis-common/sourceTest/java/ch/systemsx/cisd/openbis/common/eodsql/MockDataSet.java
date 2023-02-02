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

package ch.systemsx.cisd.openbis.common.eodsql;

import java.util.ArrayList;

import net.lemnik.eodsql.DataSet;

/**
 * @author Franz-Josef Elmer
 */
public class MockDataSet<T> extends ArrayList<T> implements DataSet<T>
{
    private static final long serialVersionUID = 1L;

    private boolean closeInvoked;

    public final boolean hasCloseBeenInvoked()
    {
        return closeInvoked;
    }

    @Override
    public void close()
    {
        closeInvoked = true;
    }

    @Override
    public void disconnect()
    {
    }

    @Override
    public boolean isConnected()
    {
        return false;
    }

}
