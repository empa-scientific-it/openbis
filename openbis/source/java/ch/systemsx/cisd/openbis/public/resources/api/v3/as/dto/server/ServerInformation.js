define([ "stjs" ], function(stjs) {
	var ServerInformation = function() {
	};
	stjs.extend(ServerInformation, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.server.ServerInformation';
		constructor.serialVersionUID = 1;
		prototype.PERSONAL_ACCESS_TOKENS_MAX_VALIDITY_PERIOD = "personal-access-tokens-max-validity-period";
		prototype.PERSONAL_ACCESS_TOKENS_VALIDITY_WARNING_PERIOD = "personal-access-tokens-validity-warning-period";
	}, {
        PERSONAL_ACCESS_TOKENS_MAX_VALIDITY_PERIOD : "String",
        PERSONAL_ACCESS_TOKENS_VALIDITY_WARNING_PERIOD : "String",
    });
    return ServerInformation;
})