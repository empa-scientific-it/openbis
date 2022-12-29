from ch.systemsx.cisd.common.exceptions import UserFailureException

def process(tr, parameters, tableBuilder):
    assertAuthorization(tr)

def assertAuthorization(tr):
    authService = tr.getAuthorizationService()
    roleAssignements = authService.listRoleAssignments()
    for ra in roleAssignements:
        user = ra.getUser().getUserId()
        role = ra.getRoleSetCode()
        if user == userId and str(role).endswith("ADMIN"):
            return
    raise UserFailureException("User isn't authorized for using the Dropbox Monitor.")

