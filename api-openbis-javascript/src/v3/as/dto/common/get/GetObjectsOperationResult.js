/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/operation/IOperationResult" ], function(stjs, IOperationResult) {
	var GetObjectsOperationResult = function(objectMap) {
		if (objectMap) {
			this.ids = [];
			this.objects = [];

			for ( var id in objectMap) {
				this.ids.push(id);
				this.objects.push(objectMap[id]);
			}
		} else {
			this.ids = null;
			this.objects = null;
		}
	};
	stjs.extend(GetObjectsOperationResult, null, [ IOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.get.GetObjectsOperationResult';
		prototype.ids = null;
		prototype.objects = null;
		prototype.getObjectMap = function() {
			var objectMap = {};

			if (this.ids && this.objects) {
				var idLength = this.ids.length;
				var objectLength = this.objects.length;
				var length = Math.min(idLength, objectLength);

				for (var i = 0; i < length; i++) {
					objectMap[this.ids[i]] = this.objects[i];
				}
			}

			return objectMap;
		};
		prototype.getMessage = function() {
			return "GetObjectsOperationResult";
		};
	}, {
		ids : {
			name : "List",
			arguments : [ "IObjectId" ]
		},
		objects : {
			name : "List",
			arguments : [ "Object" ]
		}
	});
	return GetObjectsOperationResult;
})