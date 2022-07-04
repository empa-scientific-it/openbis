package ch.ethz.sis.openbis.generic.server.xls.importxls.utils;

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
            if(!this.isHeader(header)) {
                throw new UserFailureException("Header " + header + " is not an attribute.");
            }
        }

        for (IAttribute attribute: attributes) {
            if (attribute.isMandatory() && !headers.keySet().contains(attribute.getHeaderName())) {
                throw new UserFailureException("Header " + attribute.getHeaderName() + " is missing.");
            }
        }
    }

}
