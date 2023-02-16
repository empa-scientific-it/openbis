/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.shared.startup;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Configuration {

    private final Map<Enum, Object> sharables = new ConcurrentHashMap<>();
    private final Properties properties = new Properties();

    public <E extends Enum<E>> Configuration(List<Class<E>> mandatoryParametersClasses, String pathToConfigurationFile) {
        List<E> parameters = new ArrayList<>();
        for (Class<E> parametersClass : mandatoryParametersClasses) {
            parameters.addAll(Arrays.asList(parametersClass.getEnumConstants()));
        }

        try (InputStream inputStream = new FileInputStream(pathToConfigurationFile)) {
            properties.load(inputStream);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        for (E parameter : parameters) {
            String value = properties.getProperty(parameter.name());
            if (value == null) {
                throw new RuntimeException("Failed to load '" + parameter + "' from config file '" + pathToConfigurationFile + "'");
            }
        }
    }

    public Configuration(Map<Enum, String> values) {
        for (Enum key:values.keySet()) {
            properties.setProperty(key.name(), values.get(key));
        }
    }

    public <E extends Enum<E>> void setProperty(E parameter, String value) {
        properties.setProperty(parameter.name(), value);
    }

    private <E extends Enum<E>> String getProperty(E parameter) {
        return properties.getProperty(parameter.name());
    }

    public <E extends Enum<E>> String getStringProperty(E parameter) {
        return getProperty(parameter);
    }

    public <E extends Enum<E>> int getIntegerProperty(E parameter) {
        return Integer.parseInt(getProperty(parameter));
    }

    public <E extends Enum<E>, I> I getSharableInstance(E parameter)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        I sharable = (I) sharables.get(parameter);
        if (sharable == null) {
            String value = getProperty(parameter);
            if (value != null) {
                sharable = (I) Class.forName(value).newInstance();
                sharables.put(parameter, sharable);
            }
        }
        return sharable;
    }

    public <E extends Enum<E>, I> I getInstance(E parameter) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        String value = getProperty(parameter);
        if (value != null) {
            return (I) Class.forName(value).newInstance();
        } else {
            return null;
        }
    }

    public <E extends Enum<E>> Class<?> getInterfaceClass(E parameter) throws ClassNotFoundException {
        String value = getProperty(parameter);
        if (value != null) {
            return Class.forName(value);
        } else {
            return null;
        }
    }
}