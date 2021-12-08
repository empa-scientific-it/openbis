var BBBServerFacade = new function() {
    this.getExperiment = function($container, model) {
        require(["openbis",
                 "as/dto/sample/fetchoptions/SampleFetchOptions",
                 "as/dto/experiment/fetchoptions/ExperimentFetchOptions",
                 "as/dto/experiment/id/ExperimentIdentifier",
                 "as/dto/dataset/fetchoptions/DataSetFetchOptions"],
        function(openbis, SampleFetchOptions,
                          ExperimentFetchOptions,
                          ExperimentIdentifier,
                          DataSetFetchOptions) {

            var v3 = new openbis(null);
            v3._private.sessionToken = mainController.openbisV1._internal.sessionToken;

            var webAppContext = v3.getWebAppContext();

            var dataSetFetchOptions = new DataSetFetchOptions();
            dataSetFetchOptions.withType();
            dataSetFetchOptions.withPhysicalData();

            var experimentId = new ExperimentIdentifier(model.experiment.identifier);
            var fetchOptions = new ExperimentFetchOptions();
            fetchOptions.withDataSetsUsing(dataSetFetchOptions);
            fetchOptions.withType();

            var sampleFetchOptions = new SampleFetchOptions();
            sampleFetchOptions.withType().withPropertyAssignments().withPropertyType();
            sampleFetchOptions.withProperties();
            fetchOptions.withSamplesUsing(sampleFetchOptions);
            v3.getExperiments([experimentId], fetchOptions).done(function(result) {
                var experiment = result[model.experiment.identifier];
                if (experiment.type.code == "BBB") {
                    SnakemakeTrigger.paintTriggerContainers($container, model, experiment);
                }
            });
        });
    }

    this.getExperiments = function($content) {
        require(["openbis", "as/dto/sample/fetchoptions/SampleFetchOptions",
                            "as/dto/experiment/fetchoptions/ExperimentFetchOptions",
                            "as/dto/experiment/id/ExperimentIdentifier",
                            "as/dto/dataset/fetchoptions/DataSetFetchOptions",
                            "as/dto/experiment/search/ExperimentSearchCriteria",
                            "as/dto/project/fetchoptions/ProjectFetchOptions"],
        function(openbis, SampleFetchOptions,
                          ExperimentFetchOptions,
                          ExperimentIdentifier,
                          DataSetFetchOptions,
                          ExperimentSearchCriteria,
                          ProjectFetchOptions) {

            var v3 = new openbis();

            v3.loginAsAnonymousUser().done(function(sessionToken) {
                v3._private.sessionToken = sessionToken;
                var webAppContext = v3.getWebAppContext();

                var fetchOptions = new ExperimentFetchOptions();
                fetchOptions.withType();
                fetchOptions.withProperties();
                fetchOptions.withRegistrator();

                var projectFetchOptions = new ProjectFetchOptions();
                projectFetchOptions.withSpace();

                fetchOptions.withProjectUsing(projectFetchOptions);

                var searchCriteria = new ExperimentSearchCriteria();
                searchCriteria.withType();

                v3.searchExperiments(searchCriteria, fetchOptions).done(function(result) {
                    BBBServerFacade.getRoleAssignment($content, result.objects);
                });
            }).fail(function(result) {
                console.log("Call failed to server: " + JSON.stringify(result));
            });
        });
    }


    this.userIsAdmin = function(action) {
        require(["openbis", "as/dto/roleassignment/fetchoptions/RoleAssignmentFetchOptions",
                            "as/dto/roleassignment/search/RoleAssignmentSearchCriteria"],
        function(openbis, RoleAssignmentFetchOptions, RoleAssignmentSearchCriteria) {

            var v3 = new openbis(null);
            v3._private.sessionToken = mainController.openbisV1._internal.sessionToken;
            var sessionToken = v3._private.sessionToken;

            var userId = mainController.serverFacade.getUserId();
            var webAppContext = v3.getWebAppContext();

            var criteria = new RoleAssignmentSearchCriteria();
            criteria.withUser().withUserId().thatEquals(userId);

            var fetchOptions = new RoleAssignmentFetchOptions();
            fetchOptions.withUser();
            fetchOptions.withAuthorizationGroup();

            v3.searchRoleAssignments(criteria, fetchOptions).done(function(result) {
                var roles = result.objects;
                var isAdmin = false;

                for (i = 0; i < roles.length; i++) {
                    var role = roles[i].role;
                    var roleLevel = roles[i].roleLevel;

                    if (role === "ADMIN" && roleLevel === "INSTANCE") {
                        isAdmin = true;
                    }
                }

                action(isAdmin);
            });
        });
    }

    this.getRoleAssignment = function ($content, experiments) {
        require(["openbis", "as/dto/roleassignment/fetchoptions/RoleAssignmentFetchOptions",
                            "as/dto/roleassignment/search/RoleAssignmentSearchCriteria"],
        function(openbis, RoleAssignmentFetchOptions, RoleAssignmentSearchCriteria) {

            var v3 = new openbis();

            v3.loginAsAnonymousUser().done(function(sessionToken) {
                v3._private.sessionToken = sessionToken;
                var webAppContext = v3.getWebAppContext();

                var criteria = new RoleAssignmentSearchCriteria();
                criteria.withOrOperator();
                criteria.withProject();
                criteria.withSpace();

                var fetchOptions = new RoleAssignmentFetchOptions();
                fetchOptions.withSpace();
                fetchOptions.withProject();
                fetchOptions.withUser();
                fetchOptions.withAuthorizationGroup();

                v3.searchRoleAssignments(criteria, fetchOptions).done(function(result) {
                    SnakemakeTable.paintTable($content, experiments, result.objects);
                });
            }).fail(function(result) {
                console.log("Call failed to server: " + JSON.stringify(result));
            });
        });
    }

    this.callCustomASService = function(params, $model, action) {
        var result_div = $('#callback_message');

        require(["openbis",
                 "as/dto/service/id/CustomASServiceCode",
                 "as/dto/service/CustomASServiceExecutionOptions",
                 "as/dto/experiment/id/ExperimentIdentifier"],
        function(openbis, CustomASServiceCode, CustomASServiceExecutionOptions, ExperimentIdentifier) {

            var v3 = new openbis(null);
            v3._private.sessionToken = mainController.openbisV1._internal.sessionToken;
            var webAppContext = mainController.openbisV3.getWebAppContext();
            webAppContext['entityIdentifier'] = $model.experiment.identifier;

            var serviceCode = new CustomASServiceCode("snakemake_service");
            var options = new CustomASServiceExecutionOptions();

            options.withParameter("webapp_context", webAppContext)
            Object.keys(params).forEach(function(key) {
                options.withParameter(key, params[key]);
            });

            v3.executeCustomASService(serviceCode, options).done(function(result) {
                result_json = JSON.parse(result);
                action(result_json)
            });
        });
        return false;
    }
}