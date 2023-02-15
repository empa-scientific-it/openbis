define([ "require", "stjs", "as/dto/common/fetchoptions/FetchOptions", "as/dto/common/fetchoptions/EmptyFetchOptions", "as/dto/operation/fetchoptions/OperationExecutionDetailsSortOptions" ],
		function(require, stjs, FetchOptions) {
			var OperationExecutionDetailsFetchOptions = function() {
			};
			stjs.extend(OperationExecutionDetailsFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
				prototype['@type'] = 'as.dto.operation.fetchoptions.OperationExecutionDetailsFetchOptions';
				constructor.serialVersionUID = 1;
				prototype.operations = null;
				prototype.progress = null;
				prototype.error = null;
				prototype.results = null;
				prototype.sort = null;

				prototype.withOperations = function() {
					if (this.operations == null) {
						var EmptyFetchOptions = require("as/dto/common/fetchoptions/EmptyFetchOptions");
						this.operations = new EmptyFetchOptions();
					}
					return this.operations;
				};
				prototype.withOperationsUsing = function(fetchOptions) {
					return this.operations = fetchOptions;
				};
				prototype.hasOperations = function() {
					return this.operations != null;
				};

				prototype.withProgress = function() {
					if (this.progress == null) {
						var EmptyFetchOptions = require("as/dto/common/fetchoptions/EmptyFetchOptions");
						this.progress = new EmptyFetchOptions();
					}
					return this.progress;
				};
				prototype.withProgressUsing = function(fetchOptions) {
					return this.progress = fetchOptions;
				};
				prototype.hasProgress = function() {
					return this.progress != null;
				};

				prototype.withError = function() {
					if (this.error == null) {
						var EmptyFetchOptions = require("as/dto/common/fetchoptions/EmptyFetchOptions");
						this.error = new EmptyFetchOptions();
					}
					return this.error;
				};
				prototype.withErrorUsing = function(fetchOptions) {
					return this.error = fetchOptions;
				};
				prototype.hasError = function() {
					return this.error != null;
				};

				prototype.withResults = function() {
					if (this.results == null) {
						var EmptyFetchOptions = require("as/dto/common/fetchoptions/EmptyFetchOptions");
						this.results = new EmptyFetchOptions();
					}
					return this.results;
				};
				prototype.withResultsUsing = function(fetchOptions) {
					return this.results = fetchOptions;
				};
				prototype.hasResults = function() {
					return this.results != null;
				};

				prototype.sortBy = function() {
					if (this.sort == null) {
						var OperationExecutionDetailsSortOptions = require("as/dto/operation/fetchoptions/OperationExecutionDetailsSortOptions");
						this.sort = new OperationExecutionDetailsSortOptions();
					}
					return this.sort;
				};
				prototype.getSortBy = function() {
					return this.sort;
				};
			}, {
				operations : "EmptyFetchOptions",
				progress : "EmptyFetchOptions",
				error : "EmptyFetchOptions",
				results : "EmptyFetchOptions",
				sort : "OperationExecutionDetailsSortOptions"
			});
			return OperationExecutionDetailsFetchOptions;
		})