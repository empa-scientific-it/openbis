/*
 * Copyright 2017 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.CommonAuthorizationSystemTest;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.ProjectAuthorizationUser;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
public abstract class CommonPredicateSystemTest<O> extends CommonAuthorizationSystemTest
{

    protected boolean isCollectionPredicate()
    {
        return false;
    }

    protected abstract O createNonexistentObject(Object param);

    protected abstract O createObject(SpacePE spacePE, ProjectPE projectPE, Object param);

    protected abstract void evaluateObjects(ProjectAuthorizationUser user, List<O> objects, Object param);

    protected CommonPredicateSystemTestAssertions<O> getAssertions()
    {
        return new CommonPredicateSystemTestAssertionsDefault<O>();
    }

    @Test(dataProvider = PERSON_AND_PARAM_PROVIDER, groups = GROUP_PA_TESTS)
    public void testWithNullObject(ProjectAuthorizationUser user, Object param)
    {
        List<O> objects = Arrays.asList((O) null);
        Throwable t = tryEvaluateObjects(user, objects, param);
        getAssertions().assertWithNullObject(user, t, param);
    }

    @Test(dataProvider = PERSON_AND_PARAM_PROVIDER, groups = GROUP_PA_TESTS)
    public void testWithNullCollection(ProjectAuthorizationUser user, Object param)
    {
        if (false == isCollectionPredicate())
        {
            return;
        }

        Throwable t = tryEvaluateObjects(user, null, param);
        getAssertions().assertWithNullCollection(user, t, param);
    }

    @Test(dataProvider = PERSON_AND_PARAM_PROVIDER, groups = GROUP_PA_TESTS)
    public void testWithNonexistentObject(ProjectAuthorizationUser user, Object param)
    {
        O object = createNonexistentObject(param);
        List<O> objects = Arrays.asList(object);
        Throwable t = tryEvaluateObjects(user, objects, param);
        getAssertions().assertWithNonexistentObject(user, t, param);
    }

    @Test(dataProvider = PERSON_AND_PARAM_PROVIDER, groups = GROUP_PA_TESTS)
    public void testWithProject11Object(ProjectAuthorizationUser user, Object param)
    {
        O object = createObject(getSpace1(), getProject11(), param);
        List<O> objects = Arrays.asList(object);
        Throwable t = tryEvaluateObjects(user, objects, param);
        getAssertions().assertWithProject11Object(user, t, param);
    }

    @Test(dataProvider = PERSON_AND_PARAM_PROVIDER, groups = GROUP_PA_TESTS)
    public void testWithProject21Object(ProjectAuthorizationUser user, Object param)
    {
        O object = createObject(getSpace2(), getProject21(), param);
        List<O> objects = Arrays.asList(object);
        Throwable t = tryEvaluateObjects(user, objects, param);
        getAssertions().assertWithProject21Object(user, t, param);
    }

    @Test(dataProvider = PERSON_AND_PARAM_PROVIDER, groups = GROUP_PA_TESTS)
    public void testWithProject11ObjectAndProject21Object(ProjectAuthorizationUser user, Object param)
    {
        if (false == isCollectionPredicate())
        {
            return;
        }

        O objectInProject11 = createObject(getSpace1(), getProject11(), param);
        O objectInProject21 = createObject(getSpace2(), getProject21(), param);

        List<O> objects = Arrays.asList(objectInProject11, objectInProject21);

        Throwable t = tryEvaluateObjects(user, objects, param);

        getAssertions().assertWithProject11ObjectAndProject21Object(user, t, param);
    }

    protected Throwable tryEvaluateObjects(ProjectAuthorizationUser user, List<O> objects, Object param)
    {
        try
        {
            evaluateObjects(user, objects, param);
            return null;
        } catch (Throwable t)
        {
            return t;
        }
    }

}
