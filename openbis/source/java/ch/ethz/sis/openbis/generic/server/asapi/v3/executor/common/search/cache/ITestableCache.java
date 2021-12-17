package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.cache;

import java.util.Map;
import java.util.Queue;

import org.apache.commons.lang3.tuple.ImmutablePair;

/**
 * Cache interface that discloses methods for testing purposes.
 */
interface ITestableCache<V> extends ICache<V>
{

    Map<String, ImmutablePair<Long, V>> getCachedResults();

    Queue<String> getKeyQueue();

}
