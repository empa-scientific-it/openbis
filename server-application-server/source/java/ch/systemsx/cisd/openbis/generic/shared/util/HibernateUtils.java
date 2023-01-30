/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.util;

import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

import ch.systemsx.cisd.common.collection.UnmodifiableCollectionDecorator;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;

/**
 * Some utility methods around <i>Hibernate</i>.
 * <p>
 * This should be used instead of {@link Hibernate} because we check here for {@link UnmodifiableCollectionDecorator}.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class HibernateUtils
{
    private HibernateUtils()
    {
        // Can not instantiate.
    }

    private final static Object getRealProxy(final Object proxy)
    {
        final boolean unmodifiable = (proxy instanceof UnmodifiableCollectionDecorator<?>);
        final Object realProxy =
                (unmodifiable) ? ((UnmodifiableCollectionDecorator<?>) proxy).getDecorated()
                        : proxy;
        return realProxy;
    }

    /**
     * Extends {@link Hibernate#initialize(Object)} by checking for {@link UnmodifiableCollectionDecorator}.
     */
    public final static void initialize(final Object proxy)
    {
        Hibernate.initialize(getRealProxy(proxy));
    }

    /**
     * Extends {@link Hibernate#isInitialized(Object)} by checking for {@link UnmodifiableCollectionDecorator}.
     */
    public final static boolean isInitialized(final Object proxy)
    {
        return Hibernate.isInitialized(getRealProxy(proxy));
    }

    /**
     * For given <var>idHolder</var> returns corresponding {@link Long} id.
     * <p>
     * Internally checks whether given {@link IIdHolder} is a {@link HibernateProxy} and handles correspondingly.
     * </p>
     */
    public final static Long getId(final IIdHolder idHolder)
    {
        if (idHolder instanceof HibernateProxy)
        {
            return (Long) ((HibernateProxy) idHolder).getHibernateLazyInitializer().getIdentifier();
        } else
        {
            return idHolder.getId();
        }
    }

    /**
     * @return Unproxied <var>proxy</var>.
     */
    @SuppressWarnings({ "unchecked" })
    public final static <T> T unproxy(final T proxy)
    {
        if (proxy instanceof HibernateProxy && Hibernate.isInitialized(proxy))
        {
            LazyInitializer lazyInitializer =
                    ((HibernateProxy) proxy).getHibernateLazyInitializer();
            SharedSessionContractImplementor sessionImplementor = lazyInitializer.getSession();
            // check if the given bean still has a session instance attached
            if (sessionImplementor != null)
            {
                // use the unproxy method of the persistenceContext class
                return (T) sessionImplementor.getPersistenceContext().unproxy(proxy);
            } else
            {
                // return the wrapped bean instance if there's no active session instance available
                return (T) lazyInitializer.getImplementation();
            }
        } else
        {
            // not a proxy - nothing to do
            return proxy;
        }
    }

    /**
     * @param batchMode if true the second level cache will be switched off and the hibernate session will be synchronized with the database only at
     *            the end of the transaction. Note that 1) this cause that the stale data will be fetched from database, ignoring the changes in
     *            hibernate layer 2) if you have many write operations interleaved with read operations in one block, switching synchronization off
     *            greatly improves the performance.
     */
    public final static void setBatchUpdateMode(Session session, boolean batchMode)
    {
        CacheMode cacheMode = (batchMode ? CacheMode.IGNORE : CacheMode.NORMAL);
        session.setCacheMode(cacheMode);

        FlushMode mode = (batchMode ? FlushMode.COMMIT : FlushMode.AUTO);
        session.setFlushMode(mode);
    }

}
