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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import java.lang.reflect.Method;

/**
 * Wrapper class for TestNG DataProviders. This is needed because in TestNG a test with an argument of type Method isn't handled
 * as a value from a DataProvider.
 * 
 * @author Franz-Josef Elmer
 *
 */
public class MethodWrapper
{
    public Method method;

    public MethodWrapper(Method method)
    {
        this.method = method;
    }

    @Override
    public String toString()
    {
        return method.getName();
    }
    
}