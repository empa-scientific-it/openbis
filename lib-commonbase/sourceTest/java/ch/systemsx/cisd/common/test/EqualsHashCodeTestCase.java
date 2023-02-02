/*
 * Copyright 2002-2003 Vladimir R. Bossicard.  All rights reserved.
 *
 * Licensed under the JUnit-addons Software License, Version 1.0
 *     (based on the Apache Software License, Version 1.1)
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by Vladimir R.
 *        Bossicard as well as other contributors
 *        (http://junit-addons.sourceforge.net/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The name "JUnit-addons" must not be used to endorse or promote
 *    products derived from this software without prior written
 *    permission. For written permission, please contact
 *    vbossica@users.sourceforge.net.
 *
 * 5. Products derived from this software may not be called "JUnit-addons"
 *    nor may "JUnit-addons" appear in their names without prior written
 *    permission of the project managers.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ======================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals.  For more information on the JUnit-addons Project, please
 * see <http://junit-addons.sourceforge.net/>.
 */

package ch.systemsx.cisd.common.test;

import static org.testng.AssertJUnit.assertNotSame;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Extend me in order to test a class's functional compliance with the <code>equals</code> and <code>hashCode</code> contract.
 * <p>
 * Override my {@link #createInstance() createInstance} and {@link #createNotEqualInstance() createNotEqualInstance} methods to provide me with
 * objects to test against. Both methods should return objects that are of the same class.
 * <p>
 * <b>WARNING</b>: Extend me only if your class overrides <code>equals</code> to test for equivalence. If your class's <code>equals</code> tests for
 * identity or preserves the behavior from <code>Object</code>, I'm not interested, because I expect <code>createInstance</code> to return equivalent
 * but distinct objects.
 * 
 * @see java.lang.Object#equals(Object)
 * @see java.lang.Object#hashCode()
 * @author Christian Ribeaud
 */
public abstract class EqualsHashCodeTestCase<T>
{

    private T eq1;

    private T eq2;

    private T eq3;

    private T neq;

    private static final int NUM_ITERATIONS = 20;

    /**
     * Creates and returns an instance of the class under test.
     * 
     * @return a new instance of the class under test; each object returned from this method should compare equal to each other.
     * @throws Exception
     */
    protected abstract T createInstance() throws Exception;

    /**
     * Creates and returns an instance of the class under test.
     * 
     * @return a new instance of the class under test; each object returned from this method should compare equal to each other, but not to the
     *         objects returned from {@link #createInstance() createInstance}.
     * @throws Exception
     */
    protected abstract T createNotEqualInstance() throws Exception;

    /**
     * Sets up the test fixture.
     * 
     * @throws Exception
     */
    @BeforeClass
    public void setUp() throws Exception
    {
        eq1 = createInstance();
        eq2 = createInstance();
        eq3 = createInstance();
        neq = createNotEqualInstance();

        assert eq1 != null : "createInstance() returned null";
        assert eq2 != null : "2nd createInstance() returned null";
        assert eq3 != null : "3rd createInstance() returned null";
        assert neq != null : "createNotEqualInstance() returned null";

        assertNotSame(eq1, eq2);
        assertNotSame(eq1, eq3);
        assertNotSame(eq1, neq);
        assertNotSame(eq2, eq3);
        assertNotSame(eq2, neq);
        assertNotSame(eq3, neq);

        assert eq1.getClass().equals(eq2.getClass()) : "1st and 2nd equal instances of different classes";
        assert eq1.getClass().equals(eq3.getClass()) : "1st and 3nd equal instances of different classes";
        assert eq1.getClass().equals(neq.getClass()) : "1st equal instance and not-equal instance of different classes";
    }

    /**
     * Tests whether <code>equals</code> holds up against a new <code>Object</code> (should always be <code>false</code>).
     */
    @Test
    public final void testEqualsAgainstNewObject()
    {
        Object o = new Object();
        assertNotSame(eq1, o);
        assertNotSame(eq2, o);
        assertNotSame(eq3, o);
        assertNotSame(neq, o);
    }

    /**
     * Tests whether <code>equals</code> holds up against <code>null</code>.
     */
    @Test
    public final void testEqualsAgainstNull()
    {
        assertNotSame("1st vs. null", eq1, null);
        assertNotSame("2nd vs. null", eq2, null);
        assertNotSame("3rd vs. null", eq3, null);
        assertNotSame("not-equal vs. null", neq, null);
    }

    /**
     * Tests whether <code>equals</code> holds up against objects that should not compare equal.
     */
    @Test
    public final void testEqualsAgainstUnequalObjects()
    {
        assert eq1.equals(neq) == false : "1st vs. not-equal";
        assert eq2.equals(neq) == false : "2nd vs. not-equal";
        assert eq3.equals(neq) == false : "3rd vs. not-equal";

        assert neq.equals(eq1) == false : "not-equal vs. 1st";
        assert neq.equals(eq2) == false : "not-equal vs. 2nd";
        assert neq.equals(eq3) == false : "not-equal vs. 3rd";
    }

    /**
     * Tests whether <code>equals</code> is <em>consistent</em>.
     */
    @Test
    public final void testEqualsIsConsistentAcrossInvocations()
    {
        for (int i = 0; i < NUM_ITERATIONS; ++i)
        {
            testEqualsAgainstNewObject();
            testEqualsAgainstNull();
            testEqualsAgainstUnequalObjects();
            testEqualsIsReflexive();
            testEqualsIsSymmetricAndTransitive();
        }
    }

    /**
     * Tests whether <code>equals</code> is <em>reflexive</em>.
     */
    @Test
    public final void testEqualsIsReflexive()
    {
        assert eq1.equals(eq1) : "1st equal instance";
        assert eq2.equals(eq2) : "2nd equal instance";
        assert eq3.equals(eq3) : "3rd equal instance";
        assert neq.equals(neq) : "not-equal equal instance";
    }

    /**
     * Tests whether <code>equals</code> is <em>symmetric</em> and <em>transitive</em>.
     */
    @Test
    public final void testEqualsIsSymmetricAndTransitive()
    {
        assert eq1.equals(eq2) : "1st vs. 2nd";
        assert eq2.equals(eq1) : "2nd vs. 1st";

        assert eq1.equals(eq3) : "1st vs. 3rd";
        assert eq3.equals(eq1) : "3rd vs. 1st";

        assert eq2.equals(eq3) : "2nd vs. 3rd";
        assert eq3.equals(eq2) : "3rd vs. 2nd";
    }

    /**
     * Tests the <code>hashCode</code> contract.
     */
    @Test
    public final void testHashCodeContract()
    {
        assert eq1.hashCode() == eq2.hashCode() : "1st vs. 2nd";
        assert eq1.hashCode() == eq3.hashCode() : "1st vs. 3rd";
        assert eq2.hashCode() == eq3.hashCode() : "2nd vs. 3rd";
    }

    /**
     * Tests the consistency of <code>hashCode</code>.
     */
    @Test
    public final void testHashCodeIsConsistentAcrossInvocations()
    {
        int eq1Hash = eq1.hashCode();
        int eq2Hash = eq2.hashCode();
        int eq3Hash = eq3.hashCode();
        int neqHash = neq.hashCode();

        for (int i = 0; i < NUM_ITERATIONS; ++i)
        {
            assert eq1Hash == eq1.hashCode() : "1st equal instance";
            assert eq2Hash == eq2.hashCode() : "2nd equal instance";
            assert eq3Hash == eq3.hashCode() : "3rd equal instance";
            assert neqHash == neq.hashCode() : "not-equal instance";
        }
    }

}
