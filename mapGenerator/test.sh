pathmap=/Volumes/GJJexFAT/development/test/
mapextension=.map
continent=northAmerica_US_
slash=/
# delte all *.osm.pbf files
# prepare name.map : if map name contains '-' replace with '_'
echo "$pathmap"
for mname in $pathmap*"-"*; do
	echo "$mname"
	mv "$mname" "${mname//-/_}"
done
