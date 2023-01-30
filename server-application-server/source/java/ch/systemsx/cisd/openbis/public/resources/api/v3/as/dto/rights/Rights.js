define(["stjs"], function(stjs) {
	var Rights = function() {
	};
	stjs.extend(Rights, null, [], function(constructor, prototype) {
		prototype["@type"] = 'as.dto.rights.Rights';
		constructor.serialVersionUID = 1;
		prototype.rights = null;
		
		prototype.getRights = function() {
			return this.rights;
		};
		prototype.setRights = function(rights) {
			this.rights = rights;
		};
	}, {
		rights : {
			name : "Set",
			arguments : [ "Right"]
		}
	});
	return Rights;
})