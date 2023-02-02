/*
 * Copyright 2014 ETH Zuerich, CISD
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
import java.util.List;
import java.util.Map;

/**
 * Repository of {@link RecordingMatcher} instances. It delivers only one {@link RecordingMatcher} per class of objects to match and record.
 * 
 * @author Franz-Josef Elmer
 */
public class RecordingMatcherRepository
{
    private final Map<Class<?>, RecordingMatcher<?>> map =
            new HashMap<Class<?>, RecordingMatcher<?>>();

    /**
     * Returns the recording matcher for the specified class.
     */
    @SuppressWarnings(
    { "rawtypes", "unchecked" })
    public <T> RecordingMatcher<T> getRecordingMatcher(Class<T> clazz)
    {
        RecordingMatcher<?> matcher = map.get(clazz);
        if (matcher == null)
        {
            matcher = new RecordingMatcher();
            map.put(clazz, matcher);
        }
        return (RecordingMatcher<T>) matcher;
    }

    /**
     * Returns the recorded object of specified type. It assumes that only one object of the specified class has been matched and recorded.
     */
    public <T> T recordedObject(Class<T> clazz)
    {
        return getRecordingMatcher(clazz).recordedObject();
    }

    /**
     * Returns the list of matched and recorded objects of specified clazz.
     */
    public <T> List<T> getRecordedObjects(Class<T> clazz)
    {
        return getRecordingMatcher(clazz).getRecordedObjects();
    }

}