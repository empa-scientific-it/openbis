define([ "require", "stjs" ], function(require, stjs) {
	var FastDownloadSessionOptions = function() {
	};
	stjs.extend(FastDownloadSessionOptions,  null, [], function(constructor, prototype) {
		prototype['@type'] = 'dss.dto.datasetfile.fastdownload.FastDownloadSessionOptions';
		constructor.serialVersionUID = 1;
		prototype.wishedNumberOfStreams = null;
		prototype.withWishedNumberOfStreams = function(wishedNumberOfStreams) {
			if (wishedNumberOfStreams != null && wishedNumberOfStream <= 0) {
				throw new exceptions.IllegalArgumentException("Wished number of streams must be > 0: " + wishedNumberOfStreams);
			}
			this.wishedNumberOfStreams = wishedNumberOfStreams;
			return this;
		};
		prototype.getWishedNumberOfStreams = function() {
			return this.wishedNumberOfStreams;
		};
	}, {});
	return FastDownloadSessionOptions;
})
