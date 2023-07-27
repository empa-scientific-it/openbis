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
package ch.ethz.sis.afsserver.worker.proxy;

import java.util.List;
import java.util.UUID;

import ch.ethz.sis.afsapi.dto.File;
import ch.ethz.sis.afsserver.server.performance.Event;
import ch.ethz.sis.afsserver.worker.AbstractProxy;
import lombok.NonNull;

public class AuditorProxy extends AbstractProxy {

    public AuditorProxy(AbstractProxy nextProxy) {
        super(nextProxy);
    }

    @Override
    public void begin(UUID transactionId) throws Exception {
        auditBefore();
        nextProxy.begin(transactionId);
        auditAfter(null);
    }

    @Override
    public Boolean prepare() throws Exception {
        auditBefore();
        return auditAfter(nextProxy.prepare());
    }

    @Override
    public void commit() throws Exception {
        auditBefore();
        nextProxy.commit();
        auditAfter(null);
    }

    @Override
    public void rollback() throws Exception {
        auditBefore();
        nextProxy.rollback();
        auditAfter(null);
    }

    @Override
    public List<UUID> recover() throws Exception {
        auditBefore();
        return auditAfter(nextProxy.recover());
    }

    @Override
    public String login(@NonNull String userId, @NonNull String password) throws Exception {
        auditBefore();
        return auditAfter(nextProxy.login(userId, password));
    }

    @Override
    public Boolean isSessionValid() throws Exception {
        auditBefore();
        return auditAfter(nextProxy.isSessionValid());
    }

    @Override
    public Boolean logout() throws Exception {
        auditBefore();
        return auditAfter(nextProxy.logout());
    }

    @Override
    public List<File> list(@NonNull String owner, @NonNull String source, @NonNull Boolean recursively) throws Exception {
        auditBefore();
        return auditAfter(nextProxy.list(owner, source, recursively));
    }

    @Override
    public byte[] read(@NonNull String owner, @NonNull String source, @NonNull Long offset, @NonNull Integer limit) throws Exception {
        auditBefore();
        return auditAfter(nextProxy.read(owner, source, offset, limit));
    }

    @Override
    public Boolean write(@NonNull String owner, @NonNull String source, @NonNull Long offset, @NonNull byte[] data, @NonNull byte[] md5Hash) throws Exception {
        auditBefore();
        return auditAfter(nextProxy.write(owner, source, offset, data, md5Hash));
    }

    @Override
    public Boolean delete(@NonNull String owner, @NonNull String source) throws Exception {
        auditBefore();
        return auditAfter(nextProxy.delete(owner, source));
    }

    @Override
    public Boolean copy(@NonNull String sourceOwner, @NonNull String source, @NonNull String targetOwner, @NonNull String target) throws Exception {
        auditBefore();
        return auditAfter(nextProxy.copy(sourceOwner, source, targetOwner, target));
    }

    @Override
    public Boolean move(@NonNull String sourceOwner, @NonNull String source, @NonNull String targetOwner, @NonNull String target) throws Exception {
        auditBefore();
        return auditAfter(nextProxy.move(sourceOwner, source, targetOwner, target));
    }

    @Override
    public @NonNull Boolean create(@NonNull final String owner, @NonNull final String source, @NonNull final Boolean directory) throws Exception
    {
        auditBefore();
        return auditAfter(nextProxy.create(owner, source, directory));
    }

    private void auditBefore() {
        Class clazz = nextProxy.getClass();
        Event event = null;

        if (clazz == LogProxy.class) { // If next is LogProxy last was ApiServer
            event = Event.Api;
        } else if (clazz == AuthenticationProxy.class) { // If next is AuthenticationProxy last was LogProxy
            event = Event.Log;
        } else if (clazz == ValidationProxy.class) { // If next is CorrectnessProxy last was AuthenticationProxy
            event = Event.Authentication;
        } else if (clazz == AuthorizationProxy.class) { // If next is AuthorizationProxy last was CorrectnessProxy
            event = Event.Validation;
        } else if (clazz != null) { // If next is not null, last was AuthorizationProxy
            event = Event.Authorization;
        }

        workerContext.getPerformanceAuditor().audit(event);
    }

    private <RETURN> RETURN auditAfter(RETURN r) {
        Class clazz = nextProxy.getClass();
        if (clazz == LogProxy.class) {
        } else if (clazz == AuthenticationProxy.class) {
        } else if (clazz == ValidationProxy.class) {
        } else if (clazz == AuthorizationProxy.class) {
        } else if (clazz != null) { // If next is not null, is the database proxy
            workerContext.getPerformanceAuditor().audit(Event.Work);
        }
        return r;
    }
}
