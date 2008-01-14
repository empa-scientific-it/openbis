#!/bin/sh
# author: Tomasz Pylak, 2007-09-27
# Implementation assumptions:
# - the current directory after calling a function does not change

# ----------------------------- configuration
TIME_TO_COMPLETE=60 # time (in seconds) needed by the whole pipeline to process everything
SVN_PATHS="/opt/local/bin /usr/bin"
LSOF_PATHS="/usr/sbin"

# all paths are relative to the template directory
TEMPLATE=templates
TARGETS=targets
WORK=$TARGETS/playground
INSTALL=$TARGETS/install
LOCAL_PROJECTS=..

LIMS_SERVER_NAME=openBIS-server
LIMS_SERVER=$WORK/$LIMS_SERVER_NAME
LIMS_CLIENT_NAME=openBIS-client
LIMS_CLIENT=$WORK/$LIMS_CLIENT_NAME

DATA=$WORK/data
ERR_LOG=$WORK/all_err_log.txt

# ---- global state
TEST_FAILED=false # working variable, if true then some tests failed

# --------------------------- build distributions from sources

function get_env_path {
    echo $PATH | tr ":" " "
}

# looks for a specified file in environment paths and paths given as a parameter (space separated)
function locate_file {
    local file=$1
    shift
    local additional_paths=$@
    for dir in `get_env_path` $additional_paths; do 
	local full_path=$dir/$file
	if [ -f $full_path ]; then
    	    echo $full_path;
	    return
	fi 
    done
}

function run_svn {
    `locate_file svn $SVN_PATHS` $@
}

function build_zips {
    build_etl=$1
    build_dmv=$2
    build_lims=$3
    use_local_source=$4

    if [ $build_etl == "true" -o $build_dmv == "true" -o $build_lims == "true" ]; then
        mkdir -p $INSTALL
	if [ "$use_local_source" = "true" ]; then
    	    build_zips_from_local $build_etl $build_dmv $build_lims
        else
	    build_zips_from_svn $build_etl $build_dmv $build_lims
	fi
    else
	echo "No components to build were specified (--help explains how to do this)."
	echo "Build process skipped."
    fi
    assert_file_exists_or_die "$INSTALL/openBIS-server*.zip"
    assert_file_exists_or_die "$INSTALL/openBIS-client*.zip"
    assert_file_exists_or_die "$INSTALL/etlserver*.zip"
    assert_file_exists_or_die "$INSTALL/datamover*.zip"

}

function build_zips_from_local {
    build_etl=$1
    build_dmv=$2
    build_lims=$3

    build_components build_local $build_etl $build_dmv $build_lims
}

function build_local {
    local PROJECT_NAME=$1
    $LOCAL_PROJECTS/$PROJECT_NAME/build/antrun.sh
    mv $LOCAL_PROJECTS/$PROJECT_NAME/targets/dist/*.zip $INSTALL
}

function build_components {
    build_cmd=$1
    build_etl=$2
    build_dmv=$3
    build_lims=$4

    if [ $build_etl == "true" ]; then
	rm -f $INSTALL/etlserver*.zip
        $build_cmd etlserver
    fi
    if [ $build_dmv == "true" ]; then
	rm -f $INSTALL/datamover*.zip
	$build_cmd datamover
    fi
    if [ $build_lims == "true" ]; then
	rm -f $INSTALL/openBIS-server*.zip
	rm -f $INSTALL/openBIS-client*.zip
        $build_cmd lims_webclient
    fi
}

function build_remote {
    local RSC=$1
    local PROJECT_NAME=$2
    
    cd $RSC
    ./build.sh $PROJECT_NAME
    cd ..
}

function build_zips_from_svn {
    build_etl=$1
    build_dmv=$2
    build_lims=$3

    RSC=build_resources
    rm -fr $RSC
    run_svn checkout svn+ssh://source.systemsx.ch/repos/cisd/build_resources/trunk $RSC
    build_components "build_remote $RSC" $build_etl $build_dmv $build_lims
    mv $RSC/*.zip $INSTALL
    rm -fr $RSC 
}

# -------------------------- installation

function clean_svn {
    local DIR=$1
    for file in `find $DIR -name ".svn"`; do 
	rm -fr $file; 
    done
}

function copy_templates {
    local template_dir=$1
    cp -fR $TEMPLATE/$template_dir $WORK
    clean_svn $WORK/$template_dir
}

function prepare {
    src=$1
    dest=$2
    rm -fr $WORK/$dest
    cp -R $WORK/$src $WORK/$dest
    copy_templates $dest
}

function unpack { # from ZIPS to BUILD
    local file_pattern=$1
    unzip -d $WORK $INSTALL/$file_pattern*
}

function remove_unpacked {
    rm -fR $WORK/$1
}

function run_lsof {
    `locate_file lsof $LSOF_PATHS` $@
}

function check_server_port {
    run_lsof -i -n -P | grep 8443
}

function wait_for_server {
    echo -n "Server starting"
    i=0; 
    while [ "`check_server_port`" == "" -a $i -lt 5 ]; do 
	sleep 2; 
	echo -n "."; 
	let i=$i+1; 
    done
    if [ "`check_server_port`" == "" ]; then
	report_error "Server could not be started!"
	exit 1
    else
	echo "...[Done]"
    fi
}

function install_lims_server {
    local install_lims=$1

    if [ $install_lims == "true" ]; then
        rm -fr $LIMS_SERVER
	copy_templates $LIMS_SERVER_NAME
    
        unzip -d $LIMS_SERVER $INSTALL/openBIS-server*.zip
	$LIMS_SERVER/openBIS-server/install.sh $PWD/$LIMS_SERVER $LIMS_SERVER/service.properties $LIMS_SERVER/roles.conf
	wait_for_server
    else
        copy_templates $LIMS_SERVER_NAME
        restart_lims
    fi
}


function startup_lims_server {
    call_in_dir bin/startup.sh $LIMS_SERVER/apache-tomcat
    wait_for_server
}

function shutdown_lims_server {
    if [ "`check_server_port`" != "" ]; then
        $LIMS_SERVER/apache-tomcat/bin/shutdown.sh
    fi
}

function register_cell_plates {
    assert_dir_exists_or_die $LIMS_CLIENT
    call_in_dir load-lims-data.sh $LIMS_CLIENT
}

function install_lims_client {
    local install_lims=$1

    if [ $install_lims == "true" ]; then
        rm -fr $WORK/$LIMS_CLIENT_NAME
	unpack openBIS-client
    fi
    cp -fR $TEMPLATE/$LIMS_CLIENT_NAME $WORK
}

# unpack everything, override default configuration with test configuation	
function install_etls {
    local install_etl=$1
    if [ $install_etl == "true" ]; then
        unpack etlserver
	prepare etlserver etlserver-all
	remove_unpacked etlserver
    else
	copy_templates etlserver-all    
    fi
}

function install_datamovers {
    local install_dmv=$1
    if [ $install_dmv == "true" ]; then
        unpack datamover
	prepare datamover datamover-raw
        prepare datamover datamover-analys
	remove_unpacked datamover
	cp -fR $TEMPLATE/dummy-img-analyser $WORK
    else 
	copy_templates datamover-raw
	copy_templates datamover-analys
    fi
}

function restart_lims {
    assert_dir_exists_or_die $LIMS_SERVER
    shutdown_lims_server
    sleep 1
    startup_lims_server
    sleep 4
}

function install {
    local install_etl=$1
    local install_dmv=$2
    local install_lims=$3

    mkdir -p $WORK
    
    install_etls $install_etl
    install_datamovers $install_dmv
    install_lims_client $install_lims
    install_lims_server	$install_lims

    register_cell_plates
}


# ----------------------------- general

# calls $cmd script, changing directory to $dir
function call_in_dir {
    cmd=$1
    dir=$2
    
    prev=$PWD
    cd $dir
    sh $cmd
    cd $prev
}

function is_empty_dir {
    dir=$1
    if [ "`ls $dir`" = "" ]; then
	return 1;
    else
	return 0;
    fi
}

# ----------------------------- assertions

function init_log {
    rm -fr $ERR_LOG
}

function report_error {
    local msg=$@

    echo [ERROR] $msg | tee -a $ERR_LOG    
    TEST_FAILED="true"
}

function exit_if_assertion_failed {
    if [ "$TEST_FAILED" = "true" ]; then
	report_error Test failed.
	exit 1;
    else
	echo [OK] Test was successful!
    fi
}

function assert_dir_exists {
    local DIR=$1
    if [ ! -d "$DIR" ]; then
	report_error $DIR does not exist!  
    else
	echo [OK] $DIR exists
    fi
}

function fatal_error {
    local MSG=$@
    report_error $MSG
    exit_if_assertion_failed
}

# remember to pass the parameter in quote marks
function assert_file_exists_or_die {
    local F="$1"
    local files_num=`ls -1 $F 2> /dev/null | wc -l`
    if [ $files_num -gt 1 ]; then
	fatal_error "One file expected for pattern $F, but more found: " $F
    else 
	if [ ! -f $F ]; then
	    fatal_error "No file matching pattern $F exists"
	fi
    fi
}

function assert_dir_exists_or_die {
    local DIR=$1
    if [ ! -d $DIR ]; then
	fatal_error "Directory $DIR does not exist!"
    fi
}

function assert_dir_empty {
    dir=$1
    is_empty_dir $dir
    empty=$?
    if [ $empty == 0 ]; then
	report_error Directory \'$dir\' should be empty!
    fi
}


function assert_pattern_present {
  local file=$1
  local occurences=$2
  local pattern=$3

  echo Matched lines: 
  cat $file | grep "$pattern"  
  local lines=`cat $file | grep "$pattern" | wc -l`
  if [ $lines != $occurences ]; then
	report_error $lines instead of $occurences occurences of pattern $pattern found!
  else
	echo [OK] $occurences occurences of pattern $pattern found
  fi 
}

# ----------------------- Test data

function create_test_data_file {
    local FILE_PATH=$1
    local file_size=2000000
    openssl rand -base64 $file_size -out $FILE_PATH
}

function create_test_data_dir {
    local NAME=$1
    local DIR=$2
    mkdir $DIR/$NAME
    local i=0  
    while [  $i -lt 15 ]; do
	create_test_data_file $DIR/$NAME/$NAME-data$i.txt
	let i=i+1 
    done
}

function generate_test_data {
    echo Generate incoming data
    local DIR=$DATA/in-raw
    create_test_data_dir "3VCP1" $DIR
    create_test_data_dir "3VCP3" $DIR
    create_test_data_dir "3VCP4" $DIR
}

# ----------------------- Launching 

function chmod_exec {
    for file in $@; do
        if [ -f $file ]; then
	    chmod u+x $file
	fi
    done 
}


function switch_sth {
    switch_on=$1 # on/off
    dir=$WORK/$2
    cmd_start=$3
    cmd_stop=$4

    assert_dir_exists_or_die $dir
    chmod_exec $dir/$cmd_start
    chmod_exec $dir/$cmd_stop

    if [ "$switch_on" == "on" ]; then
	echo "Launching $dir..."
	rm -fr $dir/log/*
	call_in_dir "$cmd_start" $dir
    else
	echo "Stopping $dir, displaying errors from the log"
	if [ "`cat $dir/log/* | grep ERROR | tee -a $ERR_LOG`" != "" ]; then
	    report_error $dir reported errors.
	    cat $dir/log/* | grep ERROR	    
	fi
	call_in_dir "$cmd_stop" $dir
    fi
}


function switch_etl {
    switch_sth $1 $2 etlserver.sh shutdown.sh
}

function switch_dmv {
    switch_sth $1 $2 "datamover.sh start" "datamover.sh stop"
}

function switch_processing_pipeline {
    new_state=$1
    switch_etl $new_state etlserver-all
    switch_dmv $new_state datamover-analys
    switch_sth $new_state dummy-img-analyser start.sh stop.sh
    switch_dmv $new_state datamover-raw
}


function launch_tests {
    # prepare empty incoming data
    rm -fr $DATA
    cp -R $TEMPLATE/data $WORK
    clean_svn $DATA

    switch_processing_pipeline "on"
    sleep 4

    generate_test_data
    sleep $TIME_TO_COMPLETE

    switch_processing_pipeline "off"
}

function assert_correct_results {
    local res=$WORK/client-result.txt
    call_in_dir check-results.sh $LIMS_CLIENT/ > $res
    assert_pattern_present $res 3 ".*NEMO.*EXP1.*IMAGE_ANALYSIS_DATA.*3VCP[[:digit:]].*microX.*3VCP[[:digit:]]" 
    assert_pattern_present $res 3 ".*NEMO.*EXP1.*IMAGE\/.*3VCP[[:digit:]].*microX.*3VCP[[:digit:]]" 

    assert_dir_empty $DATA/in-raw
    assert_dir_empty $DATA/out-raw
    assert_dir_empty $DATA/in-analys
    assert_dir_empty $DATA/out-analys
    assert_dir_empty $DATA/analys-copy
    
    local store_dir=$DATA/main-store
    local imgAnalys="$store_dir/Project_NEMO/Experiment_EXP1/ObservableType_IMAGE_ANALYSIS_DATA/Barcode_3VCP1/1"
    assert_dir_exists "$imgAnalys"
    local rawData="$store_dir/3V/Project_NEMO/Experiment_EXP1/ObservableType_IMAGE/Barcode_3VCP1/1"
    assert_dir_exists "$rawData"
}

function integration_tests {
    install_etl=$1
    install_dmv=$2
    install_lims=$3
    use_local_source=$4
    
    init_log
    build_zips $install_etl $install_dmv $install_lims $use_local_source
    install $install_etl $install_dmv $install_lims
    launch_tests
    assert_correct_results
    shutdown_lims_server
    exit_if_assertion_failed
}

function clean_after_tests {
    echo "Cleaning $INSTALL..."
    rm -fr $INSTALL
    echo "Cleaning $WORK..."
    rm -fr $WORK
}

function print_help {
    echo "Usage: $0 [ (--etl | --lims | --dmv)* | --all [ --local-source ]]"
    echo "	--etl, --lims, --dmv	build chosen components only"
    echo "	--all 			build all components"
    echo "	--local-source		use local source code during building process instead of downloading it from svn"  
    echo "	--clean			clean and exit"
    echo "	--help			displays this help"
    echo "If no option is given, integration tests will be restarted without building anything."
    echo "Examples:"
    echo "- Rebuild everything, fetch sources from svn:"
    echo "	$0 --all"
    echo "- Use lims server and client installation from previous tests, rebuild etl server and datamover using local source:"
    echo "	$0 --etl --dmv --local-source"
    echo "- Rebuild etl server only fetching sources from svn:"
    echo "	$0 --etl"
}

# -- MAIN ------------ 
if [ "$1" = "--clean" ]; then
    clean_after_tests
else
    install_etl=false
    install_dmv=false
    install_lims=false
    use_local_source=false
    while [ ! "$1" = "" ]; do
	case "$1" in
	    '-e'|'--etl')
	        install_etl=true
		;;
	    '-d'|'--dmv')
		install_dmv=true
		;;
	    '-l'|'--lims')
		install_lims=true
		;;
	    '-a'|'--all')
	        install_etl=true
		install_dmv=true
		install_lims=true
		;;
	    '--local-source')
		use_local_source=true
		;;			
	    '--help')
		print_help
		exit 0
		;;
	    *)
		echo "Illegal option $1."
		print_help
		exit 1
		;;
         esac
	 shift
    done
    integration_tests $install_etl $install_dmv $install_lims $use_local_source
fi