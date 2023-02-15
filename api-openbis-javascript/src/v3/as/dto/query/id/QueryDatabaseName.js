define([ "stjs", "util/Exceptions", "as/dto/query/id/IQueryDatabaseId" ], function(stjs, exceptions, IQueryDatabaseId) {
	/**
	 * @param name
	 *            Database name, e.g. "test-database".
	 */
	var QueryDatabaseName = function(name) {
		this.setName(name);
	};
	stjs.extend(QueryDatabaseName, null, [ IQueryDatabaseId ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.query.id.QueryDatabaseName';
		constructor.serialVersionUID = 1;
		prototype.name = null;
		prototype.getName = function() {
			return this.name;
		};
		prototype.setName = function(name) {
			if (name == null) {
				throw new exceptions.IllegalArgumentException("Name cannot be null");
			}
			this.name = name;
		};
		prototype.toString = function() {
			return this.getName();
		};
		prototype.hashCode = function() {
			return ((this.getName() == null) ? 0 : this.getName().hashCode());
		};
		prototype.equals = function(obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (this.getClass() != obj.getClass()) {
				return false;
			}
			var other = obj;
			return this.getName() == null ? this.getName() == other.getName() : this.getName().equals(other.getName());
		};
	}, {});
	return QueryDatabaseName;
})