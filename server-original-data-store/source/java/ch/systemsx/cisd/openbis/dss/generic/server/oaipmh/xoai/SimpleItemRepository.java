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

package ch.systemsx.cisd.openbis.dss.generic.server.oaipmh.xoai;

import java.util.Date;
import java.util.List;

import org.dspace.xoai.dataprovider.exceptions.IdDoesNotExistException;
import org.dspace.xoai.dataprovider.exceptions.OAIException;
import org.dspace.xoai.dataprovider.filter.ScopedFilter;
import org.dspace.xoai.dataprovider.handlers.results.ListItemIdentifiersResult;
import org.dspace.xoai.dataprovider.handlers.results.ListItemsResults;
import org.dspace.xoai.dataprovider.model.Item;
import org.dspace.xoai.dataprovider.repository.ItemRepository;

/**
 * <p>
 * Simple implementation of {@link org.dspace.xoai.dataprovider.repository.ItemRepository} that reduces the number of methods that have to
 * implemented. It delegates all the calls to the overloaded versions of getItemIdentifiersXXX and getItemsXXX methods down to:
 * {@link #doGetItemIdentifiers(List, int, int, String, Date, Date)} and {@link #doGetItems(List, int, int, String, Date, Date)}.
 * </p>
 * 
 * @author pkupczyk
 */
public abstract class SimpleItemRepository implements ItemRepository
{

    public abstract Item doGetItem(String identifier) throws IdDoesNotExistException, OAIException;

    public abstract ListItemIdentifiersResult doGetItemIdentifiers(List<ScopedFilter> filters, int offset, int length, String setSpec, Date from,
            Date until) throws OAIException;

    public abstract ListItemsResults doGetItems(List<ScopedFilter> filters, int offset, int length, String setSpec, Date from, Date until)
            throws OAIException;

    @Override
    public Item getItem(String identifier) throws IdDoesNotExistException, OAIException
    {
        Item item = doGetItem(identifier);

        if (item != null)
        {
            return item;
        } else
        {
            throw new IdDoesNotExistException();
        }
    }

    @Override
    public ListItemIdentifiersResult getItemIdentifiers(List<ScopedFilter> filters, int offset, int length) throws OAIException
    {
        return doGetItemIdentifiers(filters, offset, length, null, null, null);
    }

    @Override
    public ListItemIdentifiersResult getItemIdentifiers(List<ScopedFilter> filters, int offset, int length, Date from) throws OAIException
    {
        return doGetItemIdentifiers(filters, offset, length, null, from, null);
    }

    @Override
    public ListItemIdentifiersResult getItemIdentifiersUntil(List<ScopedFilter> filters, int offset, int length, Date until) throws OAIException
    {
        return doGetItemIdentifiers(filters, offset, length, null, null, until);
    }

    @Override
    public ListItemIdentifiersResult getItemIdentifiers(List<ScopedFilter> filters, int offset, int length, Date from, Date until)
            throws OAIException
    {
        return doGetItemIdentifiers(filters, offset, length, null, from, until);
    }

    @Override
    public ListItemIdentifiersResult getItemIdentifiers(List<ScopedFilter> filters, int offset, int length, String setSpec) throws OAIException
    {
        return doGetItemIdentifiers(filters, offset, length, setSpec, null, null);
    }

    @Override
    public ListItemIdentifiersResult getItemIdentifiers(List<ScopedFilter> filters, int offset, int length, String setSpec, Date from)
            throws OAIException
    {
        return doGetItemIdentifiers(filters, offset, length, setSpec, from, null);
    }

    @Override
    public ListItemIdentifiersResult getItemIdentifiersUntil(List<ScopedFilter> filters, int offset, int length, String setSpec, Date until)
            throws OAIException
    {
        return doGetItemIdentifiers(filters, offset, length, setSpec, null, until);
    }

    @Override
    public ListItemIdentifiersResult getItemIdentifiers(List<ScopedFilter> filters, int offset, int length, String setSpec, Date from, Date until)
            throws OAIException
    {
        return doGetItemIdentifiers(filters, offset, length, setSpec, from, until);
    }

    @Override
    public ListItemsResults getItems(List<ScopedFilter> filters, int offset, int length) throws OAIException
    {
        return doGetItems(filters, offset, length, null, null, null);
    }

    @Override
    public ListItemsResults getItems(List<ScopedFilter> filters, int offset, int length, Date from) throws OAIException
    {
        return doGetItems(filters, offset, length, null, from, null);
    }

    @Override
    public ListItemsResults getItemsUntil(List<ScopedFilter> filters, int offset, int length, Date until) throws OAIException
    {
        return doGetItems(filters, offset, length, null, null, until);
    }

    @Override
    public ListItemsResults getItems(List<ScopedFilter> filters, int offset, int length, Date from, Date until) throws OAIException
    {
        return doGetItems(filters, offset, length, null, from, until);
    }

    @Override
    public ListItemsResults getItems(List<ScopedFilter> filters, int offset, int length, String setSpec) throws OAIException
    {
        return doGetItems(filters, offset, length, setSpec, null, null);
    }

    @Override
    public ListItemsResults getItems(List<ScopedFilter> filters, int offset, int length, String setSpec, Date from) throws OAIException
    {
        return doGetItems(filters, offset, length, setSpec, from, null);
    }

    @Override
    public ListItemsResults getItemsUntil(List<ScopedFilter> filters, int offset, int length, String setSpec, Date until) throws OAIException
    {
        return doGetItems(filters, offset, length, setSpec, null, until);
    }

    @Override
    public ListItemsResults getItems(List<ScopedFilter> filters, int offset, int length, String setSpec, Date from, Date until) throws OAIException
    {
        return doGetItems(filters, offset, length, setSpec, from, until);
    }

}
