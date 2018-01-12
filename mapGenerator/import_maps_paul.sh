#!/bin/bash

##############################################################################
##
##  Starcommander@github.com
##  Pauls script to download and convert all maps!
##  The only manual steps:
##  - Import the mapfile-writer
##  - - See also https://github.com/mapsforge/mapsforge-creator
##  - Install: zip svn wget curl
##
##  ============= Steps that are executed automatically: ===========
##  Download the osm-maps from:
##  - http://download.geofabrik.de/
##  Import the map:
##  - ./graphhopper.sh import downloadedMap.osm.pbf
##  Create the map file with osmosis tool:
##  - osmosis --rb file=/tmp/berlin.osm.pbf --mapfile-writer file=/tmp/berlin.map
##  - - See also http://wiki.openstreetmap.org/wiki/Osmosis/Installation
##  Create file cityNodes.txt for limited OfflineSearch (Geocoding)
##  Compress the content of mapdir-gh to mapdir.ghz
##  Create a list for json file
##
##############################################################################

WORK_DIR="/tmp/graphhopper_0-9-0/"
HOPPER_REP="https://github.com/graphhopper/graphhopper.git/tags/0.9.0"
GEO_TMP="/tmp/geofabrik-list.txt"
GEO_URL="http://download.geofabrik.de/"
MAP_DIR="/tmp/graphhopper_0-9-0/maps-osm/"
CONTINUE="ask"
MEMORY_USE="2048m"
# Tags: VERSION_DEBUG VERSION_USED MANIPULATE

print_map_list() # Args europe
{
  curl "$GEO_URL$1.html" > "$GEO_TMP"
  local lineS=$(cat -n "$GEO_TMP" | grep "Sub Regions" | head -n 1 | awk '{ print $1; }')
  local lineE=$(cat -n "$GEO_TMP" | grep "Special Sub Regions" | head -n 1 | awk '{ print $1; }')
  local content=$(cat "$GEO_TMP" | tail --lines="+$lineS")
  local content=$(echo "$content" | head --lines="$lineE")
  echo "$content" | grep -o 'href=".*-latest\.osm\.pbf\"'
}

check_exist() # Args: file|dir
{
  if [ ! -e "$1" ]; then
    echo "Error, path is missing: $1"
    exit 1
  fi
}

goto_graphhopper()
{
  export JAVA_OPTS="-Xmx$MEMORY_USE -Xms1000m -server"
  if [ -d "$WORK_DIR/gh" ]; then
    cd "$WORK_DIR/gh"
    return
  fi
  mkdir -p "$WORK_DIR"
  cd "$WORK_DIR"
  echo "Checking out graphhopper repository, please wait ..."
  # VERSION_USED: graphhopper_0.9.0
  svn co "$HOPPER_REP"
  local hopper_dirname=$(basename "$HOPPER_REP")
  mv "$hopper_dirname" gh
  cd gh
  # MANIPULATE: The default config must be changed, because some flags are missing otherwise.
  # MANIPULATE: Also changed to use hdd instead of ram memory.
  # MANIPULATE: Also in PocketMaps: MapHandler.java: "shortest" must be added manually
  cp config-example.properties config.properties
  sed -i -e "s#^graph.flag_encoders=car\$#graph.flag_encoders=car,bike,foot#g" config.properties
  sed -i -e "s#^prepare.ch.weightings=fastest\$#prepare.ch.weightings=fastest,shortest#g" config.properties
  sed -i -e "s#^graph.dataaccess=RAM_STORE\$#graph.dataaccess=MMAP_STORE#g" config.properties
  # VERSION_DEBUG: The java file returns the wrong version --> patch.
  sed -i -e "s#^        return 3;\$#        return 4;#g" core/src/main/java/com/graphhopper/routing/util/FootFlagEncoder.java
}

goto_osmosis()
{
  local work_tmp_dir="$WORK_DIR/osmosis_tmp"
  export JAVACMD_OPTIONS="-Xmx$MEMORY_USE -server -Djava.io.tmpdir=$work_tmp_dir"
  if [ ! -d "$work_tmp_dir" ]; then
    mkdir "$work_tmp_dir"
  fi
  if [ -d "$WORK_DIR/osmosis" ]; then
    cd "$WORK_DIR/osmosis"
    return
  fi
  mkdir -p "$WORK_DIR/osmosis"
  # VERSION_USED: osmosis-0.46.tgz and plugin mapsforge-map-writer-master-20171203.163155-186-jar-with-dependencies.jar
  wget http://bretth.dev.openstreetmap.org/osmosis-build/osmosis-latest.tgz -O "$WORK_DIR/osmosis/osmosis-latest.tgz"
  cd "$WORK_DIR/osmosis"
  tar xvfz osmosis-latest.tgz
  rm osmosis-latest.tgz
  chmod a+x bin/osmosis
}

import_map() # Args: map_url_rel
{
  local map_file=$(echo "$1" | tr '/' '_')
  local gh_map_name=$(echo "$map_file" | sed -e 's/-latest.osm.pbf$//g')
  local gh_map_dir=$(echo "$map_file" | sed -e 's/-latest.osm.pbf$/-gh/g')
  local gh_map_file=$(echo "$map_file" | sed -e 's/-latest.osm.pbf$/.map/g')
  local gh_map_zip=$(echo "$map_file" | sed -e 's/-latest.osm.pbf$/.ghz/g')

  if [ -f "$MAP_DIR$gh_map_zip" ]; then
    echo "Allready existing: $gh_map_zip"
    return
  fi
  mkdir -p "$MAP_DIR"
  if [ ! -f "$MAP_DIR$map_file" ]; then
    wget "$GEO_URL$1" -O "$MAP_DIR$map_file"
  fi
  check_exist "$MAP_DIR$map_file"
  if [ ! -d "$MAP_DIR$gh_map_dir" ]; then
    goto_graphhopper
    ./graphhopper.sh import "$MAP_DIR$map_file"
    mv "$MAP_DIR$gh_map_name"-latest.osm-gh "$MAP_DIR$gh_map_dir"
  fi
  check_exist "$MAP_DIR$gh_map_dir/nodes_ch_fastest_car"
  if [ ! -f "$MAP_DIR$gh_map_dir/$gh_map_file" ]; then
    goto_osmosis
    ./bin/osmosis --rb file="$MAP_DIR$map_file" --mapfile-writer type=hd file="$MAP_DIR$gh_map_dir/$gh_map_file"
    ./bin/osmosis --rb file="$MAP_DIR$map_file" \
                  --tf reject-relations \
                  --tf reject-ways \
                  --tf accept-nodes place=city,town,village \
                  --write-xml "$MAP_DIR$gh_map_dir/cityNodes.osm"
    cat "$MAP_DIR$gh_map_dir/cityNodes.osm" \
         | grep -o "<tag k=\"name\".*\|<node id=\".*\|<tag k=\".*postal_code.*\|</node>" \
         | set -e 's# version=".*"##g' \
         | set -e 's# timestamp=".*"##g' \
         | set -e 's# uid=".*"##g' \
         | set -e 's# user=".*"##g' \
         | set -e 's# changeset=".*"##g' \
         > "$MAP_DIR$gh_map_dir/cityNodes.txt"
    rm "$MAP_DIR$gh_map_dir/cityNodes.osm"
  fi
  check_exist "$MAP_DIR$gh_map_dir/$gh_map_file"
  if [ ! -f "$MAP_DIR$gh_map_zip" ]; then
    cd "$MAP_DIR$gh_map_dir"
    zip -r "$WORK_DIR$gh_map_zip" *
    rm -r -f "$MAP_DIR/$gh_map_dir" # Cleanup
    rm "$MAP_DIR/$map_file" # Cleanup
    mv "$WORK_DIR$gh_map_zip" "$MAP_DIR"
  fi
  check_exist "$MAP_DIR$gh_map_zip"
  local ghz_size=$(du -h "$MAP_DIR$gh_map_zip" | awk '{ print $1 }')
  local ghz_time=$(date +%Y-%m)
  echo "    { \"name\": \"$gh_map_name\", \"size\": \"$ghz_size\", \"time\": \"$ghz_time\" }," >> "$MAP_DIR/All_list.txt"
  echo "Successful created: $gh_map_zip"
}

import_continent() # Args europe
{
  local full_list=$(print_map_list "$1" | cut -d'"' -s -f 2)
  for curUrl in $full_list ; do
    if [ "$CONTINUE" = "ask" ]; then
      echo "=================================="
      echo "Starting with map $curUrl"
      echo "Continue? y=yes b=break a=yesToAll"
      echo "          s=skip w=skipWhooleContinent"
      read -e -p ">>>" ANSWER
      if [ "$ANSWER" = "b" ]; then
        echo "Stop by user, exiting."
        exit 0
      elif [ "$ANSWER" = "a" ]; then
        CONTINUE="yesToAll"
      elif [ "$ANSWER" = "s" ]; then
        continue
      elif [ "$ANSWER" = "w" ]; then
        break
      elif [ "$ANSWER" = "y" ]; then
        echo "Continue ..."
      else
        echo "Unknown user input, exiting."
        exit 1
      fi
    fi
    import_map "$curUrl"
  done
}

import_continent europe
import_continent africa
import_continent antarctica
import_continent asia
import_continent australia-oceania
import_continent central-america
import_continent north-america
import_continent south-america

echo "Finish! Get the maps from $MAP_DIR"
