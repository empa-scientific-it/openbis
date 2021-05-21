define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var Event = function() {
	};
	stjs.extend(Event, null, [], function(constructor, prototype) {
        prototype['@type'] = 'as.dto.event.Event';
        constructor.serialVersionUID = 1;
        prototype.fetchOptions = null;
        prototype.id = null;
        prototype.eventType = null;
        prototype.entityType = null;
        prototype.entitySpace = null;
        prototype.entitySpaceId = null;
        prototype.entityProject = null;
        prototype.entityProjectId = null;
        prototype.entityRegistrator = null;
        prototype.entityRegistrationDate = null;
        prototype.identifier = null;
        prototype.description = null;
        prototype.reason = null;
        prototype.content = null;
        prototype.registrator = null;
        prototype.registrationDate = null;

		prototype.getFetchOptions = function() {
			return this.fetchOptions;
		};
		prototype.setFetchOptions = function(fetchOptions) {
			this.fetchOptions = fetchOptions;
		};
		prototype.getId = function() {
			return this.id;
		};
		prototype.setId = function(id) {
			this.id = id;
		};
		prototype.getEventType = function() {
			return this.eventType;
		};
		prototype.setEventType = function(eventType) {
			this.eventType = eventType;
		};
		prototype.getEntityType = function() {
			return this.entityType;
		};
		prototype.setEntityType = function(entityType) {
			this.entityType = entityType;
		};
		prototype.getEntitySpace = function() {
			return this.entitySpace;
		};
		prototype.setEntitySpace = function(entitySpace) {
			this.entitySpace = entitySpace;
		};
		prototype.getEntitySpaceId = function() {
			return this.entitySpaceId;
		};
		prototype.setEntitySpaceId = function(entitySpaceId) {
			this.entitySpaceId = entitySpaceId;
		};
		prototype.getEntityProject = function() {
			return this.entityProject;
		};
		prototype.setEntityProject = function(entityProject) {
			this.entityProject = entityProject;
		};
		prototype.getEntityProjectId = function() {
			return this.entityProjectId;
		};
		prototype.setEntityProjectId = function(entityProjectId) {
			this.entityProjectId = entityProjectId;
		};
		prototype.getEntityRegistrator = function() {
			return this.entityRegistrator;
		};
		prototype.setEntityRegistrator = function(entityRegistrator) {
			this.entityRegistrator = entityRegistrator;
		};
		prototype.getEntityRegistrationDate = function() {
			return this.entityRegistrationDate;
		};
		prototype.setEntityRegistrationDate = function(entityRegistrationDate) {
			this.entityRegistrationDate = entityRegistrationDate;
		};
		prototype.getIdentifier = function() {
			return this.identifier;
		};
		prototype.setIdentifier = function(identifier) {
			this.identifier = identifier;
		};
		prototype.getDescription = function() {
			return this.description;
		};
		prototype.setDescription = function(description) {
			this.description = description;
		};
		prototype.getReason = function() {
			return this.reason;
		};
		prototype.setReason = function(reason) {
			this.reason = reason;
		};
		prototype.getContent = function() {
			return this.content;
		};
		prototype.setContent = function(content) {
			this.content = content;
		};
		prototype.getRegistrator = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasRegistrator()) {
				return this.registrator;
			} else {
				throw new exceptions.NotFetchedException("Registrator has not been fetched.");
			}
		};
		prototype.setRegistrator = function(registrator) {
			this.registrator = registrator;
		};
        prototype.getRegistrationDate = function() {
            return this.registrationDate;
        };
        prototype.setRegistrationDate = function(registrationDate) {
            this.registrationDate = registrationDate;
        };
	}, {
		fetchOptions : "EventFetchOptions",
		id : "IEventId",
		eventType : "EventType",
		entityType : "EntityType",
		entitySpaceId : "ISpaceId",
		entityProjectId : "IProjectId",
		entityRegistrator : "Person",
		entityRegistrationDate : "Date",
		registrator : "Person",
		registrationDate : "Date"
	});
	return Event;
})