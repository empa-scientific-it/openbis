/**
 * Library
 */
define([ 'jquery', 'underscore' ], function(jquery, _) {
    var NUMERIC_TYPES = [
        "Date",
        "Integer",
        "int",
        "Float",
        "float",
        "Long",
        "long",
        "Double",
        "double",
    ].reduce(function(map, type){
        map[type] = type
        return map
    }, {})

	var Json = function() {
        this.requireFn = require
	}

    Json.prototype.setRequireFn = function(requireFn) {
        this.requireFn = requireFn
    }

	Json.prototype.fromJson = function(jsonType, jsonObject) {
		var dfd = jquery.Deferred();

		var types = {}
		collectTypes(jsonObject, types);

		var moduleNames = Object.keys(types).map(function(type) {
			return typeToModuleName(type);
		});
		this.requireFn(moduleNames, function() {
			var moduleMap = {};

			for (var i = 0; i < arguments.length; i++) {
				var moduleName = moduleNames[i];
				var module = arguments[i];
				moduleMap[moduleName] = module;
			}

			try {
				var jsonObjectMap = {};
				var v3ObjectMap = {};

				// Find all objects with @id.

				collectObjects(jsonObject, jsonObjectMap, moduleMap);

				// Covert objects (from the lowest to the highest @id value).
				// Such order is generated on the server-side but is not
				// guaranteed on be kept on the client-side (e.g. order of
				// entries in maps may be different between textual JSON
				// representation generated on the server-side and in memory JS
				// objects created after parsing such JSON).
				// 
				// Sorting objects by @id value guarantees the same order of
				// processing on both server and client sides. This saves
				// us from situations where in JSON an object @id is used
				// before the object is actually defined.

				var sortedJsonIds = Object.keys(jsonObjectMap).map(Number).sort(function(jsonId1, jsonId2) {
					return jsonId1 - jsonId2;
				});
				sortedJsonIds.forEach(function(jsonId) {
					createJsonObjectWithType(jsonObjectMap[jsonId], v3ObjectMap, moduleMap);
				});

				// Convert the whole JSON using the already converted objects.

				var dto = fromJsonObjectWithTypeOrArrayOrMap(jsonType, jsonObject, v3ObjectMap, moduleMap);
				dfd.resolve(dto);
			} catch (e) {
				dfd.reject(e);
			}

		});

		return dfd.promise();
	}

	Json.prototype.decycle = function(object) {
		return decycleLocal(object, []);
	}

	var collectTypes = function(jsonObject, types) {
		if (jsonObject instanceof Array) {
			jsonObject.forEach(function(item) {
				collectTypes(item, types);
			});
		} else if (jsonObject instanceof Object) {
			Object.keys(jsonObject).forEach(function(key) {
				var value = jsonObject[key];
				if (key == "@type") {
					types[value] = value;
				} else {
					collectTypes(value, types);
				}
			});
		}
	}

	var collectObjects = function(jsonObject, objectMap, modulesMap) {
		if (jsonObject instanceof Array) {
			jsonObject.forEach(function(jsonItem) {
				collectObjects(jsonItem, objectMap, modulesMap);
			});
		} else if (jsonObject instanceof Object) {
			var jsonId = jsonObject["@id"];
			var jsonType = jsonObject["@type"];

			if (jsonId && jsonType) {
				objectMap[jsonId] = jsonObject;
			}

			Object.keys(jsonObject).forEach(function(key) {
				collectObjects(jsonObject[key], objectMap, modulesMap);
			});
		}
	}

	var createJsonObjectWithType = function(jsonObject, objectMap, modulesMap) {
		var jsonId = jsonObject["@id"]
		var jsonType = jsonObject["@type"]

		var moduleName = typeToModuleName(jsonType);
		var module = modulesMap[moduleName];
		var moduleFieldTypeMap = module.$typeDescription || {};

		try {
			// some entities have a mandatory
			// non-null constructor parameters
			var object = new module("");
			object.__stub__ = true;
		} catch (e) {
			throw "Failed deserializing object " + JSON.stringify(jsonObject) + " into type " + jsonType + " with error " + e.message;
		}

		objectMap[jsonId] = object;
		return object;
	}

	var typeToModuleName = function(type) {
		return type.replace(/\./g, '/');
	}

	var fromJsonObjectWithTypeOrArrayOrMap = function(jsonType, jsonObject, objectMap, modulesMap) {
		if (jsonObject instanceof Array) {
			if (jsonType && _.isString(jsonType) && jsonObject.length == 2) {
				return jsonObject[1];
			} else {
				var array = [];
				var jsonType = (jsonType && jsonType["arguments"] && jsonType["arguments"].length > 0) ? jsonType["arguments"][0] : null;

				jsonObject.forEach(function(item, index) {
					var dto = fromJsonObjectWithTypeOrArrayOrMap(jsonType, item, objectMap, modulesMap);
					array.push(dto);
				});
				return array;
			}
		} else if (jsonObject instanceof Object) {
			if (jsonObject["@id"] && jsonObject["@type"]) {
			    var jsonTypeArguments = (jsonType && jsonType["arguments"]) ? jsonType["arguments"] : null
				return fromJsonObjectWithType(jsonTypeArguments, jsonObject, objectMap, modulesMap)
			} else {
				var map = {};
				var jsonType = (jsonType && jsonType["arguments"] && jsonType["arguments"].length > 1) ? jsonType["arguments"][1] : null;

				Object.keys(jsonObject).forEach(function(key) {
					var dto = fromJsonObjectWithTypeOrArrayOrMap(jsonType, jsonObject[key], objectMap, modulesMap);
					map[key] = dto;
				});
				return map;
			}
		} else {
			if (_.isNumber(jsonObject) && jsonType && !NUMERIC_TYPES[jsonType]) {
				if (jsonObject in objectMap) {
					return objectMap[jsonObject];
				} else {
					throw "Object with id: " + JSON.stringify(jsonObject) + " and type: " + jsonType + " haven't been found in cache";
				}
			} else {
				return jsonObject;
			}
		}
	}

	var fromJsonObjectWithType = function(jsonTypeArguments, jsonObject, objectMap, modulesMap) {
		var jsonId = jsonObject["@id"]
		var jsonType = jsonObject["@type"]
		var object = objectMap[jsonId];

		if (object.__stub__) {
			delete object.__stub__;

			var moduleName = typeToModuleName(jsonType);
			var module = modulesMap[moduleName];
			var moduleFieldTypeMap = module.$typeDescription || {};

			for ( var key in jsonObject) {
				var fieldType = moduleFieldTypeMap[key];

				if(_.isFunction(fieldType)){
				    fieldType = fieldType(jsonTypeArguments);
				}

				var fieldValue = jsonObject[key];

				if (object["_" + key] !== undefined) {
					key = "_" + key;
				}

				object[key] = fromJsonObjectWithTypeOrArrayOrMap(fieldType, fieldValue, objectMap, modulesMap);
			}

			return object;
		} else {
			return object;
		}
	};

	var decycleLocal = function(object, references) {
		if (object === null) {
			return object;
		}
		var index = _.indexOf(references, object);
		if (index >= 0) {
			return index;
		}
		if (_.isArray(object)) {
			return _.map(object, function(el, key) {
				return decycleLocal(el, references)
			});
		} else if (_.isObject(object)) {
			var result = {};
			if (object["@type"] != null) {
				var id = references.length;
				result["@id"] = id;
				references.push(object);
			}
			for ( var i in object) {
				if (_.isFunction(object[i]) === false && i !== "@id") {
					var value = decycleLocal(object[i], references);
					if (i.indexOf("_") == 0) {
						i = i.substring(1);
					}
					result[i] = value;

				}
			}
			return result;
		}
		return object;
	};

	return new Json();
});