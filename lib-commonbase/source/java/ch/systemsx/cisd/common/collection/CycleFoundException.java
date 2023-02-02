/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.systemsx.cisd.common.collection;

import java.util.Collection;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * @author pkupczyk
 */
@SuppressWarnings("rawtypes")
public class CycleFoundException extends UserFailureException
{

    private static final long serialVersionUID = 1L;

    private Object cycleRoot;

    private Collection cycle;

    public CycleFoundException(Object cycleRoot, Collection cycle)
    {
        super("" + cycleRoot + " depends on itself. Dependency chain : " + cycleRoot + " -> " + cycle);
        this.cycleRoot = cycleRoot;
        this.cycle = cycle;
    }

    public Object getCycleRoot()
    {
        return cycleRoot;
    }

    public Collection getCycle()
    {
        return cycle;
    }

}
