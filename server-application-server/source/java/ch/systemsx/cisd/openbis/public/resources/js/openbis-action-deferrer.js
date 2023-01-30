/**
 * A utility class for deferring an action until all of some kind of action has completed
 *
 * @argument dependencies An array of the keys for the dependencies.
 */

function openbisActionDeferrer(pendingAction, dependencies) {
	this.pendingAction = pendingAction;
	this.dependencies = {};
	var newme = this;
	dependencies.forEach(function(key) {
		newme.dependencies[key] = false;
	});
}

/**
 * Note that a dependency completed. Execute the pending action if appropriate.
 */
openbisActionDeferrer.prototype.dependencyCompleted = function(key) {
	this.dependencies[key] = true;
	var shouldExecute = true;
	for (prop in this.dependencies) {
		if (false == this.dependencies[prop]) {
			shouldExecute = false;
			break;
		}
	}
	if (shouldExecute) {
		this.pendingAction();
	}
}