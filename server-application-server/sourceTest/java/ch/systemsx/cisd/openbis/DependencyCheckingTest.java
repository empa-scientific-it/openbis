/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.test.AbstractDependencyCheckingTestCase;

/**
 * @author Franz-Josef Elmer
 */
@Test
public class DependencyCheckingTest extends AbstractDependencyCheckingTestCase
{

    @Override
    protected List<String> getExcludingClassesPatterns()
    {
        return Arrays.asList("*.testframework.*", "*TestSuite*",
                "*.web.client.application.*.Show*Editor");
    }

}
