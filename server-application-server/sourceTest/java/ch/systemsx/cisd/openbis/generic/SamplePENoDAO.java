/*
 * Copyright ETH 2023 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic;

import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleRelationshipPE;

import java.util.HashSet;
import java.util.Set;

/*
 * Many tests have never used the DB, they lack a lot of necessary constraints.
 *
 * Under a real scenario those tests will actually throw errors.
 *
 * This abstraction allows them to avoid using the introduced DAO behaving as naively as before introducing it.
 */
public class SamplePENoDAO extends SamplePE {
//    @Override
//    protected Set<SampleRelationshipPE> getSampleChildRelationships() {
//        if (childRelationships == null) {
//            childRelationships = new HashSet<>();
//        }
//        return childRelationships;
//    }
//
//    @Override
//    protected Set<SampleRelationshipPE> getSampleParentRelationships() {
//        if(parentRelationships == null) {
//            parentRelationships = new HashSet<>();
//        }
//        return parentRelationships;
//    }
//
//    @Override
//    public void addChildRelationship(final SampleRelationshipPE relationship)
//    {
//        relationship.setParentSample(this);
//        getSampleChildRelationships().add(relationship);
//    }
//
//    @Override
//    public void addParentRelationship(final SampleRelationshipPE relationship)
//    {
//        relationship.setChildSample(this);
//        getSampleParentRelationships().add(relationship);
//    }
}
