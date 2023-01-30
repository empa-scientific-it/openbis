/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.authorization;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;

import org.apache.log4j.Level;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.Capability;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * Tests for {@link CapabilityMap}.
 * 
 * @author Bernd Rinn
 */
public class CapabilityMapTest
{
    private BufferedAppender logRecorder;

    @BeforeClass
    public void init()
    {
        LogInitializer.init();
    }

    @Capability("A")
    void dummyA1()
    {
    }

    @Capability("A")
    void dummyA2()
    {
    }

    @Capability("B")
    void dummyB(String dummy)
    {
    }

    void dummyC()
    {
    }

    @BeforeMethod
    public void startUp()
    {
        logRecorder = LogRecordingUtils.createRecorder("%-5p %c - %m%n", Level.DEBUG);
    }

    @Test
    public void testHappyCase() throws SecurityException, NoSuchMethodException
    {
        CapabilityMap capMap =
                new CapabilityMap(Arrays.asList("A: SPACE_POWER_USER\t", "# Some comment", "",
                        " B  INSTANCE_ETL_SERVER"), "<memory>", new TestAuthorizationConfig(false, false));

        assertEquals("", logRecorder.getLogContent());
        assertEquals(
                RoleWithHierarchy.SPACE_POWER_USER,
                capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod("dummyA1"), null).toArray()[0]);
        assertEquals(
                RoleWithHierarchy.SPACE_POWER_USER,
                capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod("dummyA2"), null).toArray()[0]);
        assertEquals(
                RoleWithHierarchy.INSTANCE_ETL_SERVER,
                capMap.tryGetRoles(CapabilityMapTest.class
                        .getDeclaredMethod("dummyB", String.class), null).toArray()[0]);
        assertNull(capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod("dummyC"), null));
    }

    @Test
    public void testHappyCaseAlternative() throws SecurityException, NoSuchMethodException
    {
        CapabilityMap capMap =
                new CapabilityMap(Arrays.asList("A:SPACE_POWER_USER\t", "# Some comment", "",
                        " B  INSTANCE_ETL_SERVER"), "<memory>", new TestAuthorizationConfig(false, false));

        assertEquals("", logRecorder.getLogContent());
        assertEquals(
                RoleWithHierarchy.SPACE_POWER_USER,
                capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod("dummyA1"), null).toArray()[0]);
        assertEquals(
                RoleWithHierarchy.SPACE_POWER_USER,
                capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod("dummyA2"), null).toArray()[0]);
        assertEquals(
                RoleWithHierarchy.INSTANCE_ETL_SERVER,
                capMap.tryGetRoles(CapabilityMapTest.class
                        .getDeclaredMethod("dummyB", String.class), null).toArray()[0]);
        assertNull(capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod("dummyC"), null));
    }

    @Test
    public void testHappyCaseAlternative2() throws SecurityException, NoSuchMethodException
    {
        CapabilityMap capMap =
                new CapabilityMap(Arrays.asList("A :SPACE_POWER_USER\t", "# Some comment", "",
                        " B  INSTANCE_ETL_SERVER"), "<memory>", new TestAuthorizationConfig(false, false));

        assertEquals("", logRecorder.getLogContent());
        assertEquals(
                RoleWithHierarchy.SPACE_POWER_USER,
                capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod("dummyA1"), null).toArray()[0]);
        assertEquals(
                RoleWithHierarchy.SPACE_POWER_USER,
                capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod("dummyA2"), null).toArray()[0]);
        assertEquals(
                RoleWithHierarchy.INSTANCE_ETL_SERVER,
                capMap.tryGetRoles(CapabilityMapTest.class
                        .getDeclaredMethod("dummyB", String.class), null).toArray()[0]);
        assertNull(capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod("dummyC"), null));
    }

    @Test
    public void testHappyCaseAlternative3() throws SecurityException, NoSuchMethodException
    {
        CapabilityMap capMap =
                new CapabilityMap(Arrays.asList("A : SPACE_POWER_USER\t", "# Some comment", "",
                        " b  instance_etl_server"), "<memory>", new TestAuthorizationConfig(false, false));

        assertEquals("", logRecorder.getLogContent());
        assertEquals(
                RoleWithHierarchy.SPACE_POWER_USER,
                capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod("dummyA1"), null).toArray()[0]);
        assertEquals(
                RoleWithHierarchy.SPACE_POWER_USER,
                capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod("dummyA2"), null).toArray()[0]);
        assertEquals(
                RoleWithHierarchy.INSTANCE_ETL_SERVER,
                capMap.tryGetRoles(CapabilityMapTest.class
                        .getDeclaredMethod("dummyB", String.class), null).toArray()[0]);
        assertNull(capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod("dummyC"), null));
    }

    @Test
    public void testHappyCaseAlternative4() throws SecurityException, NoSuchMethodException
    {
        CapabilityMap capMap =
                new CapabilityMap(Arrays.asList("A  SPACE_POWER_USER\t",
                        " A  INSTANCE_ETL_SERVER"), "<memory>", new TestAuthorizationConfig(false, false));

        assertEquals("", logRecorder.getLogContent());
        assertEquals(2, capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod("dummyA1"), null)
                .size());
        assertTrue(capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod("dummyA1"), null)
                .contains(RoleWithHierarchy.SPACE_POWER_USER));
        assertTrue(capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod("dummyA1"), null)
                .contains(RoleWithHierarchy.INSTANCE_ETL_SERVER));
    }

    @Test
    public void testHappyCaseAlternative5() throws SecurityException, NoSuchMethodException
    {
        CapabilityMap capMap =
                new CapabilityMap(Arrays.asList("A : SPACE_POWER_USER,INSTANCE_ETL_SERVER\t"),
                        "<memory>", new TestAuthorizationConfig(false, false));

        assertEquals("", logRecorder.getLogContent());
        assertEquals(2, capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod("dummyA1"), null)
                .size());
        assertTrue(capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod("dummyA1"), null)
                .contains(RoleWithHierarchy.SPACE_POWER_USER));
        assertTrue(capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod("dummyA1"), null)
                .contains(RoleWithHierarchy.INSTANCE_ETL_SERVER));
    }

    @Test
    public void testParameterRoles() throws Exception
    {
        CapabilityMap capMap =
                new CapabilityMap(Arrays.asList("A\tSPACE_POWER_USER,INSTANCE_ETL_SERVER; "
                        + "sample = SPACE_USER, SPACE_ETL_SERVER"), "<memory>", new TestAuthorizationConfig(false, false));

        assertEquals("", logRecorder.getLogContent());
        assertRoles("[INSTANCE_ETL_SERVER, SPACE_POWER_USER]", capMap, "dummyA1", null);
        assertRoles("[SPACE_USER, SPACE_ETL_SERVER]", capMap, "dummyA1", "SAMPLE");
    }

    @Test
    public void testOnlyParameterRoles() throws Exception
    {
        CapabilityMap capMap =
                new CapabilityMap(Arrays.asList("a : sample = SPACE_USER, SPACE_ETL_SERVER"), "<memory>", new TestAuthorizationConfig(false, false));

        assertEquals("", logRecorder.getLogContent());
        assertNoRoles(capMap, "dummyA1", null);
        assertRoles("[SPACE_USER, SPACE_ETL_SERVER]", capMap, "dummyA1", "SAMPLE");
    }

    @Test
    public void testInvalidMapLines() throws SecurityException, NoSuchMethodException
    {
        CapabilityMap capMap =
                new CapabilityMap(Arrays.asList(
                        "CapabilityMapTest.dummyA: SPACE_POWER_USER #wrong",
                        "CapabilityMapTest.dummyB  NO_ROLE"), "<memory>", new TestAuthorizationConfig(false, false));

        assertEquals("WARN  OPERATION.CapabilityMap - Ignoring mal-formed line "
                + "'CapabilityMapTest.dummyA: SPACE_POWER_USER #wrong' in <memory> "
                + "[role 'SPACE_POWER_USER #WRONG' doesn't exist].\n"
                + "WARN  OPERATION.CapabilityMap - Ignoring mal-formed line "
                + "'CapabilityMapTest.dummyB  NO_ROLE' in <memory> [role 'NO_ROLE' doesn't exist].",
                logRecorder.getLogContent());
        assertNull(capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod("dummyA1"), null));
        assertNull(capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod("dummyB",
                String.class), null));
        assertNull(capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod("dummyC"), null));
    }

    @Test
    public void testUserRoleDisabled() throws SecurityException, NoSuchMethodException
    {
        CapabilityMap capMap =
                new CapabilityMap(Arrays.asList("A: INSTANCE_DISABLED\t"), "<memory>", new TestAuthorizationConfig(false, false));

        assertEquals("", logRecorder.getLogContent());
        assertEquals(
                RoleWithHierarchy.INSTANCE_DISABLED,
                capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod("dummyA1"), null).toArray()[0]);
        assertTrue(capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod("dummyA1"), null)
                .toArray(new RoleWithHierarchy[0])[0]
                        .getRoles().isEmpty());
    }

    private void assertRoles(String expectedRoles, CapabilityMap capMap, String methodName,
            String argumentNameOrNull) throws Exception
    {
        TreeSet<RoleWithHierarchy> set = new TreeSet<RoleWithHierarchy>();
        Collection<RoleWithHierarchy> roles = capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod(methodName), argumentNameOrNull);
        if (roles != null)
        {
            set.addAll(roles);
        }
        assertEquals(expectedRoles, set.toString());
    }

    private void assertNoRoles(CapabilityMap capMap, String methodName, String argumentNameOrNull) throws Exception
    {
        Collection<RoleWithHierarchy> roles = capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod(methodName), argumentNameOrNull);
        assertEquals(null, roles);
    }

}
