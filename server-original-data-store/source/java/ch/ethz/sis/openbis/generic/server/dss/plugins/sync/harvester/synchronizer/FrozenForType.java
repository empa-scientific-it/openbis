/*
 * Copyright 2019 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer;

import java.lang.reflect.Field;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;

/**
 * @author Franz-Josef Elmer
 */
public enum FrozenForType
{
    CHILDREN("frozenForChildren"),
    COMPONENTS("frozenForComponents"),
    CONTAINERS("frozenForContainers"),
    DATA_SETS("frozenForDataSets"),
    EXPERIMENTS("frozenForExperiments"),
    PARENTS("frozenForParents"),
    PROJECTS("frozenForProjects"),
    SAMPLES("frozenForSamples");
    
    private String attributeName;
    private Field field;

    private FrozenForType(String attributeName)
    {
        this.attributeName = attributeName;
        try
        {
            field = FrozenFlags.class.getField(attributeName);
        } catch (Exception e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        }
    }

    public String getAttributeName()
    {
        return attributeName;
    }

    public void setFlag(FrozenFlags frozenFlags, boolean value)
    {
        try
        {
            field.set(frozenFlags, value);
        } catch (Exception e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        }
    }

}
