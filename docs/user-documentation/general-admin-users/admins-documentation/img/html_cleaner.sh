#!/bin/bash

declare -A dict

dict['&']='&amp;'
dict['<']='&lt;'
dict['>']='&gt;'
dict['"']='&quot;'
dict['–']='&ndash;'
dict['—']='&mdash;'
dict['©']='&copy;'
dict['®']='&reg;'
dict['™']='&trade;'
dict['≈']='&asymp;'
dict['≠']='&ne;'
dict['£']='&pound;'
dict['€']='&euro;'
dict['°']='&deg;'

for key in "${!dict[@]}"; do
    echo "${dict[$key]}"
    grep -irn --include \*.md "${dict[$key]}" ./docs/*
    #grep -irl --include \*.md "${dict[$key]}" ./docs/* | xargs sed -i -e 's/${dict[$key]}/${key}/g'
done
