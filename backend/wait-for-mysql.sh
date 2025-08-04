#!/bin/sh
# wait-for-mysql.sh

set -e

host="$1"
shift
cmd="$@"

echo "Waiting for MySQL at $host to be ready..."

max_attempts=60
attempt=1

# Enable debugging
set -x

echo "Step 1: Checking if MySQL port 3306 is open on $host..."

# First check if the port is open
until nc -z "$host" 3306; do
  if [ $attempt -ge $max_attempts ]; then
    echo "Failed to connect to MySQL port after $max_attempts attempts"
    exit 1
  fi
  
  echo "MySQL port is not ready - attempt $attempt/$max_attempts - sleeping for 2 seconds"
  sleep 2
  attempt=$((attempt + 1))
done

echo "MySQL port is open, checking if MySQL service is ready..."
attempt=1

echo "Step 2: Checking if MySQL service responds to ping..."

# Then check if MySQL service is ready to accept connections
until mysqladmin ping -h"$host" -u"root" -proot --silent; do
  if [ $attempt -ge $max_attempts ]; then
    echo "Failed to ping MySQL service after $max_attempts attempts"
    echo "Attempting one more connection test..."
    mysql -h"$host" -u"root" -proot -e 'SELECT 1' || echo "MySQL connection test failed"
    exit 1
  fi
  
  echo "MySQL service is not ready - attempt $attempt/$max_attempts - sleeping for 2 seconds"
  sleep 2
  attempt=$((attempt + 1))
done

echo "Step 3: Final connection test..."
mysql -h"$host" -u"root" -proot -e 'SELECT 1' && echo "MySQL connection test successful!"

# Disable debugging
set +x

echo "MySQL is up and ready - executing command"
exec $cmd
