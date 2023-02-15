define([ "stjs", "as/dto/common/fetchoptions/CacheMode" ], function(stjs, CacheMode) {
	var FetchOptions = function() {
		this._count = null;
		this._from = null;
		this._cacheMode = CacheMode.NO_CACHE;
	};
	stjs.extend(FetchOptions, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.fetchoptions.FetchOptions';
		constructor.serialVersionUID = 1;
		prototype.count = function(count) {
			this._count = count;
			return this;
		};
		prototype.getCount = function() {
			return this._count;
		};
		prototype.from = function(from) {
			this._from = from;
			return this;
		};
		prototype.getFrom = function() {
			return this._from;
		};
		prototype.cacheMode = function(cacheMode) {
			this._cacheMode = cacheMode;
			return this;
		};
		prototype.getCacheMode = function() {
			return this._cacheMode;
		}
	}, {});
	return FetchOptions;
})