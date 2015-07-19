#run this first in graphhopper/ folder 
#generate grafhopper files from *.osm.pbf

#path=/Volumes/GJJexFAT/development/test/osm/
path=/Volumes/GJJexFAT/development/osm/

#bash graphhopper.sh import /Volumes/GJJexFAT/development/test/osm/andorra-latest.osm.pbf

rm generatebashlist.sh
 
for map in $path*.pbf; do
	echo "$map"
	line="bash graphhopper.sh import "
	c=$line$map
	echo "$c" >> generatebashlist.sh
done

bash generatebashlist.sh 
