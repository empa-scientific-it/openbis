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

package ch.ethz.sis.openbis.generic.server.asapi.v3.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * @author pkupczyk
 */
public class Context implements IContext
{

    private final ProgressStack progressStack = new ProgressStack();

    private final Collection<IProgressListener> progressListeners = new ArrayList<IProgressListener>();

    private final Map<String, Object> attributes = new HashMap<String, Object>();

    private final Session session;

    public Context(Session session)
    {
        this.session = session;
    }

    @Override
    public void addProgressListener(IProgressListener progressListener)
    {
        progressListeners.add(progressListener);
    }

    private void notifyProgressListeners()
    {
        for (IProgressListener progressListener : progressListeners)
        {
            progressListener.onProgress(progressStack);
        }
    }

    @Override
    public void pushProgress(IProgress progress)
    {
        progressStack.push(progress);
        notifyProgressListeners();
    }

    @Override
    public IProgress popProgress()
    {
        return progressStack.pop();
    }

    @Override
    public IProgressStack getProgressStack()
    {
        return progressStack;
    }

    @Override
    public Session getSession()
    {
        return session;
    }

    @Override
    public Object getAttribute(String attributeName)
    {
        return attributes.get(attributeName);
    }

    @Override
    public void setAttribute(String attributeName, Object attributeValue)
    {
        attributes.put(attributeName, attributeValue);
    }

}
