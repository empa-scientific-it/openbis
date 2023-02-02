/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.test;

import java.util.HashMap;
import java.util.Map;

import org.jmock.Mockery;

/**
 * A {@link Mockery} that tracks all created mocks and offers a method to retrieve them by name.
 * 
 * @author Kaloyan Enimanev
 */
public class TrackingMockery extends Mockery
{

    private Map<String, Object> createdMocks = new HashMap<String, Object>();

    @Override
    public <T> T mock(Class<T> typeToMock, String name)
    {
        T mock = super.mock(typeToMock, name);
        createdMocks.put(name, mock);
        return mock;
    }

    /**
     * retrieves a previously created mock object.
     */
    @SuppressWarnings("unchecked")
    public <T> T getMock(String name, Class<T> type)
    {
        return (T) createdMocks.get(name);
    }

}
