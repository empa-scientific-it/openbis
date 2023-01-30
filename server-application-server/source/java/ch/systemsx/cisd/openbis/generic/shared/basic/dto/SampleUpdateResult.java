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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;
import java.util.List;

/**
 * Stores result of sample update.
 * 
 * @author Piotr Buczek
 */
public class SampleUpdateResult implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private int version;

    private List<String> parents;

    public SampleUpdateResult()
    {
    }

    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    public List<String> getParents()
    {
        return parents;
    }

    public void setParents(List<String> parents)
    {
        this.parents = parents;
    }

    public void copyFrom(SampleUpdateResult result)
    {
        setParents(result.getParents());
        setVersion(result.getVersion());
    }

}
