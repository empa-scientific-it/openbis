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
package ch.ethz.sis.openbis.generic.server.xls.importer.utils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AttributeValidator<E extends IAttribute> {
    private final Set<String> headerNames;

    public AttributeValidator(Class<E> attributeClass) {
        Set<String> headerNamesBuilder = new HashSet<>();
        for (E attribute:attributeClass.getEnumConstants()) {
            headerNamesBuilder.add(attribute.getHeaderName());
        }
        headerNames = Collections.unmodifiableSet(headerNamesBuilder);
    }

    public boolean isHeader(String name) {
        return headerNames.contains(name);
    }

    public Set<String> getHeaderNames() {
        return headerNames;
    }

    public void validateHeaders(E[] attributes, Map<String, Integer> headers) {
        for (String header:headers.keySet()) {
            if(this.isHeader(header)) {
                continue;
            }
            throw new UserFailureException("Header '" + header + "' is not an attribute.");
        }

        for (IAttribute attribute: attributes) {
            if (attribute.isMandatory() && !headers.containsKey(attribute.getHeaderName())) {
                throw new UserFailureException("Header '" + attribute.getHeaderName() + "' is missing.");
            }
        }
    }

    public void validateHeaders(E[] attributes, PropertyTypeSearcher propertyTypeSearcher, Map<String, Integer> headers) {
        for (String header:headers.keySet()) {
            if(this.isHeader(header)) {
               continue;
            }
            if (propertyTypeSearcher.getCode2PropertyType().containsKey(header)) {
                continue;
            }
            if (propertyTypeSearcher.getLabel2PropertyType().containsKey(header)) {
                continue;
            }
            throw new UserFailureException("Header '" + header + "' is neither an attribute, property code or property label.");
        }

        for (IAttribute attribute: attributes) {
            if (attribute.isMandatory() && !headers.containsKey(attribute.getHeaderName())) {
                throw new UserFailureException("Header " + attribute.getHeaderName() + " is missing.");
            }
        }
    }

    public static void validateHeader(String valid, Map<String, Integer> headers) {
        for (String header:headers.keySet()) {
            if (!valid.equals(header)) {
                throw new UserFailureException("Header '" + header + "' is not a valid header. Check for typos, the only valid header is: '" + valid + "'.");
            }
        }
    }

}
