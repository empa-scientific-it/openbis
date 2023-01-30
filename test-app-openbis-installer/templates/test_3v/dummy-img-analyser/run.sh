#!/bin/bash
IN=../datamover1/data/extra_local_copy
OUT=../datamover2/data/incoming
MARKER=.marker_stop_processing

function log {
    echo $@ >> ./log.txt
}

rm -f $MARKER

log Image analyser started.
while [[ ! -f $MARKER && -d $IN ]]; do 
    sleep 3
    files=`ls $IN`
    if [ "$files" != "" ]; then
        for file in $files; do
            log [Image analyser] Moving $IN/$file to $OUT
            mv $IN/$file $OUT
        done
    fi
done
log Image analyser stopped.
rm -f $MARKER