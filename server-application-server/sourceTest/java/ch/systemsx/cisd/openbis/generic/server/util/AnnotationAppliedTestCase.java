/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.testng.AssertJUnit;

import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;

/**
 * Test case for test classes testing whether an interface and/or its implementation have all mandatory annotations.
 * 
 * @author Franz-Josef Elmer
 */
public class AnnotationAppliedTestCase extends AssertJUnit
{
    /**
     * Asserts that the specified class is an interface and all its methods have the annotations {@link RolesAllowed} and {@link Transactional}.
     */
    protected void assertMandatoryMethodAnnotations(Class<?> interfaceClass, List<String> exemptMethods)
    {
        assertMandatoryMethodAnnotations(interfaceClass, interfaceClass, exemptMethods);
    }

    /**
     * Asserts that the specified interface class is an interface, the implementing class really implements that interface, and all interface methods
     * or their implementations have the annotations {@link RolesAllowed} and {@link Transactional}.
     */
    protected void assertMandatoryMethodAnnotations(Class<?> interfaceClass,
            Class<?> implementingClass, List<String> exemptMethods)
    {
        assertMandatoryMethodAnnotations(interfaceClass, implementingClass, "", exemptMethods);
    }

    /**
     * Asserts that the specified interface class is an interface, the implementing class really implements that interface, and all interface methods
     * or their implementations have the annotations {@link RolesAllowed} and {@link Transactional}. The specified exceptions are allowed.
     */
    protected void assertMandatoryMethodAnnotations(Class<?> interfaceClass,
            Class<?> implementingClass, String exceptions, List<String> exemptMethods)
    {
        List<Class<? extends Annotation>> mandatoryAnnotations =
                new ArrayList<Class<? extends Annotation>>();
        mandatoryAnnotations.add(RolesAllowed.class);
        mandatoryAnnotations.add(Transactional.class);

        assertMandatoryMethodAnnotations(mandatoryAnnotations, interfaceClass, implementingClass,
                exceptions, exemptMethods);
    }

    /**
     * Asserts that the specified interface class is an interface, the implementing class really implements that interface, and all interface methods
     * or their implementations have the specified annotations. The specified exceptions are allowed.
     */
    protected void assertMandatoryMethodAnnotations(
            List<Class<? extends Annotation>> mandatoryAnnotations, Class<?> interfaceClass,
            Class<?> implementingClass, String exceptions, List<String> exemptMethods)
    {
        final String noMissingAnnotationsMsg =
                "Annotation checking for interface " + interfaceClass.getName()
                        + " and implementing class " + implementingClass.getName()
                        + ": The mandatory annotations doesn't appear in the following methods:\n";
        assertEquals(true, interfaceClass.isInterface());
        assertEquals(true, interfaceClass.isAssignableFrom(implementingClass));

        StringBuilder problems = new StringBuilder(noMissingAnnotationsMsg);
        Method[] declaredMethods = interfaceClass.getDeclaredMethods();
        Arrays.sort(declaredMethods, new Comparator<Method>()
            {
                @Override
                public int compare(Method m0, Method m1)
                {
                    return m0.getName().compareTo(m1.getName());
                }
            });
        for (Method interfaceMethod : declaredMethods)
        {
            if (exemptMethods != null && exemptMethods.contains(interfaceMethod.getName()))
            {
                continue;
            }

            List<String> missingAnnotations = new ArrayList<String>();
            for (Class<? extends Annotation> annotationClass : mandatoryAnnotations)
            {
                if (interfaceMethod.getAnnotation(annotationClass) == null)
                {
                    try
                    {
                        Method implementedMethod =
                                implementingClass.getMethod(interfaceMethod.getName(),
                                        interfaceMethod.getParameterTypes());
                        if (implementedMethod.getAnnotation(annotationClass) == null)
                        {
                            missingAnnotations.add(annotationClass.getSimpleName());
                        }
                    } catch (Exception ex)
                    {
                        fail("Method '" + interfaceMethod.toGenericString() + "' of interface "
                                + interfaceClass.getName()
                                + " is not defined in implementing class "
                                + implementingClass.getName() + ".");
                    }
                }
            }
            if (missingAnnotations.size() > 0)
            {
                problems.append(String.format("%s: %s\n", interfaceMethod.getName(),
                        StringUtils.join(missingAnnotations, ", ")));
            }
        }
        assertEquals(noMissingAnnotationsMsg + exceptions, problems.toString());
    }
}
