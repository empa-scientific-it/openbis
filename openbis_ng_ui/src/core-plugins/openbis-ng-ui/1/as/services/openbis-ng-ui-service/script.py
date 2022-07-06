def process(context, parameters):
    method = parameters.get("method");
    result = None;

    if method == "sendCountActiveUsersEmail":
        result = sendCountActiveUsersEmail(context, parameters);

    return result


def sendCountActiveUsersEmail(context, parameters):
    # use CustomASServiceCode
    return True
