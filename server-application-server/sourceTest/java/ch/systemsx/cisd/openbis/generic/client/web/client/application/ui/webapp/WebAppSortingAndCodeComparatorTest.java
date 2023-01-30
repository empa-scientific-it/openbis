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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.webapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.WebApp;

/**
 * @author pkupczyk
 */
public class WebAppSortingAndCodeComparatorTest
{

    @Test
    public void testCompareWithAllSortingValuesNotNull()
    {
        List<WebApp> webApps = new ArrayList<WebApp>();
        webApps.add(new WebApp("webapp3", null, 3, null, null));
        webApps.add(new WebApp("webapp1", null, 1, null, null));
        webApps.add(new WebApp("webapp2", null, 2, null, null));
        webApps.add(new WebApp("webapp4", null, 4, null, null));

        Collections.sort(webApps, new WebAppSortingAndCodeComparator());

        Assert.assertEquals("webapp1", webApps.get(0).getCode());
        Assert.assertEquals("webapp2", webApps.get(1).getCode());
        Assert.assertEquals("webapp3", webApps.get(2).getCode());
        Assert.assertEquals("webapp4", webApps.get(3).getCode());
    }

    @Test
    public void testCompareWithAllSortingValuesNull()
    {
        List<WebApp> webApps = new ArrayList<WebApp>();
        webApps.add(new WebApp("webapp3", null, null, null, null));
        webApps.add(new WebApp("webapp1", null, null, null, null));
        webApps.add(new WebApp("webapp2", null, null, null, null));
        webApps.add(new WebApp("webapp4", null, null, null, null));

        Collections.sort(webApps, new WebAppSortingAndCodeComparator());

        Assert.assertEquals("webapp1", webApps.get(0).getCode());
        Assert.assertEquals("webapp2", webApps.get(1).getCode());
        Assert.assertEquals("webapp3", webApps.get(2).getCode());
        Assert.assertEquals("webapp4", webApps.get(3).getCode());
    }

    @Test
    public void testCompareWithSomeSortingValuesNotNull()
    {
        List<WebApp> webApps = new ArrayList<WebApp>();
        webApps.add(new WebApp("webapp3", null, 1, null, null));
        webApps.add(new WebApp("webapp1", null, null, null, null));
        webApps.add(new WebApp("webapp2", null, null, null, null));
        webApps.add(new WebApp("webapp4", null, 2, null, null));

        Collections.sort(webApps, new WebAppSortingAndCodeComparator());

        Assert.assertEquals("webapp3", webApps.get(0).getCode());
        Assert.assertEquals("webapp4", webApps.get(1).getCode());
        Assert.assertEquals("webapp1", webApps.get(2).getCode());
        Assert.assertEquals("webapp2", webApps.get(3).getCode());
    }

}
