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
package ch.ethz.sis.filetransfer;

import static org.testng.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author pkupczyk
 */
public class TestAssertions
{

    private List<Throwable> problems = new ArrayList<Throwable>();

    public void executeAssertion(IAssertion assertion)
    {
        try
        {
            assertion.execute();
        } catch (Throwable e)
        {
            problems.add(e);
        }
    }

    public void assertOK()
    {
        if (false == problems.isEmpty())
        {
            StringBuilder sb = new StringBuilder();

            for (Throwable problem : problems)
            {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(os, true);
                problem.printStackTrace(ps);
                ps.close();
                sb.append("\n" + new String(os.toByteArray()));
            }

            fail("Unexpected problems: " + sb);
        }
    }

    public interface IAssertion
    {

        public void execute() throws Exception;

    }

}
