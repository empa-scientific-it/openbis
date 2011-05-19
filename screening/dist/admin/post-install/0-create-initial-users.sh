#
# Creates an openBIS user by executing the 'passwd.sh' command-line tool.
# These users will only be available when 'file-authentication-service' is configured in openBIS.
 
# $1 - username
# $2 - password. If not specified the function will ask for password input form stdin.
createUser()
{
  username=$1
  password=$2

	if [ -z "$password" ]; then
	    read -s -p "Enter password for '$username' : " password
	fi

  
  pushd
  cd $BASE/../../servers/openBIS-server/jetty
  
  echo "Creating user $username ..."
  ./bin/passwd.sh add -p "$password" $username
  
  popd  
}

BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

createUser "admin" "$ADMIN_PASSWORD"
createUser "etlserver" "$ETLSERVER_PASSWORD"

