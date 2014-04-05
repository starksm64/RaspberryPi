# RaspberryPi JBoss Integration

Raspberry Pi integration work

# Pi4J Project
The [Pi4J](http://pi4j.com) is used as the means for integrating with the Raspberry Pi board from Java.

# Configuration of the pidora image
I had to do the following to install the neccessary dependencies for this RaspberryPi project

1. Install git using `yum install git`
2. Install maven. Note that I could not use the yum install maven command as this fails with many dependency problems. Just download maven from [http://apache.mesi.com.ar/maven/maven-3/3.0.5/binaries/apache-maven-3.0.5-bin.zip](http://apache.mesi.com.ar/maven/maven-3/3.0.5/binaries/apache-maven-3.0.5-bin.zip), unzip and put its bin directory in your PATH.
3. Build and install the wiringPi native project required by the pi4j project:

    `git clone git://git.drogon.net/wiringPi`

    `cd wiringPi`

	`/build`

4. Create links from the /usr/bin/vc* command to 

	`sudo mkdir -p /opt/vc/bin`

	`cd /opt/vc/bin`

	`sudo ln -s /usr/bin/vcgencmd`

	`sudo ln -s /usr/bin/vcdbg`

	`sudo ln -s /usr/bin/vchiq_test`
	
