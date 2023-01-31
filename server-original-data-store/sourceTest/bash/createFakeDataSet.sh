#!/bin/sh
#
# @date: 2008-04-18
# @author: franz-josef.elmer@systemsx.ch
# @author: basil.neff@systemsx.ch
# 
# This bash script generates dummy data to test the installation.
# The output is currently in the Subfolder of the Script: <SAMPLE COLDE>/TIFF
# The Files are empty and have the following filename:
# <SAMPLE CODE>_<PLATE ROW (as character)><PLATE COLUMN>_<TILE NUMBER>_w<WAVELENGTH>.tif
#

###################
## USED BINARIES ##
###################
TOUCH=/usr/bin/touch
BC=/usr/bin/bc


############################
## DO NOT CROSS THIS LINE ##
############################

if [ $# -ne 4 ]; then
    echo "Usage: `basename $0` <sample code> <number of channels> <plate geometry> <well geometry>"
    exit 1
fi

SAMPLE=$1
CHANNELS=$2
PLATE_GEOMETRY=$3
WELL_GEOMETRY=$4



if [ ${CHANNELS} -le 0 ]; then
	echo "<number of channnels> has to be greater than 0"
	exit 1
fi

case ${PLATE_GEOMETRY} in
    8x12)
        PLATE_ROWS=8
        PLATE_COLUMNS=12
        ;;
    16x24)
        PLATE_ROWS=16
        PLATE_COLUMNS=24
        ;;
    32x48)
        PLATE_ROWS=32
        PLATE_COLUMNS=48
        ;;
    *)
        echo "Plate geometry has to be '8x12', '16x24', or '32x48'"
        exit 1
esac

case ${WELL_GEOMETRY} in 
    1x1)
        WELL_ROWS=1
        WELL_COLUMNS=1
        TILES=1
        ;;       
    2x2)
        WELL_ROWS=2
        WELL_COLUMNS=2
        TILES=4
        ;;       
    3x3)
        WELL_ROWS=3
        WELL_COLUMNS=3
        TILES=9
        ;;       
    *)
        echo "Well geometry has to be '1x1', '2x2', or '3x3'"
        exit 1
esac

###############
## FUNCTIONS ##
###############

function getCharacterFromInt {
	POSITION=$1
	if [ ${POSITION} -lt 26 ];then
		characters=(A B C D E F G H I J K L M N O P Q R S T U V W X Y Z)
		echo ${characters[${POSITION}]}
	else
		FIRST_INTEGER=`echo $[(${POSITION}-25)/25]|${BC}`
		FIRST_CHARACTER=`getCharacterFromInt ${FIRST_INTEGER}`
		SECOND_CHARACTER=`getCharacterFromInt $[${POSITION}%26]`
		echo "${FIRST_CHARACTER}${SECOND_CHARACTER}"
	fi
}

function getWavelength {
	WAVELENGTH=$1
	echo "${WAVELENGTH}42"
	}

PROGRESS_INFO=1
function printProgress {
	PERCENT=`echo $1|${BC}`
	WHEEL=`getProgressWheel ${PROGRESS_INFO}`
  echo -ne "    ${PERCENT}% [${WHEEL}]\r"

	PROGRESS_INFO=$[$PROGRESS_INFO+1]
}

function getProgressWheel {
	PARAMETER=$1
	modulo_tree=`echo $[${PARAMETER}%3]`
	progress_array=(\| / - \\)
	echo ${progress_array[${modulo_tree}]}
}

############
## SCRIPT ##
############

echo "${CHANNELS} channels, plate: ${PLATE_ROWS} x ${PLATE_COLUMNS}, well: ${WELL_ROWS} x ${WELL_COLUMNS}"

if [ ! -d ${SAMPLE} ];then
	mkdir ${SAMPLE}
fi
if [ ! -d ${SAMPLE}/TIFF ];then
	mkdir ${SAMPLE}/TIFF
fi

###
# LOOPS
###

OVERALL_COUNTER=0
TOTAL_FILES_TO_GENERATE=$[${CHANNELS}*${PLATE_ROWS}*${PLATE_COLUMNS}*${TILES}]
# Plate Rows
plateRowCounter=0
while [ ${plateRowCounter} -lt ${PLATE_ROWS} ]
do

	# Plate Columns
	plateColumsCounter=1
	while [ ${plateColumsCounter} -le ${PLATE_COLUMNS} ]
	do
	
		PLATE_ROW_CHARACTER=`getCharacterFromInt ${plateRowCounter}`
		
		# Plate Column
		if [ ${plateColumsCounter} -lt 10 ];then
			PLATE_COLUMN_INT=0${plateColumsCounter}
		else
			PLATE_COLUMN_INT=${plateColumsCounter}
		fi
		
		# TILES
		tileCounter=1
		while [ ${tileCounter} -le ${TILES} ]
		do
			# Channels
			channelCounter=1
			while [ ${channelCounter} -le ${CHANNELS} ]
			do
				FILECHANEL=`getWavelength ${channelCounter}`
				${TOUCH} ${SAMPLE}/TIFF/${SAMPLE}_${PLATE_ROW_CHARACTER}${PLATE_COLUMN_INT}_${tileCounter}_w${FILECHANEL}.tif      	
				
				OVERALL_COUNTER=$[${OVERALL_COUNTER}+1]
				channelCounter=$[${channelCounter}+1]
			done
    		tileCounter=$[$tileCounter+1]
		done
		printProgress $((${OVERALL_COUNTER}*100/${TOTAL_FILES_TO_GENERATE}))
    	plateColumsCounter=$[$plateColumsCounter+1]
	done
    plateRowCounter=$[$plateRowCounter+1]
done

echo ""
echo "${OVERALL_COUNTER} files generated"
