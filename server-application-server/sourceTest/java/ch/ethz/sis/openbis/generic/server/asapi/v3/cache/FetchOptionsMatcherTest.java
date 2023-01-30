/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.cache;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.sharedapi.v3.ApiClassesProvider;

public class FetchOptionsMatcherTest
{

    @Test
    public void testMatchTheSameObjects()
    {
        SampleFetchOptions fo = new SampleFetchOptions();
        Assert.assertTrue(new FetchOptionsMatcher().areMatching(fo, fo));
    }

    @Test
    public void testMatchEmptyObjects()
    {
        SampleFetchOptions fo1 = new SampleFetchOptions();
        SampleFetchOptions fo2 = new SampleFetchOptions();
        Assert.assertTrue(new FetchOptionsMatcher().areMatching(fo1, fo2));
    }

    @Test
    public void testMatchObjectsWithTheSameParts()
    {
        SampleFetchOptions fo1 = new SampleFetchOptions();
        fo1.withSpace();
        fo1.withExperiment();

        SampleFetchOptions fo2 = new SampleFetchOptions();
        fo2.withSpace();
        fo2.withExperiment();

        Assert.assertTrue(new FetchOptionsMatcher().areMatching(fo1, fo2));
    }

    @Test
    public void testMatchObjectsWithDifferentParts()
    {
        SampleFetchOptions fo1 = new SampleFetchOptions();
        fo1.withSpace();
        fo1.withExperiment();

        SampleFetchOptions fo2 = new SampleFetchOptions();
        fo2.withSpace();

        Assert.assertFalse(new FetchOptionsMatcher().areMatching(fo1, fo2));
    }

    @Test
    public void testMatchAllFetchOptionsClasses() throws Exception
    {
        Collection<Class<?>> classes = ApiClassesProvider.getPublicClasses();

        for (Class<?> clazz : classes)
        {
            if (FetchOptions.class.isAssignableFrom(clazz) && false == Modifier.isAbstract(clazz.getModifiers()))
            {
                FetchOptions<?> fo1 = (FetchOptions<?>) clazz.newInstance();
                FetchOptions<?> fo2 = (FetchOptions<?>) clazz.newInstance();

                Assert.assertTrue(new FetchOptionsMatcher().areMatching(fo1, fo2));
            }
        }
    }

    @Test
    public void testMatchPersonWithSameAllWebAppSettings() throws Exception
    {
        PersonFetchOptions fo1 = new PersonFetchOptions();
        fo1.withAllWebAppSettings();

        PersonFetchOptions fo2 = new PersonFetchOptions();
        fo2.withAllWebAppSettings();

        Assert.assertTrue(new FetchOptionsMatcher().areMatching(fo1, fo2));
    }

    @Test
    public void testMatchPersonWithDifferentAllWebAppSettings() throws Exception
    {
        PersonFetchOptions fo1 = new PersonFetchOptions();
        fo1.withAllWebAppSettings();

        PersonFetchOptions fo2 = new PersonFetchOptions();

        Assert.assertFalse(new FetchOptionsMatcher().areMatching(fo1, fo2));
    }

    @Test
    public void testMatchPersonWithSameWebAppSettings() throws Exception
    {
        PersonFetchOptions fo1 = new PersonFetchOptions();
        fo1.withWebAppSettings("w1").withAllSettings();
        fo1.withWebAppSettings("w2").withSetting("s1");
        fo1.withWebAppSettings("w2").withSetting("s2");

        PersonFetchOptions fo2 = new PersonFetchOptions();
        fo2.withWebAppSettings("w1").withAllSettings();
        fo2.withWebAppSettings("w2").withSetting("s1");
        fo2.withWebAppSettings("w2").withSetting("s2");

        Assert.assertTrue(new FetchOptionsMatcher().areMatching(fo1, fo2));
    }

    @Test
    public void testMatchPersonWithDifferentWebAppSettings() throws Exception
    {
        PersonFetchOptions fo1 = new PersonFetchOptions();
        fo1.withWebAppSettings("w1").withAllSettings();
        fo1.withWebAppSettings("w2").withSetting("s1");
        fo1.withWebAppSettings("w2").withSetting("s2");

        PersonFetchOptions fo2 = new PersonFetchOptions();
        fo2.withWebAppSettings("w1");
        fo2.withWebAppSettings("w2").withSetting("s1");
        fo2.withWebAppSettings("w2").withSetting("s2");

        PersonFetchOptions fo3 = new PersonFetchOptions();
        fo3.withWebAppSettings("w1").withAllSettings();
        fo3.withWebAppSettings("w2").withSetting("s1");

        PersonFetchOptions fo4 = new PersonFetchOptions();
        fo4.withWebAppSettings("w1").withAllSettings();
        fo4.withWebAppSettings("w2");

        PersonFetchOptions fo5 = new PersonFetchOptions();
        fo4.withWebAppSettings("w1");
        fo5.withWebAppSettings("w2").withSetting("s1");

        PersonFetchOptions fo6 = new PersonFetchOptions();
        fo6.withWebAppSettings("w1");
        fo6.withWebAppSettings("w2");

        PersonFetchOptions fo7 = new PersonFetchOptions();

        List<PersonFetchOptions> list = Arrays.asList(fo1, fo2, fo3, fo4, fo5, fo6, fo7);

        for (PersonFetchOptions item1 : list)
        {
            for (PersonFetchOptions item2 : list)
            {
                if (item1 != item2)
                {
                    Assert.assertFalse(new FetchOptionsMatcher().areMatching(item1, item2));
                }
            }
        }
    }

    @Test
    public void testMatchObjectsWithSameTopLevelRecursiveParts()
    {
        SampleFetchOptions fo1 = new SampleFetchOptions();
        fo1.withSpace();
        fo1.withChildrenUsing(fo1);

        SampleFetchOptions fo2 = new SampleFetchOptions();
        fo2.withSpace();
        fo2.withChildrenUsing(fo2);

        Assert.assertTrue(new FetchOptionsMatcher().areMatching(fo1, fo2));
    }

    @Test
    public void testMatchObjectsWithDifferentTopLevelRecursiveParts()
    {
        SampleFetchOptions fo1 = new SampleFetchOptions();
        fo1.withSpace();
        fo1.withChildrenUsing(fo1);

        SampleFetchOptions fo2 = new SampleFetchOptions();
        fo2.withSpace();
        fo2.withExperiment();
        fo2.withChildrenUsing(fo2);

        Assert.assertFalse(new FetchOptionsMatcher().areMatching(fo1, fo2));
    }

    @Test
    public void testMatchObjectsWithSameSubLevelRecursiveParts()
    {
        SampleFetchOptions fo1Children = new SampleFetchOptions();
        fo1Children.withExperiment();
        fo1Children.withChildrenUsing(fo1Children);

        SampleFetchOptions fo1 = new SampleFetchOptions();
        fo1.withSpace();
        fo1.withChildrenUsing(fo1Children);

        SampleFetchOptions fo2Children = new SampleFetchOptions();
        fo2Children.withExperiment();
        fo2Children.withChildrenUsing(fo2Children);

        SampleFetchOptions fo2 = new SampleFetchOptions();
        fo2.withSpace();
        fo2.withChildrenUsing(fo2Children);

        Assert.assertTrue(new FetchOptionsMatcher().areMatching(fo1, fo2));
    }

    @Test
    public void testMatchObjectsWithDifferentSubLevelRecursiveParts()
    {
        SampleFetchOptions fo1Children = new SampleFetchOptions();
        fo1Children.withExperiment();
        fo1Children.withChildrenUsing(fo1Children);

        SampleFetchOptions fo1 = new SampleFetchOptions();
        fo1.withSpace();
        fo1.withChildrenUsing(fo1Children);

        SampleFetchOptions fo2Children = new SampleFetchOptions();
        fo2Children.withContainer();
        fo2Children.withChildrenUsing(fo2Children);

        SampleFetchOptions fo2 = new SampleFetchOptions();
        fo2.withSpace();
        fo2.withChildrenUsing(fo2Children);

        Assert.assertFalse(new FetchOptionsMatcher().areMatching(fo1, fo2));
    }

    @Test
    public void testMatchObjectsWithSameMultiLevelParts()
    {
        SampleFetchOptions fo1 = new SampleFetchOptions();
        fo1.withSpace().withProjects().withAttachments();
        fo1.withChildren().withDataSets().withHistory();

        SampleFetchOptions fo2 = new SampleFetchOptions();
        fo2.withSpace().withProjects().withAttachments();
        fo2.withChildren().withDataSets().withHistory();

        Assert.assertTrue(new FetchOptionsMatcher().areMatching(fo1, fo2));
    }

    @Test
    public void testMatchObjectsWithDifferentMultiLevelParts()
    {
        SampleFetchOptions fo1 = new SampleFetchOptions();
        fo1.withSpace().withProjects().withAttachments();
        fo1.withChildren().withDataSets().withHistory();

        SampleFetchOptions fo2 = new SampleFetchOptions();
        fo2.withSpace().withProjects().withAttachments();
        fo2.withChildren().withDataSets();

        Assert.assertFalse(new FetchOptionsMatcher().areMatching(fo1, fo2));
    }

    @Test
    public void testMatchObjectsWithSameTopLevelPaging()
    {
        MaterialFetchOptions fo1 = new MaterialFetchOptions();
        fo1.from(10).count(5);

        MaterialFetchOptions fo2 = new MaterialFetchOptions();
        fo2.count(5).from(10);

        Assert.assertTrue(new FetchOptionsMatcher().areMatching(fo1, fo2));
    }

    @Test
    public void testMatchObjectsWithDifferentTopLevelPaging()
    {
        MaterialFetchOptions fo1 = new MaterialFetchOptions();
        fo1.from(10).count(5);

        MaterialFetchOptions fo2 = new MaterialFetchOptions();
        fo2.from(3).count(7);

        Assert.assertTrue(new FetchOptionsMatcher().areMatching(fo1, fo2));
    }

    @Test
    public void testMatchObjectsWithSameSubLevelPaging()
    {
        MaterialFetchOptions fo1 = new MaterialFetchOptions();
        fo1.withTags().from(1).count(5);

        MaterialFetchOptions fo2 = new MaterialFetchOptions();
        fo2.withTags().from(1).count(5);

        Assert.assertTrue(new FetchOptionsMatcher().areMatching(fo1, fo2));
    }

    @Test
    public void testMatchObjectsWithDifferentSubLevelPaging()
    {
        MaterialFetchOptions fo1 = new MaterialFetchOptions();
        fo1.withTags().from(1).count(5);

        MaterialFetchOptions fo2 = new MaterialFetchOptions();
        fo2.withTags().from(2).count(6);

        Assert.assertTrue(new FetchOptionsMatcher().areMatching(fo1, fo2));
    }

}
