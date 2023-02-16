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
package ch.ethz.sis.shared.reflect;

import java.lang.reflect.Method;
import java.util.Set;

public class Reflect {

    public static <T> Set<String> getMethodNames(Class<T> clazz) {
        Method[] methods = clazz.getMethods();
        String[] methodNames = new String[methods.length];
        for (int i = 0; methods.length > i; i++) {
            methodNames[i] = methods[i].getName();
        }

        return Set.of(methodNames);
    }

}
