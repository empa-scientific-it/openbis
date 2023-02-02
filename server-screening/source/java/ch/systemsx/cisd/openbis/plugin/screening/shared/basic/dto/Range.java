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

package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto;

import java.io.Serializable;

import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.IRangeType;

/**
 * Definition of a range of two float values. Note, that <code>from</code> can be greater than <code>until</code>.
 *
 * @author Franz-Josef Elmer
 */
public class Range implements Serializable, IRangeType
{
    private static final long serialVersionUID = 1L;

    private float from;

    private float until;

    // GWT only
    @SuppressWarnings("unused")
    private Range()
    {
    }

    public Range(float from, float until)
    {
        this.from = from;
        this.until = until;
    }

    public float getFrom()
    {
        return from;
    }

    public float getUntil()
    {
        return until;
    }

}