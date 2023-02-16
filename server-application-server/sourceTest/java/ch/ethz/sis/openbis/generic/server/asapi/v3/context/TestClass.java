/*
 * Copyright ETH 2016 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.context;

import java.util.Collection;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.test.context.TestClass")
public class TestClass
{

    public int primitive;

    public String string;

    public Integer integer;

    public TestClass object;

    public FieldUpdateValue<String> fieldUpdateValue;

    public ListUpdateValue<String, String, String, String> listUpdateValue;

    public int[] primitiveArray;

    public String[] stringArray;

    public Integer[] integerArray;

    public TestClass[] objectArray;

    public Collection<String> stringCollection;

    public Collection<Integer> integerCollection;

    public Collection<TestClass> objectCollection;

    public Map<String, String> stringMap;

    public Map<String, Integer> integerMap;

    public Map<String, TestClass> objectMap;

    @Override
    public String toString()
    {
        throw new UnsupportedOperationException();
    }
}
