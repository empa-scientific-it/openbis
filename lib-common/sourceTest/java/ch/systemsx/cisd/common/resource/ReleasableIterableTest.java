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

package ch.systemsx.cisd.common.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author pkupczyk
 */
public class ReleasableIterableTest
{

    @Test
    public void testIterableReturnsEmptyIteratorWhenOriginalIterableIsNull()
    {
        ReleasableIterable<Object> iterable = new ReleasableIterable<Object>(null);
        Iterator<Object> iterator = iterable.iterator();
        Assert.assertFalse(iterator.hasNext());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testIterableReturnsEmptyIteratorWhenOriginalIterableReturnsNullIterator()
    {
        Mockery context = new Mockery();

        final Iterable<Object> originalIterable = context.mock(Iterable.class);

        context.checking(new Expectations()
            {
                {
                    one(originalIterable).iterator();
                    will(returnValue(null));
                }
            });

        ReleasableIterable<Object> iterable = new ReleasableIterable<Object>(originalIterable);
        Iterator<Object> iterator = iterable.iterator();
        Assert.assertFalse(iterator.hasNext());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testIterableReturnsEmptyIteratorWhenOriginalIterableReturnsEmptyIterator()
    {
        Mockery context = new Mockery();

        final Iterable<Object> originalIterable = context.mock(Iterable.class);

        context.checking(new Expectations()
            {
                {
                    one(originalIterable).iterator();
                    will(returnValue(Collections.emptyList().iterator()));
                }
            });

        ReleasableIterable<Object> iterable = new ReleasableIterable<Object>(originalIterable);
        Iterator<Object> iterator = iterable.iterator();
        Assert.assertFalse(iterator.hasNext());
    }

    @Test
    public void testIterableReturnsTheSameItemsAsOriginalIterable()
    {
        List<String> originalList = Arrays.asList("a", "b", "c");
        ReleasableIterable<String> iterable = new ReleasableIterable<String>(originalList);

        List<String> items = new LinkedList<String>();
        for (String item : iterable)
        {
            items.add(item);
        }

        Assert.assertEquals(items, originalList);
    }

    @Test
    public void testIterableReturnsItemsThatCanBeRemoved()
    {
        final List<String> originalList = new ArrayList<String>(Arrays.asList("a", "b", "c"));
        ReleasableIterable<String> iterable = new ReleasableIterable<String>(originalList);
        Iterator<String> iterator = iterable.iterator();

        List<String> items = new LinkedList<String>();
        while (iterator.hasNext())
        {
            String item = iterator.next();

            if (item.equals("b"))
            {
                iterator.remove();
            } else
            {
                items.add(item);
            }
        }

        Assert.assertEquals(items, Arrays.asList("a", "c"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testIterableReleasesOriginalIterableWhenOriginalIterableIsReleasable()
    {
        Mockery context = new Mockery();

        final IIterableAndReleasable<Object> originalIterable = context.mock(IIterableAndReleasable.class);
        final IIteratorAndReleasable<Object> originalIterator = context.mock(IIteratorAndReleasable.class);
        final IReleasable releasable1 = context.mock(IReleasable.class, "releasable1");
        final IReleasable releasable2 = context.mock(IReleasable.class, "releasable2");

        context.checking(new Expectations()
            {
                {
                    one(originalIterable).iterator();
                    will(returnValue(originalIterator));

                    one(originalIterator).hasNext();
                    will(returnValue(true));
                    one(originalIterator).next();
                    will(returnValue(releasable1));

                    one(originalIterator).hasNext();
                    will(returnValue(true));
                    one(originalIterator).next();
                    will(returnValue(releasable2));

                    one(originalIterator).hasNext();
                    will(returnValue(false));

                    one(originalIterable).release();
                }
            });

        ReleasableIterable<Object> iterable = new ReleasableIterable<Object>(originalIterable);

        List<Object> items = new LinkedList<Object>();
        for (Object item : iterable)
        {
            items.add(item);
        }

        Assert.assertEquals(items, Arrays.asList(releasable1, releasable2));

        // iterable should be released only once even though we call release twice here
        iterable.release();
        iterable.release();

        context.assertIsSatisfied();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testIterableReleasesOriginalIteratorWhenOriginalIterableIsNotReleasableAndOriginalIteratorIsReleasable()
    {
        Mockery context = new Mockery();

        final Iterable<Object> originalIterable = context.mock(Iterable.class);
        final IIteratorAndReleasable<Object> originalIterator = context.mock(IIteratorAndReleasable.class);
        final IReleasable releasable1 = context.mock(IReleasable.class, "releasable1");
        final IReleasable releasable2 = context.mock(IReleasable.class, "releasable2");

        context.checking(new Expectations()
            {
                {
                    one(originalIterable).iterator();
                    will(returnValue(originalIterator));

                    one(originalIterator).hasNext();
                    will(returnValue(true));
                    one(originalIterator).next();
                    will(returnValue(releasable1));

                    one(originalIterator).hasNext();
                    will(returnValue(true));
                    one(originalIterator).next();
                    will(returnValue(releasable2));

                    one(originalIterator).hasNext();
                    will(returnValue(false));

                    one(originalIterator).release();
                }
            });

        ReleasableIterable<Object> iterable = new ReleasableIterable<Object>(originalIterable);

        List<Object> items = new LinkedList<Object>();
        for (Object item : iterable)
        {
            items.add(item);
        }

        Assert.assertEquals(items, Arrays.asList(releasable1, releasable2));

        // iterator should be released only once even though we call release twice here
        iterable.release();
        iterable.release();

        context.assertIsSatisfied();
    }

    @Test
    public void testIterableReleasesItemsItHasReturnedWhenOriginalIterableAndOriginalIteratorAreNotReleasable()
    {
        Mockery context = new Mockery();

        final IReleasable releasable1 = context.mock(IReleasable.class, "releasable1");
        final IReleasable releasable2 = context.mock(IReleasable.class, "releasable2");

        context.checking(new Expectations()
            {
                {
                    one(releasable1).release();

                    one(releasable2).release();
                }
            });

        List<Object> originalList = Arrays.asList(releasable1, "string", releasable2);
        ReleasableIterable<Object> iterable = new ReleasableIterable<Object>(originalList);

        List<Object> items = new LinkedList<Object>();
        for (Object item : iterable)
        {
            items.add(item);
        }

        Assert.assertEquals(items, originalList);

        // items should be released only once even though we call release twice here
        iterable.release();
        iterable.release();

        context.assertIsSatisfied();
    }

    private static interface IIterableAndReleasable<T> extends Iterable<T>, IReleasable
    {

    }

    private static interface IIteratorAndReleasable<T> extends Iterator<T>, IReleasable
    {

    }

}
