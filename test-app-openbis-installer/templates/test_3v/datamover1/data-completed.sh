FILE_PATH=$1
if [ $# -eq 2 ]; then
  FILE_PATH=$2:$FILE_PATH
fi
echo Data complete: $FILE_PATH >> data-completed-info.txt
