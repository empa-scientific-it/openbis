/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.filesystem.control;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author anttil
 */
public class ParameterMap
{
    private final Map<String, IValueFilter> parameterNameToValueFilterMap;

    private final Map<String, Parameter> parameterNameToParameterMap;

    private final IEventFeed eventFeed;

    public ParameterMap(IEventFeed eventFeed)
    {
        this.eventFeed = eventFeed;
        this.parameterNameToValueFilterMap = new HashMap<String, IValueFilter>();
        this.parameterNameToParameterMap = new HashMap<String, Parameter>();
    }

    public synchronized void addParameter(String parameterName, String defaultValue)
    {
        addParameter(parameterName, defaultValue, new AcceptAllValuesFilter());
    }

    public synchronized void addParameter(String parameterName, String defaultValue, IValueFilter valueFilter)
    {
        if (valueFilter.isValid(defaultValue) == false)
        {
            throw new IllegalArgumentException("Default value " + defaultValue + " is not valid value for parameter " + parameterName);
        }

        parameterNameToValueFilterMap.put(parameterName, valueFilter);
        if (defaultValue != null)
        {
            parameterNameToParameterMap.put(parameterName, new Parameter(parameterName, defaultValue));
        }
    }

    public synchronized Parameter getParameterValue(String parameterName)
    {
        List<Parameter> parameters = loadParameters();
        for (Parameter parameter : parameters)
        {
            parameterNameToParameterMap.put(parameter.getName(), parameter);
        }
        return parameterNameToParameterMap.get(parameterName);
    }

    public synchronized void removeParameterValue(String parameterName)
    {
        parameterNameToParameterMap.remove(parameterName);
    }

    private List<Parameter> loadParameters()
    {
        List<String> events = eventFeed.getNewEvents(new AcceptEventsWithPrefixesFilter(getParameterNames()));
        List<Parameter> parameters = new ArrayList<Parameter>();

        for (String event : events)
        {
            String longestMatchingParameterName = null;

            for (String parameterName : getParameterNames())
            {
                if (event.startsWith(parameterName)
                        && (longestMatchingParameterName == null || longestMatchingParameterName.length() < parameterName.length()))
                {
                    {
                        longestMatchingParameterName = parameterName;
                    }
                }
            }

            if (longestMatchingParameterName != null)
            {
                String parameterValue = null;

                if (event.length() > longestMatchingParameterName.length() && event.charAt(longestMatchingParameterName.length()) == '-')
                {
                    parameterValue = event.substring(longestMatchingParameterName.length() + 1);
                }

                IValueFilter valueFilter = parameterNameToValueFilterMap.get(longestMatchingParameterName);

                if (valueFilter != null && valueFilter.isValid(parameterValue))
                {
                    Parameter parameter = new Parameter(longestMatchingParameterName, parameterValue);
                    parameters.add(parameter);
                }
            }
        }

        return parameters;
    }

    private Collection<String> getParameterNames()
    {
        return parameterNameToValueFilterMap.keySet();
    }

    private static class AcceptEventsWithPrefixesFilter implements IEventFilter
    {

        private Collection<String> eventPrefixes;

        public AcceptEventsWithPrefixesFilter(Collection<String> eventPrefixes)
        {
            this.eventPrefixes = eventPrefixes;
        }

        @Override
        public boolean accepts(String event)
        {
            for (String eventPrefix : eventPrefixes)
            {
                if (event.startsWith(eventPrefix))
                {
                    return true;
                }
            }
            return false;
        }
    }

    private static class AcceptAllValuesFilter implements IValueFilter
    {
        @Override
        public boolean isValid(String value)
        {
            return true;
        }

    }
}
