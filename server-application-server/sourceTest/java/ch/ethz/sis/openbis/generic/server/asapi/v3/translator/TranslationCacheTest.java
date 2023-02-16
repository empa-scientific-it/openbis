/*
 * Copyright ETH 2019 - 2023 Zürich, Scientific IT Services
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

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationCache.CacheEntry;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationCache.CacheKey;

public class TranslationCacheTest
{

    @Test
    public void testWithTranslatorIdEqual()
    {
        CacheKey key1 = new CacheKey("id", 1L, new SampleFetchOptions());
        CacheKey key2 = new CacheKey("id", 1L, new SampleFetchOptions());

        TranslationCache cache = new TranslationCache();

        CacheEntry entry1 = cache.getEntry(key1);
        CacheEntry entry2 = cache.getEntry(key2);

        assertTrue(entry1 == entry2);
    }

    @Test
    public void testWithTranslatorIdNotEqual()
    {
        CacheKey key1 = new CacheKey("id1", 1L, new SampleFetchOptions());
        CacheKey key2 = new CacheKey("id2", 1L, new SampleFetchOptions());

        TranslationCache cache = new TranslationCache();

        CacheEntry entry1 = cache.getEntry(key1);
        CacheEntry entry2 = cache.getEntry(key2);

        assertTrue(entry1 != entry2);
    }

    @Test
    public void testWithObjectIdEqual()
    {
        CacheKey key1 = new CacheKey("id", 1L, new SampleFetchOptions());
        CacheKey key2 = new CacheKey("id", 1L, new SampleFetchOptions());

        TranslationCache cache = new TranslationCache();

        CacheEntry entry1 = cache.getEntry(key1);
        CacheEntry entry2 = cache.getEntry(key2);

        assertTrue(entry1 == entry2);
    }

    @Test
    public void testWithObjectIdNotEqual()
    {
        CacheKey key1 = new CacheKey("id", 1L, new SampleFetchOptions());
        CacheKey key2 = new CacheKey("id", 2L, new SampleFetchOptions());

        TranslationCache cache = new TranslationCache();

        CacheEntry entry1 = cache.getEntry(key1);
        CacheEntry entry2 = cache.getEntry(key2);

        assertTrue(entry1 != entry2);
    }

    @Test
    public void testWithFetchOptionsEqual()
    {
        SampleFetchOptions fo1 = new SampleFetchOptions();
        fo1.withParents().withChildren().withType();
        fo1.withParents().withExperiment().withHistory();
        fo1.withParents().sortBy().permId().asc();
        fo1.withType();
        fo1.sortBy().code().desc();
        fo1.count(10).from(20);

        SampleFetchOptions fo2 = new SampleFetchOptions();
        fo2.withParents().withChildren().withType();
        fo2.withParents().withExperiment().withHistory();
        fo2.withParents().sortBy().permId().asc();
        fo2.withType();
        fo2.sortBy().code().desc();
        fo2.count(10).from(20);

        CacheKey key1 = new CacheKey("id", 1L, fo1);
        CacheKey key2 = new CacheKey("id", 1L, fo2);

        TranslationCache cache = new TranslationCache();

        CacheEntry entry1 = cache.getEntry(key1);
        CacheEntry entry2 = cache.getEntry(key2);

        assertTrue(entry1 == entry2);
    }

    @Test
    public void testWithFetchOptionsNotEqual()
    {
        SampleFetchOptions fo1 = new SampleFetchOptions();
        fo1.withParents().withChildren().withType();
        fo1.withParents().withExperiment().withHistory();
        fo1.withType();
        fo1.sortBy().code().desc();
        fo1.count(10).from(20);

        SampleFetchOptions fo2 = new SampleFetchOptions();
        fo2.withParents().withChildren().withType();
        fo2.withParents().withExperiment().withHistory().withAuthor(); // difference: withAuthor()
        fo2.withType();
        fo2.sortBy().code().desc();
        fo2.count(10).from(20);

        SampleFetchOptions fo3 = new SampleFetchOptions();
        fo3.withParents().withChildren().withType();
        fo3.withParents().withExperiment().withHistory();
        fo3.withType();
        fo3.sortBy().code().desc();
        fo3.count(10).from(30); // difference: 30

        CacheKey key1 = new CacheKey("id", 1L, fo1);
        CacheKey key2 = new CacheKey("id", 1L, fo2);
        CacheKey key3 = new CacheKey("id", 1L, fo3);

        TranslationCache cache = new TranslationCache();

        CacheEntry entry1 = cache.getEntry(key1);
        CacheEntry entry2 = cache.getEntry(key2);
        CacheEntry entry3 = cache.getEntry(key3);

        assertTrue(entry1 != entry2);
        assertTrue(entry1 != entry3);
    }

}
