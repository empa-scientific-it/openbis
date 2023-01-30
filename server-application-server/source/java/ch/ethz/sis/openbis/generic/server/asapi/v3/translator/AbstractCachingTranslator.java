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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationCache.CacheEntry;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationCache.CacheKey;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * @author pkupczyk
 */
public abstract class AbstractCachingTranslator<I, O, F extends FetchOptions<?>> extends AbstractTranslator<I, O, F>
{

    private final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, getClass());

    private final String namespace = getClass().getName();

    @Override
    protected final O doTranslate(TranslationContext context, I object, F fetchOptions)
    {
        Map<I, O> translated = doTranslate(context, Collections.singleton(object), fetchOptions);
        if (translated.isEmpty())
        {
            return null;
        } else
        {
            return translated.get(object);
        }
    }

    @Override
    protected final Map<I, O> doTranslate(TranslationContext context, Collection<I> allInputs, F fetchOptions)
    {
        Map<I, O> translated = new LinkedHashMap<I, O>();
        Map<I, O> updated = new LinkedHashMap<I, O>();
        TranslationCache cache = context.getTranslationCache();

        Collection<I> inputs = doShouldTranslate(context, allInputs, fetchOptions);

        for (I input : inputs)
        {
            CacheEntry cacheEntry = cache.getEntry(getObjectCacheKey(input, fetchOptions));

            if (cacheEntry.isTranslatedObjectSet())
            {
                handleAlreadyTranslatedInput(context, input, translated, updated, fetchOptions);
            } else
            {
                handleNewInput(context, input, translated, updated, fetchOptions);
            }
        }

        if (false == updated.isEmpty())
        {
            Object relations = getObjectsRelations(context, updated.keySet(), fetchOptions);

            for (Map.Entry<I, O> updatedEntry : updated.entrySet())
            {
                updateObject(context, updatedEntry.getKey(), updatedEntry.getValue(), relations, fetchOptions);
            }
        }

        filterTranslated(context, translated);
        postTranslate(context, translated);

        return translated;
    }

    @SuppressWarnings("unchecked")
    private final void handleAlreadyTranslatedInput(TranslationContext context, I input, Map<I, O> translated, Map<I, O> updated, F fetchOptions)
    {
        TranslationCache cache = context.getTranslationCache();
        CacheEntry cacheEntry = cache.getEntry(getObjectCacheKey(input, fetchOptions));
        O output = (O) cacheEntry.getTranslatedObject();

        if (output == null)
        {
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug("Found that object was already rejected from translation: " + getObjectId(input));
            }
        } else
        {
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug("Found in cache: " + output.getClass() + " with id: " + getObjectId(input));
            }

            translated.put(input, output);
        }
    }

    private void handleNewInput(TranslationContext context, I input, Map<I, O> translated, Map<I, O> updated, F fetchOptions)
    {
        O output = createObject(context, input, fetchOptions);

        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Created: " + output.getClass() + " with id: " + getObjectId(input));
        }

        TranslationCache cache = context.getTranslationCache();
        CacheEntry cacheEntry = cache.getEntry(getObjectCacheKey(input, fetchOptions));
        cacheEntry.setTranslatedObject(output);

        updated.put(input, output);
        translated.put(input, output);

        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Updating created: " + output.getClass() + " with id: " + getObjectId(input));
        }
    }

    protected Object getObjectId(I input)
    {
        if (input instanceof IIdHolder)
        {
            return HibernateUtils.getId((IIdHolder) input);
        } else if (input instanceof Long)
        {
            return (Long) input;
        } else
        {
            final String message = (input != null) ? "Unsupported input type: " + input.getClass()
                    : "Null input is unsupported.";
            throw new IllegalArgumentException(message);
        }
    }

    protected CacheKey getObjectCacheKey(I input, F fetchOptions)
    {
        return new TranslationCache.CacheKey(namespace, getObjectId(input), fetchOptions);
    }

    private final Collection<I> doShouldTranslate(TranslationContext context, Collection<I> inputs, F fetchOptions)
    {
        TranslationCache cache = context.getTranslationCache();
        Collection<I> toCheck = new LinkedHashSet<I>();
        Collection<I> toTranslate = new LinkedHashSet<I>();

        for (I input : inputs)
        {
            CacheEntry cacheEntry = cache.getEntry(getObjectCacheKey(input, fetchOptions));

            if (cacheEntry.isShouldTranslateSet())
            {
                boolean should = cacheEntry.getShouldTranslate();

                if (should)
                {
                    toTranslate.add(input);
                }

                if (operationLog.isDebugEnabled())
                {
                    Object id = getObjectId(input);

                    if (should)
                    {
                        operationLog.debug("Found in cache that object with id: " + id + " should be translated");
                    } else
                    {
                        operationLog.debug("Found in cache that object with id: " + id + " should NOT be translated");
                    }
                }
            } else
            {
                toCheck.add(input);
            }
        }

        Collection<I> checked = shouldTranslate(context, toCheck, fetchOptions);
        toTranslate.addAll(checked);

        for (I input : checked)
        {
            CacheEntry cacheEntry = cache.getEntry(getObjectCacheKey(input, fetchOptions));
            cacheEntry.setShouldTranslate(true);

            if (operationLog.isDebugEnabled())
            {
                operationLog.debug("Should translate object with id: " + getObjectId(input));
            }
        }

        toCheck.removeAll(checked);

        for (I input : toCheck)
        {
            CacheEntry cacheEntry = cache.getEntry(getObjectCacheKey(input, fetchOptions));
            cacheEntry.setShouldTranslate(false);

            if (operationLog.isDebugEnabled())
            {
                operationLog.debug("Should NOT translate object with id: " + getObjectId(input));
            }
        }

        return toTranslate;
    }

    /**
     * Override this method if you want to conditionally skip translation (e.g. when the input object is not visible for a user the translation is
     * performed for)
     */
    protected Set<I> shouldTranslate(TranslationContext context, Collection<I> inputs, F fetchOptions)
    {
        Set<I> result = new LinkedHashSet<I>();

        for (I input : inputs)
        {
            if (shouldTranslate(context, input, fetchOptions))
            {
                result.add(input);
            }
        }

        return result;
    }

    /**
     * Override this method if you want to conditionally skip translation (e.g. when the input object is not visible for a user the translation is
     * performed for)
     */
    protected boolean shouldTranslate(TranslationContext context, I input, F fetchOptions)
    {
        return true;
    }

    /**
     * Override this method if you want to filter out some translated objects (e.g. because they should not be visible to a user the translation is
     * performed for). Because of performance overhead related with unnecessary translation do use this method only when the number of objects is
     * small. In other cases do use shouldTranslate instead.
     */
    protected void filterTranslated(TranslationContext context, Map<I, O> translated)
    {

    }

    protected void postTranslate(TranslationContext context, Map<I, O> translated)
    {
    }

    /**
     * Implementation of this method should create a translated version of the input object. Only basic attributes of the input object should be
     * translated here. Parts that have a corresponding fetch option should be translated in the
     * {@link AbstractCachingTranslator#updateObject(TranslationContext, Object, Object, Object, FetchOptions)} method.
     */
    protected abstract O createObject(TranslationContext context, I input, F fetchOptions);

    /**
     * Override this method if you want to fetch related objects for all the inputs at once. This way you can greatly improve the performance of the
     * translation.
     */
    protected Object getObjectsRelations(TranslationContext context, Collection<I> inputs, F fetchOptions)
    {
        return new Object();
    }

    /**
     * Implementation of this method should update the translated version of the input object to meet the fetch options.
     */
    protected abstract void updateObject(TranslationContext context, I input, O output, Object relations, F fetchOptions);

}
