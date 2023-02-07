from ch.systemsx.cisd.openbis.generic.server import CommonServiceProvider


def process(context, parameters):
    method = parameters.get("method");
    result = None;

    if method == "sendCountActiveUsersEmail":
        result = sendCountActiveUsersEmail(context, parameters);

    return result


def sendCountActiveUsersEmail(context, parameters):
    # use CustomASServiceCode
    try:
        sessionToken = context.getSessionToken()
        server = CommonServiceProvider.getCommonServer()
        server.sendCountActiveUsersEmail(sessionToken)
    except Exception as e:
        return {"status": "error", "message": str(e)}
    return {"status": "OK"}
