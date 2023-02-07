#!/bin/bash
dropboxesNames=('runDbShort' 'runDbForever' 'idleDb' 'massiveDb' 'neverFailedDb' 'neverSuccededDb' 'neverRunDb')

if [ ! -d failed ]
  then
  mkdir "failed"
  echo "Created failed filelog directory"
else
  touch failed/randomFile
  rm failed/*
  echo "Cleared failed filelog directory"

fi

if [ ! -d succeeded ]
  then
  mkdir "succeeded"
  echo "Created succeeded filelog directory"
else
  touch succeeded/randomFile
  rm succeeded/*
  echo "Cleared succeeded filelog directory"
fi

if [ ! -d "in-process" ]
  then
  mkdir "in-process"
  echo "Created in-process filelog directory"
else
  touch in-process/randomFile
  rm in-process/*
  echo "Cleared in-process filelog directory"
fi

#generate runDbShort
echo "Created runDbShort data"
filename=$(date +"%Y-%m-%d_%I-%M-%S-000_runDbShort_testfile.svg.log")
touch "in-process/${filename}"
echo "Created runDbShort data"
#done

#generate runDbForever
echo "Creating runDbForever data"
filename="1970-01-01_00-00-00-000_runDbForever_testfileForever.frvr.log"
touch "in-process/${filename}"
echo "Created runDbForever data"
#done

#generate idleDb
echo "Creating idleDb data"
filename1="1970-01-01_00-00-00-000_idleDb_testidleDb.log"
filename2="1970-01-02_00-00-00-000_idleDb_testidleDb.log"
touch "failed/${filename1}"
touch "succeeded/${filename2}"
echo "Created idleDb data"
#done

#generate massiveDb
echo "Creating massiveDb data"
for i in {1..1000}
do
  rnd=$(( ( RANDOM % 2 ) ))
  rnd2=$(( ( RANDOM % 3 ) ))
  rnd3=$(( ( RANDOM % 5 ) ))
  rnd4=$(( ( RANDOM % 9 ) +1 ))
  rnd5=$(( ( RANDOM % 9 ) +1 ))
  touch "failed/19${rnd4}0-0${rnd5}-0${rnd4}_${rnd}${rnd4}-00-00-000_massiveDb_massivefail${i}.log"
  touch "succeeded/19${rnd4}${rnd5}-0${rnd4}-0${rnd5}_${rnd}${rnd}-00-00-000_massiveDb_massivesucc${i}.log"
done
echo "Created massiveDb data"

#done

#generate neverFailedDb
echo "Creating neverFailedDb data"
for i in {1..10}
do
  rnd=$(( ( RANDOM % 2 ) ))
  rnd2=$(( ( RANDOM % 3 ) ))
  rnd3=$(( ( RANDOM % 5 ) ))
  rnd4=$(( ( RANDOM % 9 ) +1 ))
  rnd5=$(( ( RANDOM % 9 ) +1 ))
  touch "succeeded/19${rnd4}0-0${rnd5}-0${rnd4}_${rnd}${rnd4}-00-00-000_neverFailedDb${i}.log"
  touch "succeeded/20${rnd}${rnd2}-0${rnd5}-0${rnd4}_${rnd}${rnd4}-00-00-000_neverFailedDb${i}${i}.log"
done
echo "Created neverFailedDb data"
#done

#generate neverSuccededDb
echo "Creating neverSuccededDb data"
for i in {1..10}
do
  rnd=$(( ( RANDOM % 2 ) ))
  rnd2=$(( ( RANDOM % 3 ) ))
  rnd3=$(( ( RANDOM % 5 ) ))
  rnd4=$(( ( RANDOM % 9 ) +1 ))
  rnd5=$(( ( RANDOM % 9 ) +1 ))
  touch "failed/19${rnd4}0-0${rnd5}-0${rnd4}_${rnd}${rnd4}-00-00-000_neverSuccededDb${i}.log"
  touch "failed/20${rnd}${rnd2}-0${rnd5}-0${rnd4}_${rnd}${rnd4}-00-00-000_neverSuccededDb${i}${i}.log"
done
echo "Created neverSuccededDb data"
#done


#generate neverRunDb
echo "Creating neverRunDb data"
echo "Created neverRunDb data"
#done
