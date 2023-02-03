#!/bin/bash

version=0.1.0
commands=( addref clone collection commit config data_set download init init_analysis move object removeref repository settings status sync )

mkdir -p man/man1

for command in "${commands[@]}"
do
    echo generating man page for $command...
    help2man --version-string=$version "obis $command" > man/obis-addref.1 > man/man1/obis-$command.1
done

echo done.
