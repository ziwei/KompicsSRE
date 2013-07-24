#!/bin/bash
# --------------------------------------------
# /etc/init.d/vision-sre
# initd script for tomcat application SRE.war
# --------------------------------------------
# Source function library.
. /etc/rc.d/init.d/functions

case "$1" in
	start)
		echo -n "Starting sre service: "
		RESULT=$(curl -s --anyauth -u vision:vision http://localhost:8080/manager/text/start?path=/SRE)
		echo $RESULT
		logger -t vision-sre "Starting sre service: $RESULT"
	;;
	stop)
		echo -n "Stopping sre service: "
		RESULT=$(curl -s --anyauth -u vision:vision http://localhost:8080/manager/text/stop?path=/SRE)
		echo $RESULT
        logger -t vision-sre "Stopping sre service: $RESULT"
	;;
	status)
		echo -n "Getting status of sre service: "
		RESULT=$(curl -s --anyauth -u vision:vision http://localhost:8080/manager/text/list?path=/SRE | grep SRE | awk 'BEGIN{ FS = ":" }{ print $2 }{}')
	    echo $RESULT
	    logger -t vision-sre "Getting status of sre service: $RESULT"
	;;
	restart)
		echo -n "Restaring sre service: first stopping: "
		RESULT=$(curl -s --anyauth -u vision:vision http://localhost:8080/manager/text/stop?path=/SRE)
		echo $RESULT
	    logger -t vision-sre "Restaring sre service, stop result: $RESULT"
		echo -n "Restaring sre service: then restarting: "
		RESULT=$(curl -s --anyauth -u vision:vision http://localhost:8080/manager/text/start?path=/SRE)
		echo $RESULT
	    logger -t vision-sre "Restaring sre service, then restarting: $RESULT"
	;;
	reload)
		echo -n "Reloading sre service: "
		export RESULT=$(curl -s --anyauth -u vision:vision http://localhost:8080/manager/text/reload?path=/SRE)
		echo $RESULT
	    logger -t vision-sre "Reloading sre service: $RESULT"
	;;
	probe)
		echo -n "(probe not supported at the moment)"
	;;
	*)
		echo "Usage: vision-sre {start|stop|status|reload|restart[|probe]"
		exit 1
	;;
esac