define([ "stjs" ], function(stjs) {
  var ContentCopyCreation = function() {
  };
  stjs.extend(ContentCopyCreation, null, [], function(constructor, prototype) {
    prototype['@type'] = 'as.dto.dataset.create.ContentCopyCreation';
    constructor.serialVersionUID = 1;
    prototype.externalId = null;
    prototype.path = null;
    prototype.gitCommitHash = null;
    prototype.gitRepositoryId = null;
    prototype.externalDmsId = null;
    
    prototype.getExternalId = function() {
      return this.externalId;
    };
    prototype.setExternalId = function(externalId) {
      this.externalId = externalId;
    };
    prototype.getPath = function() {
      return this.path;
    };
    prototype.setPath = function(path) {
      this.path = path;
    };
    prototype.getGitCommitHash = function() {
      return this.gitCommitHash;
    };
    prototype.setGitCommitHash = function(gitCommitHash) {
      this.gitCommitHash = gitCommitHash;
    };
    prototype.getGitRepositoryId = function() {
      return this.gitRepositoryId;
    };
    prototype.setGitRepositoryId = function(gitRepositoryId) {
      this.gitRepositoryId = gitRepositoryId;
    };
    prototype.getExternalDmsId = function() {
      return this.externalDmsId;
    };
    prototype.setExternalDmsId = function(externalDmsId) {
      this.externalDmsId = externalDmsId;
    };
  }, {
    externalDmsId : "IExternalDmsId"
  });
  return ContentCopyCreation;
})    
