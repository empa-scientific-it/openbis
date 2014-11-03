/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.translator;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author pkupczyk
 */
public abstract class AbstractTranslator<I, O> implements ITranslator<I, O>
{

    @Override
    public O translate(I object)
    {
        if (object == null)
        {
            return null;
        }

        return doTranslate(object);
    }

    @Override
    public final Map<I, O> translate(Collection<I> objects)
    {
        if (objects == null)
        {
            return null;
        }

        return doTranslate(objects);
    }

    protected Map<I, O> doTranslate(Collection<I> objects)
    {
        Map<I, O> translatedMap = new LinkedHashMap<I, O>();
        for (I object : objects)
        {
            O translated = doTranslate(object);
            if (translated != null)
            {
                translatedMap.put(object, translated);
            }
        }
        return translatedMap;
    }

    protected abstract O doTranslate(I object);

}
