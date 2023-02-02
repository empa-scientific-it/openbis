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

package ch.systemsx.cisd.common.jython;

/**
 * @author pkupczyk
 */
public class JythonScript
{

    private String script;

    public JythonScript(String script)
    {
        this.script = script;
    }

    public String[] getLines()
    {
        if (script == null)
        {
            return new String[] {};
        } else
        {
            return script.split("\n", -1);
        }
    }

}
