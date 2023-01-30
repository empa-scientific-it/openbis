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

package ch.systemsx.cisd.openbis.generic.server.jython.api.v1;

/**
 * Read-only interface to an existing Script.
 * 
 * @author Manuel Kohler
 */
public interface IScriptImmutable extends IAbstractType
{
    /**
     * Get the entity of the script. The values can be EXPERIMENT, SAMPLE, DATA_SET, MATERIAL.
     */
    public String getEntity();

    /**
     * Get the type of script. The values can be either DYNAMIC or MANAGED
     */
    public String getScriptType();

    /**
     * Get the description for this script.
     */
    @Override
    public String getDescription();

    /**
     * Get the name for this script.
     */
    public String getName();

    /**
     * Get the script itself.
     */
    public String getScript();

}
