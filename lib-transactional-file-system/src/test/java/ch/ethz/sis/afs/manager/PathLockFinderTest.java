/*
 * Copyright 2022 ETH Zürich, SIS
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

package ch.ethz.sis.afs.manager;

import ch.ethz.sis.shared.io.IOUtils;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class PathLockFinderTest {

    @Test
    public void getSubPaths_relativeRoot() {
        String path = IOUtils.RELATIVE_PATH_ROOT;
        List<String> actual = new PathLockFinder().getParentSubPaths(path);
        assertEquals(List.of(), actual);
    }

    @Test
    public void getSubPaths_relative() {
        String path = "./A/B/C/D.txt";
        List<String> actual = new PathLockFinder().getParentSubPaths(path);
        assertEquals(List.of("./A", "./A/B", "./A/B/C", "./A/B/C/D.txt"), actual);
    }

    @Test
    public void getSubPaths_absoluteRoot() {
        String path = IOUtils.ABSOLUTE_PATH_ROOT;
        List<String> actual = new PathLockFinder().getParentSubPaths(path);
        assertEquals(List.of(), actual);
    }

    @Test
    public void getSubPaths_absolute() {
        String path = "/A/B/C/D.txt";
        List<String> actual = new PathLockFinder().getParentSubPaths(path);
        assertEquals(List.of("/A", "/A/B", "/A/B/C", "/A/B/C/D.txt"), actual);
    }

    @Test(expected = RuntimeException.class)
    public void getSubPaths_invalidPath_exception() {
        String path = "A/B/C/D.txt";
        new PathLockFinder().getParentSubPaths(path);
    }
}
