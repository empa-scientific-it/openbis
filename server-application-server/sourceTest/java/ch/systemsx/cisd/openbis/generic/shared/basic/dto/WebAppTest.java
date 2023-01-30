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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.testng.annotations.Test;

/**
 * @author pkupczyk
 */
public class WebAppTest
{

    @Test
    public void testMatchesContextWithNullContextsList()
    {
        WebApp webApp = new WebApp("webapp", null, null, null, null);

        for (WebAppContext context : WebAppContext.values())
        {
            Assert.assertFalse(webApp.matchesContext(context));
        }
    }

    @Test
    public void testMatchesContextWithEmptyContextsList()
    {
        WebApp webApp = new WebApp("webapp", null, null, new String[] {}, null);

        for (WebAppContext context : WebAppContext.values())
        {
            Assert.assertFalse(webApp.matchesContext(context));
        }
    }

    @Test
    public void testMatchesContextWithExactMatchInContextsList()
    {
        WebApp webApp = new WebApp("webapp", null, null, new String[]
        { "experiment-details-view" }, null);

        for (WebAppContext context : WebAppContext.values())
        {
            if (WebAppContext.EXPERIMENT_DETAILS_VIEW.equals(context))
            {
                Assert.assertTrue(webApp.matchesContext(context));
            } else
            {
                Assert.assertFalse(webApp.matchesContext(context));
            }
        }
    }

    @Test
    public void testMatchesContextWithRegexpMatchInContextsList()
    {
        WebApp webApp = new WebApp("webapp", null, null, new String[]
        { ".*-details-view" }, null);

        for (WebAppContext context : WebAppContext.values())
        {
            if (WebAppContext.EXPERIMENT_DETAILS_VIEW.equals(context)
                    || WebAppContext.SAMPLE_DETAILS_VIEW.equals(context)
                    || WebAppContext.DATA_SET_DETAILS_VIEW.equals(context)
                    || WebAppContext.MATERIAL_DETAILS_VIEW.equals(context))
            {
                Assert.assertTrue(webApp.matchesContext(context));
            } else
            {
                Assert.assertFalse(webApp.matchesContext(context));
            }
        }
    }

    @Test
    public void testMatchesEntityWithNullEntityTypesMap()
    {
        WebApp webApp = new WebApp("webapp", null, null, null, null);

        for (EntityKind kind : EntityKind.values())
        {
            Assert.assertFalse(webApp.matchesEntity(kind, new BasicEntityType()));
        }
    }

    @Test
    public void testMatchesEntityWithEmptyEntityTypesMap()
    {
        Map<EntityKind, String[]> entityTypes = new HashMap<EntityKind, String[]>();
        WebApp webApp = new WebApp("webapp", null, null, null, entityTypes);

        for (EntityKind kind : EntityKind.values())
        {
            Assert.assertFalse(webApp.matchesEntity(kind, new BasicEntityType()));
        }
    }

    @Test
    public void testMatchesEntityWithExactMatchInEntityTypesMap()
    {
        Map<EntityKind, String[]> entityTypes = new HashMap<EntityKind, String[]>();
        entityTypes.put(EntityKind.EXPERIMENT, new String[]
        { "EXP0", "EXP1" });
        WebApp webApp = new WebApp("webapp", null, null, null, entityTypes);

        for (EntityKind kind : EntityKind.values())
        {
            BasicEntityType exp1Type = new BasicEntityType("EXP1");
            BasicEntityType exp2Type = new BasicEntityType("EXP2");

            if (EntityKind.EXPERIMENT.equals(kind))
            {
                Assert.assertTrue(webApp.matchesEntity(kind, exp1Type));
                Assert.assertFalse(webApp.matchesEntity(kind, exp2Type));
            } else
            {
                Assert.assertFalse(webApp.matchesEntity(kind, exp1Type));
                Assert.assertFalse(webApp.matchesEntity(kind, exp2Type));
            }
        }
    }

    @Test
    public void testMatchesEntityWithRegexpMatchInEntityTypesMap()
    {
        Map<EntityKind, String[]> entityTypes = new HashMap<EntityKind, String[]>();
        entityTypes.put(EntityKind.EXPERIMENT, new String[]
        { "EXP0", "EXP.*" });
        WebApp webApp = new WebApp("webapp", null, null, null, entityTypes);

        for (EntityKind kind : EntityKind.values())
        {
            BasicEntityType exp1Type = new BasicEntityType("EXP1");
            BasicEntityType abc2Type = new BasicEntityType("ABC2");

            if (EntityKind.EXPERIMENT.equals(kind))
            {
                Assert.assertTrue(webApp.matchesEntity(kind, exp1Type));
                Assert.assertFalse(webApp.matchesEntity(kind, abc2Type));
            } else
            {
                Assert.assertFalse(webApp.matchesEntity(kind, exp1Type));
                Assert.assertFalse(webApp.matchesEntity(kind, abc2Type));
            }
        }
    }

}
