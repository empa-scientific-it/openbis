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
package ch.ethz.sis.shared.pool;

public interface Factory<CONFIGURATION_PARAMETERS, FACTORY_PARAMETERS, ELEMENT> {

    /*
     * To be used for factories that need an initial configuration or keep some state, ignore otherwise
     */
    void init(CONFIGURATION_PARAMETERS configurationParameters) throws Exception;

    /*
     * To be implemented by all factories
     */
    ELEMENT create(FACTORY_PARAMETERS factoryParameters) throws Exception;

    /*
     * To be implemented for elements that can be closed/destroyed to recover resources, ignore otherwise
     */
    void destroy(ELEMENT element) throws Exception;

}
