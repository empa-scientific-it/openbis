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

package ch.systemsx.cisd.openbis.uitest.type;

/**
 * @author anttil
 */
public enum ScriptType
{
    ENTITY_VALIDATOR("Entity Validator", "def validate(entity, isnew):\n   return true"),
    DYNAMIC_PROPERTY_EVALUATOR("Dynamic Property Evaluator", ""),
    MANAGED_PROPERTY_HANDLER("Managed Property Handler", "");

    private String label;

    private String dummyScript;

    private ScriptType(String label, String dummyScript)
    {
        this.label = label;
        this.dummyScript = dummyScript;
    }

    public String getDummyScript()
    {
        return dummyScript;
    }

    public String getLabel()
    {
        return label;
    }
}
