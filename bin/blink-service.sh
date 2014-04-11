#! /bin/bash -p
### BEGIN INIT INFO
# Provides: blink-service
# Required-Start: network
# Default-Start: 2 3 4 5
# Default-Stop: 0 1 6
# Short-Description: Bring up/down the blink ip address service
# Description: Bring up/down the blink ip address service
### END INIT INFO
# Source function library.
. /etc/init.d/functions
blinkjar=/home/jbosspi/blink-service.jar
export BLINK_PIDFILE=/home/jbosspi/blink-service.pid
JAVA=/etc/alternatives/jre_openjdk/bin/java

start() {
    echo -n "Starting $blinkjar:"
    $JAVA -jar $blinkjar >& /home/jbosspi/blink.log &
    retval=$?
    return $retval
}

stop() {
    echo -n $"Stopping $blinkjar: "
    touch /var/lib/blink-stop
    echo "exiting stop method"
    success
}

shutdown() {
    echo -n $"Shutting down $blinkjar: "
    touch /var/lib/blink-shutdown
    echo "exiting shutdown method"
    success
}

restart() {
    stop
    start
}

status() {
	if [ -f $BLINK_PIDFILE ]; then
		read ppid < $BLINK_PIDFILE
		if [ `ps --pid $ppid 2> /dev/null | grep -c $ppid 2> /dev/null` -eq '1' ]; then
			echo "blink is running (pid $ppid)"
			return 0
		else
			echo "blink dead but pid file exists"
			return 1
		fi
	fi
	echo "blink is not running"
	return 3
}

case "$1" in
	start)
		start
		;;
	stop)
		stop
		;;
	restart)
		restart
		;;
	status)
		status
		;;
	shutdown)
		shutdown
		;;
	*)
		## If no parameters are given, print which are avaiable.
		echo "Usage: $0 {start|stop|status|restart|shutdown}"
		exit 1
		;;
esac
