from datetime import datetime


def isInList(name, list_):
    return any(name in item for item in list_)


def returnLatestResultDate(name, list_):
    files = returnNLatestResults(name, list_, 1)
    trimmedDate = []
    trimmedDate.append(files[0][0:19])
    date = (max(trimmedDate)).replace(
        "-", "/", 2).replace("_", " ").replace("-", ":")
    return datetime.strptime(date, "%Y/%m/%d %H:%M:%S")


def returnNLatestResults(name, list_, n):
    filteredDropboxList = [tmp for tmp in list_ if name in tmp]
    filesList = []
    for item in filteredDropboxList:
        filesList.append(item)
    filesList.sort(reverse=True)
    return filesList[:n]


def returnNLatestResultsAll(name, process, success, failure, n):
    return (returnNLatestResults(name, process, n),
            returnNLatestResults(name, success, n),
            returnNLatestResults(name, failure, n))
