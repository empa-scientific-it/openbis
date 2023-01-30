/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.generic.server.task;

import java.util.Date;

/**
 * A time interval between two time stamps;
 * 
 * @author Franz-Josef Elmer
 */
final class Period
{
    private final Date from;

    private final Date until;

    Period(Date from, Date until)
    {
        this.from = from;
        this.until = until;
    }

    public Date getFrom()
    {
        return from;
    }

    public Date getUntil()
    {
        return until;
    }
}