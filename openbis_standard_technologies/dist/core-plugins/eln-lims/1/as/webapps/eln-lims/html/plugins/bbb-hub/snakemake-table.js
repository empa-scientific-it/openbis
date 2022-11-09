var SnakemakeTable = new function() {

    this.paintTable = function(container, experiments, roles) {
        var columns = [];

        columns.push({
            label : 'Code',
            property : 'code',
            isExportable: false,
            sortable : true
        });

        columns.push({
            label : 'Description',
            property : 'description',
            isExportable: false,
            sortable : true
        });

        columns.push({
            label : 'Group',
            property : 'group',
            isExportable: false,
            sortable : true
        });

        columns.push({
            label : 'E-mail',
            property : 'email',
            isExportable: false,
            sortable : true
        });

        columns.push({
            label : 'Special access',
            property : 'specialAccess',
            isExportable: false,
            sortable : true
        });

        var getDataList = function(callback) {
            var entities = [];

            var rolesMap = SnakemakeTable.rolesMap(roles);

            var dataList = [];

            for (i = 0; i < experiments.length; i++) {
                if (experiments[i].type === undefined || experiments[i].type.code !== "BBB") {
                    continue;
                }

                var desc = experiments[i].properties["BBB.DESCRIPTION"];
                var group = experiments[i].properties["BBB.GROUP"];
                var registrator = experiments[i].registrator;
                var projectCode = experiments[i].project.code;
                var spaceCode = experiments[i].project.space.code;

                var model = {
                                    'id' : experiments[i].code,
                                    'code' : experiments[i].code,
                                    'description' : desc === undefined ? "" : desc,
                                    'group' : group === undefined ? "" : group,
                                    'email' : registrator === null ? "" : registrator.email,
                                    'specialAccess' : SnakemakeTable.getSpecialAccess(rolesMap, projectCode, spaceCode)
                };

                dataList.push(model);
            }
            callback(dataList);
        };

        var dataGridController = new DataGridController(null, columns, [], null, getDataList, null, true, "ENTITY_TABLE_BBB", null, {
            fileFormat: 'TSV',
            filePrefix: 'entities'
        }, 90);
        dataGridController.init(container);
    }

    this.prepareData = function(experiments, roles) {
        var data = [];

        var rolesMap = SnakemakeTable.rolesMap(roles);

        for (i = 0; i < experiments.length; i++) {
            var row = [];

            if (experiments[i].type === undefined || experiments[i].type.code !== "BBB") {
                continue;
            }

            var desc = experiments[i].properties["BBB.DESCRIPTION"];
            var group = experiments[i].properties["BBB.GROUP"];
            var registrator = experiments[i].registrator;
            var projectCode = experiments[i].project.code;
            var spaceCode = experiments[i].project.space.code;

            row.push(experiments[i].code);
            row.push(desc === undefined ? "" : desc);
            row.push(group === undefined ? "" : group);
            row.push(registrator === null ? "" : registrator.email);
            row.push(SnakemakeTable.getSpecialAccess(rolesMap, projectCode, spaceCode));

            data.push(row);
        }

        return data;
    }

    this.getSpecialAccess = function(rolesMap, projectCode, spaceCode) {
        var projectUsers = rolesMap["projects"][projectCode] === undefined ? "" : rolesMap["projects"][projectCode]["users"].join(", ");
        var projectGroups = rolesMap["projects"][projectCode] === undefined ? "" : rolesMap["projects"][projectCode]["groups"].join(", ");

        var spaceUsers = rolesMap["space"][spaceCode] === undefined ? "" : rolesMap["space"][spaceCode]["users"].join(", ");
        var spaceGroups = rolesMap["space"][spaceCode] === undefined ? "" : rolesMap["space"][spaceCode]["groups"].join(", ");

        var users = projectUsers + (projectUsers !== "" && spaceUsers !== "" ? ", " : "") + spaceUsers;
        var groups = projectGroups + (projectGroups !== "" && spaceGroups !== ""  ? ", " : "") + spaceGroups;

        var access = "";
        if (users !== "") {
            access += "<span style='font-weight:bold'>Users: </span>" + users;
        }

        if (groups !== "") {
            if (access !== "") {
                access += "<br/>";
            }
            access += "<span style='font-weight:bold'>Groups: </span>" + groups;
        }

        return access;
    }

    this.rolesMap = function(roles) {
        var rolesMap = new Map();

        var projects = new Map();
        var spaces = new Map();

        rolesMap["projects"] = projects;
        rolesMap["space"] = spaces;

        for (i = 0; i < roles.length; i++) {
            var user = roles[i].user == null ? null : roles[i].user.userId;
            var group = roles[i].authorizationGroup == null ? null : roles[i].authorizationGroup.code;

            if (roles[i].project !== null) {
                var value = projects[roles[i].project.code];
                projects[roles[i].project.code] = SnakemakeTable.appendValue(value, user, group);
            } else if (roles[i].space !== null) {
                var value = spaces[roles[i].space.code];
                spaces[roles[i].space.code] = SnakemakeTable.appendValue(value, user, group);
            }
        }

        return rolesMap;
    }

    this.appendValue = function(value, user, group) {
        if (value === undefined) {
            value = new Map();
            value["users"] = new Array();
            value["groups"] = new Array();
        }

        if (user !== null) {
            value["users"].push(user);
        } else {
            value["groups"].push(group);
        }
        return value;
    }
}