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

package ch.ethz.sis.afs.exception;

import ch.ethz.sis.afs.api.dto.ExceptionType;
import ch.ethz.sis.shared.exception.ExceptionTemplateHolder;

import java.util.List;

import static ch.ethz.sis.afs.api.dto.ExceptionType.*;

public enum AFSExceptions implements ExceptionTemplateHolder {
    FileSystemNotSupported(         RuntimeException.class,         List.of(AdminConfigError),          10001,"File System not supported"),
    NotAPath(                       RuntimeException.class,         List.of(ClientDeveloperCodingError),10002,"Given string %s is not a valid path"),
    PathsOnDifferentVolumes(        RuntimeException.class,         List.of(ClientDeveloperCodingError),10003,"Paths %s and %s are on different volumes"),
    OperationCantBeRecovered(       RuntimeException.class,         List.of(ClientDeveloperCodingError),10004,"Transaction %s can't recover locks for operation %s"),
    OperationNotAddedDueToState(    RuntimeException.class,         List.of(ClientDeveloperCodingError),10005,"The operation %s can't be added, the transaction state is %s instead %s"),
    PathCantBeOperatedAfterDeleted( RuntimeException.class,         List.of(ClientDeveloperCodingError),10006,"Path can't be operated by: %s - After been deleted: %s"),
    PathCantBeOperatedAfterMoved(   RuntimeException.class,         List.of(ClientDeveloperCodingError),10007,"Path can't be operated by: %s - After been moved: %s"),
    PathCantBeOperatedAfterCopied(  RuntimeException.class,         List.of(ClientDeveloperCodingError),10008,"Path can't be operated by: %s - After been copied: %s"),
    PathCantBeReadAfterWritten(     RuntimeException.class,         List.of(ClientDeveloperCodingError),10009,"Path can't be read by: %s - After been written: %s"),
    PathBusy(                       RuntimeException.class,         List.of(ClientDeveloperCodingError),10010,"Path can't be operated by: %s - %s is currently being used"),
    PathIsDirectory(                RuntimeException.class,         List.of(ClientDeveloperCodingError),10011,"Path can't be operated by: %s - %s is a directory"),
    PathNotDirectory(               RuntimeException.class,         List.of(ClientDeveloperCodingError),10012,"Path can't be operated by: %s - %s is not a directory"),
    PathNotInStore(                 RuntimeException.class,         List.of(ClientDeveloperCodingError),10013,"Path given to: %s - Not in store: %s"),
    PathInStore(                    RuntimeException.class,         List.of(ClientDeveloperCodingError),10014,"Path given to: %s - In store: %s"),
    PathInStoreCantBeRelative(      RuntimeException.class,         List.of(ClientDeveloperCodingError),10015,"Path given to: %s - can't contain '/../': %s"),
    PathNotStartWithRoot(           RuntimeException.class,         List.of(ClientDeveloperCodingError),10016,"Path given to: %s - don't starts with root '/' : %s"),
    MD5NotMatch(                    RuntimeException.class,         List.of(ClientDeveloperCodingError),10017,"MD5 don't match on data given to: %s - for: %s"),
    DeadlockDetected(               RuntimeException.class,         List.of(UserUsageError),            10018,"Deadlock detected, %s is already waiting for %s from %s"),
    TransactionReuse(               RuntimeException.class,         List.of(CoreDeveloperCodingError),  10019,"Transaction with uuid: %s and state: %s was going to be reused");

    private RuntimeExceptionTemplate template;

    AFSExceptions(Class clazz, List<ExceptionType> types, int code, String messageTemplate) {
        this.template = new RuntimeExceptionTemplate(5, clazz, types, code, messageTemplate);
    }

    public RuntimeException getInstance(Object... args) {
        return template.getInstance(args);
    }

    public Exception getCheckedInstance(Object... args) {
        return template.getCheckedInstance(args);
    }

    public static void throwInstance(AFSExceptions exception, Object... args) {
        throw exception.getInstance(args);
    }
}
