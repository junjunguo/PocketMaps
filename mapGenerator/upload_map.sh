#!/bin/bash

# ############## Pauls script ##############
# This script uploads maps from one server to a second server, to leverage server load and storage.
# Also updating file htaccess and deleting old file.
# So uri will be redirected via apache mod_rewrite.

SERVER_IP="185.164.4.149"
SERVER_ACC="paul@$SERVER_IP"

if [ -z "$1" ]; then
  echo "Enter map-file as arg."
  exit 1
elif [ ! -f "$1" ]; then
  echo "File does not exist: $1"
  exit 1
fi

print_error() # Arg: msg
{
  echo "$1"
  exit 1
}

start_upl() # Arg: map-file
{
  local server_maps_dir="/var/www/html/maps/"
  local file_name=$(basename "$1")
  local file_date=$(dirname "$1")
  local file_date=$(basename "$file_date")
  local cur_connect="ssh $SERVER_ACC"
  if $cur_connect test -f "$server_maps_dir/$file_date/$file_name" ; then
    echo "Error: file already existing on server!" 1>&2
    exit 1
  fi
  local free_space=$($cur_connect "df --output=avail \"$server_maps_dir\"" | sed 1d)
  if [ "$free_space" -lt 10000000 ]; then
    echo "Error: disk space low on server!" 1>&2
    exit 1
  fi
  ssh $SERVER_ACC mkdir -p "$server_maps_dir/$file_date" || print_error "Cannot create directory on server."
  scp "$1" $SERVER_ACC:"$server_maps_dir/$file_date/$file_name" || print_error "Error uploading"
  echo "Uploading src: $1"
  echo "Uploading tar: $SERVER_ACC:$server_maps_dir/$file_date/$file_name"
  cat /var/www/html/.htaccess | grep -v "$file_date/$file_name" > /tmp/htaccess.tmp
  echo "# File=$file_date/$file_name" >> /tmp/htaccess.tmp
  echo 'RewriteRule ^(.*)'"/$file_date/$file_name"' http://$SERVER_IP/maps/'"$file_date/$file_name"' [L]' >> /tmp/htaccess.tmp
  echo "##### htaccess-tail #####"
  tail -n 4 "/tmp/htaccess.tmp"
  echo "####################"
  cat "/tmp/htaccess.tmp" > /var/www/html/.htaccess
  rm "$1"
  echo "Finish"
}

while [ ! -z "$1" ]; do
  start_upl "$1"
  shift
done
