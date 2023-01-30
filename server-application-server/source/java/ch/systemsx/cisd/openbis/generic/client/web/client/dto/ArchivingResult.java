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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Archiving operations are based on data set archiving status. This class returns to the client information needed to create a message after set of
 * data sets have been scheduled for such an operation on the server side.
 * 
 * @author Piotr Buczek
 */
public class ArchivingResult implements IsSerializable
{
    private int provided;

    private int scheduled;

    public ArchivingResult()
    {
    }

    public ArchivingResult(final int provided, final int scheduled)
    {
        this.provided = provided;
        this.scheduled = scheduled;
    }

    public int getProvided()
    {
        return provided;
    }

    public void setProvided(int provided)
    {
        this.provided = provided;
    }

    public int getScheduled()
    {
        return scheduled;
    }

    public void setScheduled(int scheduled)
    {
        this.scheduled = scheduled;
    }

}
