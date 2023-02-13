/**
 * @author anttil
 */
define([ "require", "stjs", "as/dto/common/search/AbstractObjectSearchCriteria", "as/dto/dataset/search/ExternalDmsSearchCriteria", "as/dto/dataset/search/ExternalCodeSearchCriteria", "as/dto/dataset/search/PathSearchCriteria", "as/dto/dataset/search/GitCommitHashSearchCriteria",
	"as/dto/dataset/search/GitRepositoryIdSearchCriteria", "as/dto/common/search/AbstractCompositeSearchCriteria" ], function(require, stjs, AbstractObjectSearchCriteria) {
	var ContentCopySearchCriteria = function() {
		AbstractObjectSearchCriteria.call(this);
	};
	stjs.extend(ContentCopySearchCriteria, AbstractObjectSearchCriteria, [ AbstractObjectSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.search.ContentCopySearchCriteria';
		constructor.serialVersionUID = 1;

		prototype.withExternalDms = function() {
			var ExternalDmsSearchCriteria = require("as/dto/dataset/search/ExternalDmsSearchCriteria");
			return this.addCriteria(new ExternalDmsSearchCriteria());
		};

		prototype.withExternalCode = function() {
			var ExternalCodeSearchCriteria = require("as/dto/dataset/search/ExternalCodeSearchCriteria");
			return this.addCriteria(new ExternalCodeSearchCriteria());
		};

		prototype.withPath = function() {
			var PathSearchCriteria = require("as/dto/dataset/search/PathSearchCriteria");
			return this.addCriteria(new PathSearchCriteria());
		};

		prototype.withGitCommitHash = function() {
			var GitCommitHashSearchCriteria = require("as/dto/dataset/search/GitCommitHashSearchCriteria");
			return this.addCriteria(new GitCommitHashSearchCriteria());
		};

		prototype.withGitRepositoryId = function() {
			var GitRepositoryIdSearchCriteria = require("as/dto/dataset/search/GitRepositoryIdSearchCriteria");
			return this.addCriteria(new GitRepositoryIdSearchCriteria());
		};

	}, {
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriteria" ]
		}
	});
	return ContentCopySearchCriteria;
})