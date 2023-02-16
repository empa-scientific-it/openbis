/*
 * Copyright ETH 2014 - 2023 Zürich, Scientific IT Services
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

import org.apache.commons.lang3.builder.EqualsBuilder;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;

public class TranslationCache
{

    private Map<CacheKey, CacheEntry> entries = new HashMap<CacheKey, CacheEntry>();

    public CacheEntry getEntry(CacheKey cacheKey)
    {
        CacheEntry cacheEntry = entries.get(cacheKey);

        if (cacheEntry == null)
        {
            cacheEntry = new CacheEntry();
            entries.put(cacheKey, cacheEntry);
        }

        return cacheEntry;
    }

    public static class CacheKey
    {
        private String translatorId;

        private Object objectId;

        private FetchOptions<?> fetchOptions;

        public CacheKey(String translatorId, Object objectId, FetchOptions<?> fetchOptions)
        {
            this.translatorId = translatorId;
            this.objectId = objectId;
            this.fetchOptions = fetchOptions;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((translatorId == null) ? 0 : translatorId.hashCode());
            result = prime * result + ((objectId == null) ? 0 : objectId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }

            CacheKey other = (CacheKey) obj;

            if (translatorId == null)
            {
                if (other.translatorId != null)
                {
                    return false;
                }
            } else if (!translatorId.equals(other.translatorId))
            {
                return false;
            }

            if (objectId == null)
            {
                if (other.objectId != null)
                {
                    return false;
                }
            } else if (!objectId.equals(other.objectId))
            {
                return false;
            }

            if (!EqualsBuilder.reflectionEquals(fetchOptions, other.fetchOptions, false, null, true))
            {
                return false;
            }

            return true;
        }

    }

    public static class CacheEntry
    {

        private boolean shouldTranslateSet;

        private Boolean shouldTranslate;

        private boolean translatedObjectSet;

        private Object translatedObject;

        private CacheEntry()
        {
        }

        public boolean isShouldTranslateSet()
        {
            return shouldTranslateSet;
        }

        public Boolean getShouldTranslate()
        {
            return shouldTranslate;
        }

        public void setShouldTranslate(boolean shouldTranslate)
        {
            this.shouldTranslate = shouldTranslate;
            this.shouldTranslateSet = true;
        }

        public boolean isTranslatedObjectSet()
        {
            return translatedObjectSet;
        }

        public Object getTranslatedObject()
        {
            return translatedObject;
        }

        public void setTranslatedObject(Object translatedObject)
        {
            this.translatedObject = translatedObject;
            this.translatedObjectSet = true;
        }

    }

}
