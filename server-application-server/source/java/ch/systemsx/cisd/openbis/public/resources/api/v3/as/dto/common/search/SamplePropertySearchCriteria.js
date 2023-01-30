/*
 * Copyright 2023 ETH Zuerich, SIS
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

define([ "stjs", "as/dto/common/search/AbstractFieldSearchCriteria", "as/dto/common/search/SearchFieldType" ],
	function(stjs, AbstractFieldSearchCriteria, SearchFieldType) {
	var SamplePropertySearchCriteria = function(propertyName) {
		AbstractFieldSearchCriteria.call(this, propertyName, SearchFieldType.PROPERTY);
	};
	stjs.extend(SamplePropertySearchCriteria, AbstractFieldSearchCriteria, [ AbstractFieldSearchCriteria ],
		function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.SamplePropertySearchCriteria';
		constructor.serialVersionUID = 1;

		prototype.thatEquals = function (value) {
			this.setFieldValue(value);
		}
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return SamplePropertySearchCriteria;
})