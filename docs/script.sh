for i in $(find . -type f -name '*.md' -print)
do
        echo " -- "
        echo " FIXING FILE ${i}..."
        for string in `grep -oP '\(https://openbis.readthedocs.io/en/latest/\K[^\)]+' $i`
        do
                new_string=`echo "/home/marco/openbis/docs/${string}" | sed s/html/md/g | cut -d '#' -f 1`
                file2=$new_string
                file1=$i
                new_string=`realpath --relative-to="$file1" "$file2"`
                if [[ $new_string == "." ]]
                then
                        new_string=`basename $i`
                        new_string="./${new_string}"
                fi
                if [[ $new_string =~ ^\.\.\/[a-zA-Z]+ ]]
                then
                        new_string="${new_string:1}"
                fi
                if [[ $new_string =~ ^\.\.\/\.\. ]]
                then
                	#new_string="${new_string:3}"
			echo $new_string
			new_string=`echo $new_string | sed s,"../",,`
			echo $new_string
		fi
                original_string=`echo "https://openbis.readthedocs.io/en/latest/${string}" | cut -d '#' -f 1`
                #echo $original_string
                #echo $new_string
                #sed -i s,"$original_string","$new_string",g $i
        done
done
