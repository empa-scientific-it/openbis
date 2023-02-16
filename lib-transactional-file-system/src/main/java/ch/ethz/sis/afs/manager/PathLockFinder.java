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
package ch.ethz.sis.afs.manager;

import ch.ethz.sis.shared.io.IOUtils;
import ch.ethz.sis.afs.dto.Lock;
import ch.ethz.sis.afs.exception.AFSExceptions;

import java.util.*;

import static ch.ethz.sis.afs.exception.AFSExceptions.NotAPath;

class PathLockFinder implements HierarchicalLockFinder<UUID, String> {

    private Map<String, Lock<UUID, String>> hierarchicallyExclusiveLocks;
    private Map<String, Lock<UUID, String>> hierarchyExclusivityBlockedResouces;
    private Map<Lock<UUID, String>, List<String>> hierarchyExclusivityBlockedByLock;

    public PathLockFinder() {
        hierarchicallyExclusiveLocks = new HashMap<>();
        hierarchyExclusivityBlockedByLock = new HashMap<>();
        hierarchyExclusivityBlockedResouces = new HashMap<>();
    }

    public Map<String, Lock<UUID, String>> getHierarchicallyExclusiveLocks() {
        return hierarchicallyExclusiveLocks;
    }

    static List<String> getParentSubPaths(String path) {
        String[] pathParts = path.split(IOUtils.PATH_SEPARATOR_AS_STRING);
        List<String> subPaths = new ArrayList<>();

        StringBuilder pathBuilder = new StringBuilder();
        if (path.startsWith(IOUtils.ABSOLUTE_PATH_ROOT)) {
            pathBuilder.append(IOUtils.ABSOLUTE_PATH_ROOT);
        } else if (path.startsWith(IOUtils.RELATIVE_PATH_ROOT)) {
            pathBuilder.append(IOUtils.RELATIVE_PATH_ROOT);
        } else {
            AFSExceptions.throwInstance(NotAPath, path);
        }

        for (int pIdx = 1; pIdx < pathParts.length; pIdx++) {
            if (subPaths.size() > 0) {
                pathBuilder.append(IOUtils.PATH_SEPARATOR);
            }
            pathBuilder.append(pathParts[pIdx]);
            subPaths.add(pathBuilder.toString());
        }

        return subPaths;
    }

    @Override
    public Lock<UUID, String> getHierarchicallyExclusiveLock(Lock<UUID, String> lock) {
        List<String> paths = getParentSubPaths(lock.getResource());
        for (String path : paths) {
            if (hierarchyExclusivityBlockedResouces.containsKey(path)) {
                return hierarchyExclusivityBlockedResouces.get(path);
            }
        }
        //Root case, root can only be lock explicitly
        if (hierarchicallyExclusiveLocks.containsKey(lock.getResource())) {
            return hierarchicallyExclusiveLocks.get(lock.getResource());
        }
        return null;
    }

    @Override
    public void add(Lock<UUID, String> lock) {
        boolean added = hierarchicallyExclusiveLocks.put(lock.getResource(), lock) == null;
        if (added) {
            List<String> resourcesBlocked = getParentSubPaths(lock.getResource());
            hierarchyExclusivityBlockedByLock.put(lock, resourcesBlocked);
            for (String resourceBlocked : resourcesBlocked) {
                hierarchyExclusivityBlockedResouces.put(resourceBlocked, lock);
            }
        }
    }

    @Override
    public void remove(Lock<UUID, String> lock) {
        boolean removed = hierarchicallyExclusiveLocks.remove(lock.getResource(), lock);
        if (removed) {
            List<String> resourcesBlocked = hierarchyExclusivityBlockedByLock.get(lock);
            for (String resourceBlocked : resourcesBlocked) {
                hierarchyExclusivityBlockedResouces.remove(resourceBlocked);
            }
        }
    }
}
