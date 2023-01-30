/*
 * Copyright 2016 ETH Zuerich, CISD
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

import java.util.Iterator;
import java.util.Stack;

/**
 * @author pkupczyk
 */
class ProgressStack implements IProgressStack
{

    private Stack<IProgress> stack = new Stack<IProgress>();

    void push(IProgress progress)
    {
        stack.push(progress);
    }

    IProgress pop()
    {
        return stack.pop();
    }

    @Override
    public Iterator<IProgress> iterator()
    {
        return new Iterator<IProgress>()
            {

                int index = stack.size();

                @Override
                public boolean hasNext()
                {
                    return index > 0;
                }

                @Override
                public IProgress next()
                {
                    return stack.get(--index);
                }

                @Override
                public void remove()
                {
                    throw new UnsupportedOperationException();
                }
            };
    }

    @Override
    public int size()
    {
        return stack.size();
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        Iterator<IProgress> iterator = iterator();
        while (iterator.hasNext())
        {
            IProgress progress = iterator.next();
            sb.append(ProgressFormatter.format(progress));
            if (iterator.hasNext())
            {
                sb.append(", ");
            }
        }

        sb.append("]");
        return sb.toString();
    }

}
