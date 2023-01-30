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

package ch.systemsx.cisd.openbis.generic.server;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.jython.evaluator.Evaluator;
import ch.systemsx.cisd.common.jython.evaluator.EvaluatorException;
import ch.systemsx.cisd.common.jython.evaluator.IJythonEvaluator;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.IJythonEvaluatorPool;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IAtomicEvaluation;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IEvaluationRunner;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.ManagedPropertyFunctions;

/**
 * Pool of Jython Evaluators for managed properties. When the pool is created, it is filled with new Evaluator instances for every managed property
 * script. Enables thread-safe execution of calls to python functions defined in these scripts.
 * 
 * @author anttil
 */
public class JythonEvaluatorPool implements IJythonEvaluatorPool
{
    private static Logger log = LogFactory.getLogger(LogCategory.OPERATION,
            JythonEvaluatorPool.class);

    public static int DEFAULT_POOL_SIZE = 100;

    private Map<String, EvaluatorState> cache;

    private Lock cacheLock;

    public JythonEvaluatorPool(IDAOFactory daoFactory, String poolSize)
    {
        this(daoFactory, createCache(poolSize));
    }

    public JythonEvaluatorPool(IDAOFactory daoFactory, Map<String, EvaluatorState> cache)
    {
        this.cache = cache;
        this.cacheLock = new ReentrantLock();

        // only managed properties are cached during pool initialization, as only managed properties
        // affect the performance of read-only ui operations.
        for (ScriptPE script : daoFactory.getScriptDAO().listEntities(ScriptType.MANAGED_PROPERTY,
                null))
        {
            try
            {
                cache.put(
                        script.getScript(),
                        new EvaluatorState(createEvaluatorFor("", ManagedPropertyFunctions.class,
                                script.getScript())));
            } catch (EvaluatorException e)
            {
                log.warn("Could not create evaluator for script " + script.getName(), e);
            }
        }
        log.info("Initialization successful with " + cache.size() + " evaluators");
    }

    @Override
    public IEvaluationRunner getManagedPropertiesRunner(final String script)
    {
        return getRunner("", ManagedPropertyFunctions.class, script);
    }

    /**
     * Return runner that can be used to evaluate python functions using evaluator in the pool.
     */
    @Override
    public IEvaluationRunner getRunner(final String expression, final Class<?> clazz,
            final String script)
    {
        return new IEvaluationRunner()
            {
                @Override
                public <T> T evaluate(IAtomicEvaluation<T> evaluation)
                {
                    return JythonEvaluatorPool.this.evaluate(expression, clazz, script, evaluation);
                }
            };
    }

    /**
     * Evaluate python functions from a script using an Evaluator instance in the pool. If the Evaluator instance does not exist, create it. Give
     * access to the instance for only one thread at a time.
     */
    private <T> T evaluate(String expression, Class<?> clazz, String script,
            IAtomicEvaluation<T> evaluation)
    {
        String key = generateKey(expression, clazz, script);

        EvaluatorState state = cache.get(key);
        if (state == null)
        {
            cacheLock.lock();
            try
            {
                state = cache.get(key);
                if (state == null)
                {
                    state = new EvaluatorState(createEvaluatorFor(expression, clazz, script));
                    cache.put(key, state);
                }
            } finally
            {
                cacheLock.unlock();
            }
        }

        Lock lock = state.getLock();
        lock.lock();
        state.push();

        try
        {
            return evaluation.evaluate(state.getEvaluator());
        } finally
        {
            state.pop();
            lock.unlock();
        }
    }

    /**
     * Generate key for the script for the cache.
     */
    String generateKey(String expression, Class<?> clazz, String script)
    {
        String key = expression + "#" + script + "#" + clazz.getCanonicalName();
        return key;
    }

    String generateKeyForManagedProperties(String script)
    {
        return generateKey("", ManagedPropertyFunctions.class, script);
    }

    /**
     * Create the evaluator that has the body, and the default expression to be evaluated.
     */
    private IJythonEvaluator createEvaluatorFor(String expression, Class<?> clazz, String script)
    {
        return Evaluator.getFactory().create(expression, clazz, script);
    }

    /**
     * The pooled object. Contains the Evaluator and its initial state to which it is always returned to when a new evaluation is started.
     */
    public static class EvaluatorState
    {
        private final IJythonEvaluator evaluator;

        private final Map<String, Object> initialGlobals;

        private Stack<Map<String, Object>> globalsStack;

        private final Lock lock;

        public EvaluatorState(IJythonEvaluator evaluator)
        {
            this.evaluator = evaluator;
            this.lock = new ReentrantLock();
            this.globalsStack = new Stack<Map<String, Object>>();
            this.initialGlobals = new HashMap<String, Object>();
            for (String globalName : evaluator.getGlobalVariables())
            {
                initialGlobals.put(globalName, evaluator.get(globalName));
            }
        }

        public void push()
        {
            Map<String, Object> globalsValues = new HashMap<String, Object>();
            for (String globalName : evaluator.getGlobalVariables())
            {
                Object globalValue = evaluator.get(globalName);
                globalsValues.put(globalName, globalValue);
            }
            globalsStack.push(globalsValues);

            for (String globalName : globalsValues.keySet())
            {
                evaluator.delete(globalName);
            }

            for (String globalName : initialGlobals.keySet())
            {
                evaluator.set(globalName, initialGlobals.get(globalName));
            }

        }

        public void pop()
        {
            Map<String, Object> globalsValues = globalsStack.pop();

            for (String globalName : evaluator.getGlobalVariables())
            {
                evaluator.delete(globalName);
            }

            for (String globalName : globalsValues.keySet())
            {
                evaluator.set(globalName, globalsValues.get(globalName));
            }

            if (globalsStack.isEmpty())
            {
                evaluator.releaseResources();
            }

        }

        public IJythonEvaluator getEvaluator()
        {
            return evaluator;
        }

        public Lock getLock()
        {
            return lock;
        }
    }

    static Map<String, EvaluatorState> createCache(String poolSize)
    {
        int size = DEFAULT_POOL_SIZE;
        try
        {
            int number = Integer.parseInt(poolSize);
            if (number > 0)
            {
                size = number;
            }
        } catch (NumberFormatException e)
        {
        }

        return createCache(size);
    }

    static Map<String, EvaluatorState> createCache(final int poolSize)
    {
        return new LinkedHashMap<String, EvaluatorState>(16, 0.75f, true)
            {
                private static final long serialVersionUID = 1L;

                @Override
                protected boolean removeEldestEntry(Map.Entry<String, EvaluatorState> eldest)
                {
                    return size() > poolSize;
                }
            };
    }
}
