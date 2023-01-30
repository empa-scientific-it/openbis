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

package ch.systemsx.cisd.openbis.generic.client.web.client.testframework;

/**
 * Interface for preparing expectation on a property checking class. It uses a fluent API approach for its methods to prepare expectations.
 *
 * @author Franz-Josef Elmer
 */
public interface IPropertyChecker<C extends IPropertyChecker<?>>
{
    /**
     * Creates an expectation for the property of specified name.
     */
    public IProperty<C> property(String name);

    /**
     * Adds an expectation of a property with specified name and specified assertion object which checks the property value.
     * 
     * @return this checker.
     */
    public C property(String name, IValueAssertion<?> valueAssertion);
}
