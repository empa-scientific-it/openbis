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

define([ "stjs", "as/dto/common/search/StringFieldSearchCriteria", "as/dto/common/search/SearchFieldType" ], function(stjs, StringFieldSearchCriteria, SearchFieldType) {
	var AnyStringPropertySearchCriteria = function() {
		StringFieldSearchCriteria.call(this, "any string", SearchFieldType.ANY_PROPERTY);
	};
	stjs.extend(AnyStringPropertySearchCriteria, StringFieldSearchCriteria, [ StringFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.AnyStringPropertySearchCriteria';
		constructor.serialVersionUID = 1;

		prototype.thatMatches = function (text) {
			var StringMatchesValue = require("as/dto/common/search/StringMatchesValue");
			this.setFieldValue(new StringMatchesValue(text));
		}
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return AnyStringPropertySearchCriteria;
})