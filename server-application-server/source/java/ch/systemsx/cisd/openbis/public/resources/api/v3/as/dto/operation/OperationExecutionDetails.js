define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var OperationExecutionDetails = function() {
	};
	stjs.extend(OperationExecutionDetails, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.operation.OperationExecutionDetails';
		constructor.serialVersionUID = 1;
		prototype.fetchOptions = null;
		prototype.operations = null;
		prototype.progress = null;
		prototype.error = null;
		prototype.results = null;

		prototype.getFetchOptions = function() {
			return this.fetchOptions;
		};
		prototype.setFetchOptions = function(fetchOptions) {
			this.fetchOptions = fetchOptions;
		};
		prototype.getOperations = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasOperations()) {
				return this.operations;
			} else {
				throw new exceptions.NotFetchedException("Operations have not been fetched.");
			}
		};
		prototype.setOperations = function(operations) {
			this.operations = operations;
		};
		prototype.getProgress = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasProgress()) {
				return this.progress;
			} else {
				throw new exceptions.NotFetchedException("Progress has not been fetched.");
			}
		};
		prototype.setProgress = function(progress) {
			this.progress = progress;
		};
		prototype.getError = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasError()) {
				return this.error;
			} else {
				throw new exceptions.NotFetchedException("Error has not been fetched.");
			}
		};
		prototype.setError = function(error) {
			this.error = error;
		};
		prototype.getResults = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasResults()) {
				return this.results;
			} else {
				throw new exceptions.NotFetchedException("Results have not been fetched.");
			}
		};
		prototype.setResults = function(results) {
			this.results = results;
		};
		prototype.toString = function() {
			return "OperationExecutionDetails";
		};
	}, {
		fetchOptions : "OperationExecutionDetailsFetchOptions",
		operations : {
			name : "List",
			arguments : [ "IOperation" ]
		},
		progress : "IOperationExecutionProgress",
		error : "IOperationExecutionError",
		results : {
			name : "List",
			arguments : [ "IOperationResult" ]
		}
	});
	return OperationExecutionDetails;
})