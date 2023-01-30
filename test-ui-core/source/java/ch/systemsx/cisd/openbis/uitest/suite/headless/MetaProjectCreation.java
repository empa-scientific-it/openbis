/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.suite.headless;

import static org.hamcrest.MatcherAssert.assertThat;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.uitest.type.MetaProject;
import ch.systemsx.cisd.openbis.uitest.type.User;

/**
 * @author anttil
 */
public class MetaProjectCreation extends HeadlessSuite
{
    @Test
    public void createdMetaProjectAppearsInMetaProjectListing() throws Exception
    {
        MetaProject metaProject = create(aMetaProject());

        assertThat(listOfAllMetaProjects(), contains(metaProject));
    }

    @Test(expectedExceptions =
    { UserFailureException.class })
    public void metaProjectNamesDoNotAllowSpace() throws Exception
    {
        create(aMetaProject().withName("The Name"));
    }

    @Test(expectedExceptions =
    { UserFailureException.class })
    public void metaProjectNamesDoNotAllowSlash() throws Exception
    {
        create(aMetaProject().withName("This/That"));
    }

    @Test(expectedExceptions =
    { UserFailureException.class })
    public void metaProjectNamesDoNotAllowComma() throws Exception
    {
        create(aMetaProject().withName("This,That"));
    }

    @Test
    public void metaProjectNamesPreserveCase() throws Exception
    {
        create(aMetaProject().withName("UPPERCASElowercase"));

        MetaProject existing = assume(aMetaProject().withName("UPPERCASElowercase"));
        MetaProject nonExisting = assume(aMetaProject().withName("uppercaselowercase"));

        assertThat(listOfAllMetaProjects(), contains(existing));
        assertThat(listOfAllMetaProjects(), doesNotContain(nonExisting));
    }

    @Test(expectedExceptions =
    { UserFailureException.class })
    public void cannotCreateMetaProjectsWithSameNameInDifferentCases() throws Exception
    {
        create(aMetaProject().withName("NamE"));
        create(aMetaProject().withName("NAMe"));
    }

    @Test
    public void differentUsersCanCreateMetaProjectWithSameName() throws Exception
    {
        User first = create(aUser());
        User second = create(aUser());

        MetaProject a = as(user(first), create(aMetaProject().withName("metaproject")));
        MetaProject b = as(user(second), create(aMetaProject().withName("metaproject")));

        assertThat(as(user(first), listOfAllMetaProjects()), containsExactly(a));
        assertThat(as(user(second), listOfAllMetaProjects()), containsExactly(b));
    }

    @Test
    public void metaProjectsOfOtherUsersAreNotVisible() throws Exception
    {
        User first = create(aUser());
        User second = create(aUser());

        MetaProject firstMeta =
                as(user(first), create(aMetaProject().withName("metaProjectOfFirst")));
        MetaProject secondMeta =
                as(user(second), create(aMetaProject().withName("metaProjectOfSecond")));

        assertThat(as(user(first), listOfAllMetaProjects()), containsExactly(firstMeta));
        assertThat(as(user(second), listOfAllMetaProjects()), containsExactly(secondMeta));

    }
}
