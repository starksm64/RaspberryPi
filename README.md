# RaspberryPi JBoss Integration

Raspberry Pi integration work

# Booting of the pidora image
Place the SD card with the pidora image you have into the Raspberry Pi's SD card slot, plug the power into the micro USB port, and watch the LET labeled ACT on the board. During the boot, it will flash chaotically as an indication of read activity from the SD card. After a minute or so, the flashing will become very regular. This is a coded represtion of the Pi's IP address. The pattern is, as an interval starts:

1. the led flashes quickly for about 10 secs, followed by 4 secs off.
2. The for each digit of the ip address it will:
   a. flash once up to the value of the digit, followed by about a 1 sec delay.
   b. if the digit is 0, it flashes quickly for a couple of secs
followed by about a 1 sec delay.

Then the cycle repeats itself. See if you can determine you Pi's IP
address by observing this. To validate the IP address you have determined, attempt to SSH into your Pi using this address and the login username jbosspi:

`ssh -l jbosspi myip`

The password is also jbosspi

# Accessing the iotweb Application
It takes a few minutes for the wildfly server to boot and fully deploy the sample web application, but when it is deployed, you can access it at:
http://mypiaddr:8080/iotweb/index.xhtml

There are links to the wildfly admin console in the applications Links menu as well.

# Pi4J Project
The [Pi4J](http://pi4j.com) is used as the means for integrating with the Raspberry Pi board from Java.

	
