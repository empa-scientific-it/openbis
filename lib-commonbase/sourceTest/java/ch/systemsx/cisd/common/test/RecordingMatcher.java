/*
 * Copyright 2010 ETH Zuerich, CISD
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

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

/**
 * A {@link Matcher} which always matches the objects to be matched and make them available.
 * 
 * @author Franz-Josef Elmer
 */
public class RecordingMatcher<T> extends BaseMatcher<T>
{

    private List<T> objects = new ArrayList<T>();

    /** convenience method for creating recording matcher of required type (depending on context) */
    public static <T> RecordingMatcher<T> create()
    {
        return new RecordingMatcher<T>();
    }

    /**
     * Removes all recored objects.
     */
    public void reset()
    {
        objects.clear();
    }

    /**
     * Returns the one recorded object. Fails if not exactly one object was recorded.
     */
    public T recordedObject()
    {
        assert objects.size() == 1 : "expected one recorded object, found " + objects.size();
        return objects.get(0);
    }

    /**
     * Returns the objects in the order they have been recorded.
     */
    public List<T> getRecordedObjects()
    {
        return objects;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean matches(Object item)
    {
        objects.add((T) item);
        return true;
    }

    @Override
    public void describeTo(Description description)
    {
        description.appendText("<recording matcher>");
    }

}
