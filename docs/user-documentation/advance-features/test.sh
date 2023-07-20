#!/bin/bash
i=1
line_number=1
while read -r line
do
    if [[ $line == '```'* ]]
    then
        if [[ $(($i % 2)) -ne 0 ]]
        then
            echo "${line_number}: ${line}"
            sed -i "${line_number}s/pythonget/get/" ./${1}
            sed -i "${line_number}s/\`\`\`/\`\`\`bash/" ./${1}
        fi
        i=$((i+1))
    fi
    line_number=$((line_number+1))
done < $1
