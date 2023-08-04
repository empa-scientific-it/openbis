#!/bin/bash

for ext in xls xlsx pdf txt jpg; do
	grep -Eorn  --include \*.md "https://[^ >]+\.${ext}" ./docs/*
done
