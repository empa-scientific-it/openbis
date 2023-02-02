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

package ch.systemsx.cisd.openbis.common.api.server.json.mapping;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;

import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import com.fasterxml.jackson.databind.jsontype.NamedType;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
public class JsonReflectionsBaseTypeToSubTypesMapping implements IJsonBaseTypeToSubTypesMapping
{

    private Map<Class<?>, Set<NamedType>> baseTypeToSubTypesMap;

    private Set<NamedType> allSubTypesSet;

    public JsonReflectionsBaseTypeToSubTypesMapping(String[] prefixes)
    {
        baseTypeToSubTypesMap = createBaseTypeToSubTypesMap(prefixes);
        allSubTypesSet = createAllSubTypesSet(baseTypeToSubTypesMap);
    }

    @Override
    public Set<NamedType> getSubTypes(Class<?> baseType)
    {
        if (Object.class.equals(baseType))
        {
            return allSubTypesSet;
        } else
        {
            return baseTypeToSubTypesMap.get(baseType);
        }
    }

    private static Map<Class<?>, Set<NamedType>> createBaseTypeToSubTypesMap(String[] prefixes)
    {
        ConfigurationBuilder configBuilder = new ConfigurationBuilder();

        FilterBuilder filterBuilder = new FilterBuilder();
        Set<URL> urls = new HashSet<URL>();

        for (String prefix : prefixes)
        {
            urls.addAll(ClasspathHelper.forPackage(prefix));
            filterBuilder.include(FilterBuilder.prefix(prefix));
        }

        int numberOfUsedCores = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);

        configBuilder.addUrls(urls);
        configBuilder.filterInputsBy(filterBuilder);
        configBuilder.setExecutorService(Executors.newFixedThreadPool(numberOfUsedCores));

        Reflections reflections = new Reflections(configBuilder);

        Set<Class<?>> types = reflections.getTypesAnnotatedWith(JsonObject.class, true);
        Map<Class<?>, Set<NamedType>> subTypesMap = new HashMap<Class<?>, Set<NamedType>>();

        if (types != null)
        {
            for (Class<?> baseType : types)
            {
                for (Class<?> subType : types)
                {
                    if (baseType.isAssignableFrom(subType))
                    {
                        Set<NamedType> subTypes = subTypesMap.get(baseType);
                        if (subTypes == null)
                        {
                            subTypes = new HashSet<NamedType>();
                            subTypesMap.put(baseType, subTypes);
                        }

                        JsonObject subTypeAnnotation = subType.getAnnotation(JsonObject.class);
                        subTypes.add(new NamedType(subType, subTypeAnnotation.value()));
                    }
                }
            }
        }

        return subTypesMap;
    }

    private static Set<NamedType> createAllSubTypesSet(
            Map<Class<?>, Set<NamedType>> baseTypeToSubTypesMap)
    {
        Set<NamedType> allSubTypes = new HashSet<NamedType>();
        for (Set<NamedType> subTypes : baseTypeToSubTypesMap.values())
        {
            allSubTypes.addAll(subTypes);
        }
        return allSubTypes;
    }

}
