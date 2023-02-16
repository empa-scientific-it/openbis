/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.server;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.jython.evaluator.IJythonEvaluator;
import ch.systemsx.cisd.common.utilities.TestResources;
import ch.systemsx.cisd.openbis.generic.server.JythonEvaluatorPool.EvaluatorState;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IScriptDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IAtomicEvaluation;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IEvaluationRunner;

/**
 * @author anttil
 */
public class JythonEvaluatorPoolTest
{
    private JythonEvaluatorPool pool;

    private Map<String, EvaluatorState> cache;

    private static int POOL_SIZE = 10;

    @Test
    public void sameEvaluatorIsAlwaysUsedForSameScript() throws Exception
    {
        long start = System.currentTimeMillis();
        while (secondsSpentSince(start) < 2)
        {
            List<IJythonEvaluator> resultList = executeParallel(getEvaluatorInUse());
            assertThat(resultList, isFilledWithSameInstanceOf(IJythonEvaluator.class));
        }
    }

    @Test
    public void globalVariablesAreHandledCorrectlyWhenThereAreNestedCalls() throws Exception
    {
        TestResources resources = new TestResources(getClass());
        String script = FileUtilities.loadToString(resources.getResourceFile("recursive_property.py"));
        final IEvaluationRunner runner =
                pool.getRunner("calculate()", Math.class, script);

        final IAtomicEvaluation<String> action = new IAtomicEvaluation<String>()
            {

                int counter = 5;

                @Override
                public String evaluate(IJythonEvaluator evaluator)
                {
                    evaluator.set("action", this);
                    evaluator.set("runner", runner);
                    evaluator.set("value", counter);
                    if (counter > 0)
                    {
                        counter--;
                        return evaluator.evalAsStringLegacy2_2();
                    } else
                    {
                        return "";
                    }
                }
            };
        runner.evaluate(action);
        // Assertion in the python script!
    }

    @Test
    public void globalVariablesSetInPreviousRunsAreClearedBeforeNewEvaluation() throws Exception
    {
        long start = System.currentTimeMillis();
        while (secondsSpentSince(start) < 2)
        {
            List<Integer> results =
                    executeParallel(returnZeroIfAVariableIsNotSetThenSetItToSomeValue());

            Set<Integer> set = new HashSet<Integer>(results);
            assertThat(set.size(), is(1));
            assertThat(set.contains(Integer.valueOf(0)), is(true));
        }
    }

    @Test
    public void poolSizeLimitIsNotExceeded() throws Exception
    {
        List<String> scripts = new ArrayList<String>();
        for (int i = 0; i < POOL_SIZE * 2; i++)
        {
            scripts.add(createRandomScript());
        }

        for (String script : scripts)
        {
            pool.getManagedPropertiesRunner(script).evaluate(dummyEvaluation());
        }

        assertThat(cache.size(), is(POOL_SIZE));
    }

    @Test
    public void whenCacheSizeLimitIsExceededLeastAccessedElementIsEvicted() throws Exception
    {
        List<String> scripts = new ArrayList<String>();
        for (int i = 0; i < POOL_SIZE + 1; i++)
        {
            scripts.add(createRandomScript());
        }

        for (String script : scripts)
        {
            pool.getManagedPropertiesRunner(script).evaluate(dummyEvaluation());
            pool.getManagedPropertiesRunner(scripts.get(0)).evaluate(dummyEvaluation());
        }

        assertThat(cache.containsKey(pool.generateKeyForManagedProperties(scripts.get(0))),
                is(true));
        assertThat(cache.containsKey(pool.generateKeyForManagedProperties(scripts.get(1))),
                is(false));
    }

    private String createRandomScript()
    {
        return "x = '" + UUID.randomUUID().toString() + "'";
    }

    private long secondsSpentSince(long time)
    {
        return (System.currentTimeMillis() - time) / 1000;
    }

    private ExecutorService executor = new ThreadPoolExecutor(10, 50, 1000, TimeUnit.HOURS,
            new LinkedBlockingDeque<Runnable>());

    private <T> List<T> executeParallel(Callable<T> task) throws Exception
    {
        List<Callable<T>> tasks = new ArrayList<Callable<T>>();
        for (int i = 0; i < 100; i++)
        {
            tasks.add(task);
        }
        List<Future<T>> futures = executor.invokeAll(tasks);
        return getFutureValues(futures);
    }

    private <T> List<T> getFutureValues(List<Future<T>> futures) throws InterruptedException,
            ExecutionException
    {
        List<T> results = new ArrayList<T>();
        for (Future<T> future : futures)
        {
            results.add(future.get());
        }
        return results;
    }

    private Callable<IJythonEvaluator> getEvaluatorInUse()
    {
        final String script = "def something():\n\treturn '" + UUID.randomUUID().toString() + "'";

        return new Callable<IJythonEvaluator>()
            {
                @Override
                public IJythonEvaluator call() throws Exception
                {
                    return pool.getManagedPropertiesRunner(script).evaluate(
                            new IAtomicEvaluation<IJythonEvaluator>()
                                {
                                    @Override
                                    public IJythonEvaluator evaluate(IJythonEvaluator evaluator)
                                    {
                                        return evaluator;
                                    }
                                });
                }
            };
    }

    private Callable<Integer> returnZeroIfAVariableIsNotSetThenSetItToSomeValue()
    {
        final String script = readFile("script.py");

        return new Callable<Integer>()
            {
                @Override
                public Integer call() throws Exception
                {
                    return pool.getManagedPropertiesRunner(script).evaluate(
                            new IAtomicEvaluation<Integer>()
                                {
                                    @Override
                                    public Integer evaluate(IJythonEvaluator evaluator)
                                    {
                                        Integer x =
                                                (Integer) evaluator
                                                        .evalFunction("return_x_if_defined_else_0");
                                        evaluator.evalFunction("set_x", 3);
                                        return x;
                                    }
                                });
                }
            };
    }

    private IAtomicEvaluation<Void> dummyEvaluation()
    {
        return new IAtomicEvaluation<Void>()
            {
                @Override
                public Void evaluate(IJythonEvaluator evaluator)
                {
                    return null;
                }
            };
    }

    private String readFile(String name)
    {
        String filePath = "sourceTest/java/ch/systemsx/cisd/openbis/generic/server/" + name;
        try
        {
            FileInputStream stream = new FileInputStream(new File(filePath));
            try
            {
                FileChannel fc = stream.getChannel();
                MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
                return Charset.defaultCharset().decode(bb).toString();
            } finally
            {
                stream.close();
            }
        } catch (IOException e)
        {
            throw new IllegalArgumentException("Could not read file " + filePath);
        }
    }

    private <T> Matcher<Collection<T>> isFilledWithSameInstanceOf(Class<T> clazz)
    {
        return new TypeSafeMatcher<Collection<T>>()
            {
                @Override
                public void describeTo(Description description)
                {
                    description.appendText("A collection with only one instance in it");
                }

                @Override
                public boolean matchesSafely(Collection<T> collection)
                {
                    if (collection.isEmpty())
                    {
                        return false;
                    }
                    if (collection.size() == 1)
                    {
                        return true;
                    }

                    T t = null;
                    for (T element : collection)
                    {
                        if (t == null)
                        {
                            t = element;
                        } else if (t == element)
                        {
                            continue;
                        } else
                        {
                            return false;
                        }
                    }
                    return true;
                }
            };
    }

    @BeforeMethod
    public void fixture()
    {
        Mockery context = new Mockery();
        final IDAOFactory daoFactory = context.mock(IDAOFactory.class);
        final IScriptDAO scriptDao = context.mock(IScriptDAO.class);
        context.checking(new Expectations()
            {
                {
                    List<ScriptPE> scripts = new ArrayList<ScriptPE>();

                    allowing(daoFactory).getScriptDAO();
                    will(returnValue(scriptDao));

                    allowing(scriptDao).listEntities(ScriptType.MANAGED_PROPERTY, null);
                    will(returnValue(scripts));
                }
            });

        cache = JythonEvaluatorPool.createCache(Integer.toString(POOL_SIZE));
        pool = new JythonEvaluatorPool(daoFactory, cache);
    }
}
