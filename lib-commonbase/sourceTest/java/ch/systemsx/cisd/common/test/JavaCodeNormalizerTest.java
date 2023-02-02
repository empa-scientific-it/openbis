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

package ch.systemsx.cisd.common.test;

import static ch.systemsx.cisd.base.utilities.OSUtilities.LINE_SEPARATOR;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * @author Franz-Josef Elmer
 */
public class JavaCodeNormalizerTest extends AssertJUnit
{
    private static final String EXAMPLE =
            "/*\n"
                    + " * Copyright 2008 ETH Zuerich, CISD\r"
                    + " */\n"
                    + "\n"
                    + "package ch.systemsx.cisd.openbis.plugin.generic.shared;\n"
                    + "\n"
                    + "import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;\n"
                    + "\n"
                    + "/* A one line block comment. */\n"
                    + "public interface IGenericServer\n"
                    + "{\n"
                    + "    /**\n"
                    + "     * blabla\n"
                    + "     */\n"
                    + "    @Transactional\n"
                    + "    public ExperimentPE get(int a,\n"
                    + "            // TODO\n"
                    + "            @Guard // checks ID\n"
                    + "            Id id);\n";

    private static final String NORMALIZED_EXAMPLE =
            "package ch.systemsx.cisd.openbis.plugin.generic.shared;" + LINE_SEPARATOR
                    + "import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;" + LINE_SEPARATOR
                    + "public interface IGenericServer {" + LINE_SEPARATOR
                    + "@Transactional public ExperimentPE get(int a," + LINE_SEPARATOR
                    + "@Guard Id id);";

    @Test
    public void test()
    {
        assertEquals(NORMALIZED_EXAMPLE, JavaCodeNormalizer.normalizeJavaCode(EXAMPLE));
    }
}
