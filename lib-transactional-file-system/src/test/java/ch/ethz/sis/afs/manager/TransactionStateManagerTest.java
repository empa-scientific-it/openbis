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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TransactionStateManagerTest extends AbstractTransactionConnectionTest {

    @Test
    public void operation_state_new_succeed() throws Exception {
        assertEquals(getTransactionConnection().getState(), State.New);
    }

    @Test
    public void operation_state_begin_succeed() throws Exception {
        begin();
        assertEquals(getTransactionConnection().getState(), State.Begin);
    }

    @Test
    public void operation_state_prepare_succeed() throws Exception {
        begin();
        prepare();
        assertEquals(getTransactionConnection().getState(), State.Prepare);
    }

    @Test
    public void operation_state_rollback_succeed() throws Exception {
        begin();
        prepare();
        rollback();
        assertEquals(getTransactionConnection().getState(), State.Rollback);
    }

    @Test
    public void operation_state_commit_succeed() throws Exception {
        begin();
        commit();
        assertEquals(getTransactionConnection().getState(), State.Executed);
    }

    @Test
    public void operation_state_commitPrepared_succeed() throws Exception {
        begin();
        prepare();
        commit();
        assertEquals(getTransactionConnection().getState(), State.Executed);
    }

    @Test
    public void operation_state_commit_reuse_succeed() throws Exception {
        begin();
        prepare();
        commit();
        begin();
        assertEquals(getTransactionConnection().getState(), State.Begin);
    }

    @Test
    public void operation_state_rollback_reuse_succeed() throws Exception {
        begin();
        prepare();
        rollback();
        begin();
        assertEquals(getTransactionConnection().getState(), State.Begin);
    }

    @Test(expected = RuntimeException.class)
    public void operation_state_begin_reuse_fails() throws Exception {
        begin();
        begin();
    }

    @Test
    public void operation_state_prepare_reuse_succeed() throws Exception {
        begin();
        prepare();
        begin();
    }
}
