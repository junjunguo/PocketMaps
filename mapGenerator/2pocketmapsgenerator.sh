#!/bin/bash
# run this when the first is completely finished - important!

path=/Volumes/GJJexFAT/development/osm/
#path=/Volumes/GJJexFAT/development/test/osm/
pathmap=/Volumes/GJJexFAT/development/maps/
#pathmap=/Volumes/GJJexFAT/development/test/maps/
pathupload=/Volumes/GJJexFAT/development/upload/
mapextension=.map
#continent=europe_
#continent=europe_UK_
#continent=europe_Germany_
#continent=asia_
#continent=australia_
#continent=northAmerica_canada_
#continent=northAmerica_US_
continent=southAmerica_
slash=/
# delte all *.osm.pbf files
rm -rf $path*.osm.pbf
rm tempzipallmyfiles.sh
# prepare name.map : if map name contains '-' replace with '_'
for mname in $pathmap*"-"*; do
	mv "$mname" "${mname//-/_}"
done
# loop the path directory 
## move and rename the *.map files to right directory
## continent_country.ghz  
for map in $path*.osm-gh; do
	#echo "path: $map"
	mapfname=${map##*/}
	#echo "map full name: $mapfname"
	country=${mapfname//-latest.osm-gh/}
	#echo "country: $country"
	countryname=${country//-/_}
	#echo "contry name: $countryname"
	mv $pathmap$countryname$mapextension $map$slash$continent$countryname$mapextension
	# check if succeed	
	if [ $? -eq 0 ]; then
#    		echo zipping...
		echo "zip -rj "$pathupload$slash$continent$countryname".ghz "$map"/*" >> tempzipallmyfiles.sh
		
	else
    		echo WARNING!!!!FAIL!!!!!!!!!!
	fi
done

bash tempzipallmyfiles.sh


