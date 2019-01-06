#!/bin/bash

# Execute this script for expanding memory via swap-file (4GiB).
# This will slow down the speed, but prevents MemoryExceptions.

dd if=/dev/zero of=/tmp/swapfile1 bs=1024 count=4194304
chown root:root /tmp/swapfile1
chmod 0600 /tmp/swapfile1
mkswap /tmp/swapfile1
swapon /tmp/swapfile1
