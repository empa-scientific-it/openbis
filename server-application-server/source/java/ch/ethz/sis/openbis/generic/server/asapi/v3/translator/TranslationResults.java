/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator;

import java.util.HashMap;
import java.util.Map;

import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectHolder;

/**
 * @author pkupczyk
 */
@SuppressWarnings({ "unchecked" })
public class TranslationResults
{

    private Map<Class<?>, Object> resultMap = new HashMap<Class<?>, Object>();

    public <I, O, F> void put(Class<? extends ITranslator<I, ObjectHolder<O>, F>> translatorClass, Map<I, ObjectHolder<O>> result)
    {
        resultMap.put(translatorClass, result);
    }

    public <I, O, F> O get(Class<? extends ITranslator<I, ObjectHolder<O>, F>> translatorClass, I input)
    {
        Map<I, ObjectHolder<O>> result = (Map<I, ObjectHolder<O>>) resultMap.get(translatorClass);
        ObjectHolder<O> holder = result.get(input);
        if (holder != null)
        {
            return holder.getObject();
        } else
        {
            return null;
        }
    }

}
