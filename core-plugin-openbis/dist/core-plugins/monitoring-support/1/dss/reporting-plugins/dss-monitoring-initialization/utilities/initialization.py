#!/usr/bin/python
import sys
import getpass
import ssl
import json

if sys.version_info.major < 3:
    import urllib2
else:
    import urllib.request

def ask_server(url, request_data):
    if sys.version_info.major < 3:
        if sys.version_info.minor >= 2 and sys.version_info.micro > 9:
            return urllib2.urlopen(url, request_data, context=ssl._create_unverified_context()).read()
        return urllib2.urlopen(url, request_data).read()
    return urllib.request.urlopen(url, request_data.encoding('utf8'), context=ssl._create_unverified_context()).read()

if len(sys.argv) < 3:
    raise Exception("Usage: python init_dss_monitoring.py <openBIS URL> <admin user id> [<admin password>]")
url = sys.argv[1]
admin_user = sys.argv[2]
if len(sys.argv) == 3:
    password = getpass.getpass()
else:
    password = sys.argv[3]

data = '{"method":"tryToAuthenticateAtQueryServer","params":["%s","%s"],"id":"1","jsonrpc":"2:0"}' % (admin_user, password)
answer = ask_server("%s/openbis/openbis/rmi-query-v1.json" % url, data)
session_token = json.loads(answer)['result']

space_code = 'NAGIOS'
sample_code = 'NAGIOS'
sample_type_code = 'NAGIOS'
data_set_code = '99999999999999999-0000000'
data_set_type_code = 'NAGIOS'
file_name = 'nagios.txt'
file_content = 'Nagios'
user_id='nrpe'
user_password='<choose a strong password>'

data = ('{"method":"createReportFromAggregationService","params":["%s","DSS1","dss-monitoring-initialization",'
        + '{"sessionToken":"%s","data set code":"%s","space code":"%s","sample code":"%s","data set type code":"%s",'
        + '"sample type code":"%s","file name":"%s","file content":"%s",' 
        + '"user id":"%s","password":"%s"}],"id":"1","jsonrpc":"2.0"}' 
       ) % (session_token, session_token, data_set_code, space_code, sample_code, data_set_type_code, 
            sample_type_code, file_name, file_content, user_id, user_password)
answer = ask_server("%s/openbis/openbis/rmi-query-v1.json" % url, data)
print(answer)

