package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search;

import java.util.Collection;

interface ICache
{

    void add(String hashCode, Collection<Long> results);

    Collection<Long> get(String hashCode);

    boolean isCacheAvailable(String hashCode);

    void clear();

}
