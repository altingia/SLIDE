#!/bin/sh

# ****** AUTOGENERATED CODE *******
export install_dir="/Users/abhikdatta/Desktop/26 Sep 2017/slide"
export mongodb_dir="/usr/local/Cellar/mongodb/3.4.9/bin"
export glassfish_dir="/Users/abhikdatta/glassfish4/glassfish/bin"
# *********************************

$mongodb_dir/mongod&

"$glassfish_dir"/asadmin start-domain
"$glassfish_dir"/asadmin deploy --force=true "$install_dir"/lib/VTBox.war