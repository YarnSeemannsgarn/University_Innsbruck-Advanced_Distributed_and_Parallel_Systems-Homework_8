#!/bin/sh

# remove include statements in povray scenes by replacing them with the content of the included file
# allows to use them without having the complete include directory available when running povray

POVRAY_INC_DIR=./povray-3.6.1-include/

if [ $# -ne 1 ]; then
	echo "usage: $0 pov-file"
	exit 1
fi

input="$1"
output="$(basename $input .pov)_noinc.pov"

cp "$input" "$output"

while grep -q "^#include " "$output"
do
	sed -i -r "s@#include \"(.*)\"@cat $POVRAY_INC_DIR\\1@e" $output
done
