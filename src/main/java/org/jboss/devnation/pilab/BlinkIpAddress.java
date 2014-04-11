package org.jboss.devnation.pilab;
/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.jboss.logging.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class BlinkIpAddress implements Runnable {
   private static Logger logger = Logger.getLogger(BlinkIpAddress.class);
   /** Path to file system trigger for stopping the blinking */
   static final String STOP = "/var/lib/blink-stop";
   /** Path to file system trigger for stopping the blinking */
   static final String START = "/var/lib/blink-start";
   /** Path to file system trigger for shutting down the blinking */
   static final String SHUTDOWN = "/var/lib/blink-shutdown";
   /** Path to file system trigger for shutting down the blinking */
   static final String INDEX = "/var/lib/blink-index";

   /** Path to file system control of LED0 */
   private static final String LED0 = "/sys/class/leds/led0/brightness";
   private FileOutputStream led0;
   private int iterations = -1;
   private char[] fullIpAddress;
   private String fullIpAddressString;
   private String macaddr;
   private int fullIpAddressIndex;
   private Exception errorState;
   private volatile boolean stopped;

   public static String getStop() {
      return STOP;
   }

   public static String getStart() {
      return START;
   }

   public static String getShutdown() {
      return SHUTDOWN;
   }

   public static String getIndex() {
      return INDEX;
   }

   public BlinkIpAddress() {
      this(-1);
   }

   /**
    *
    * @param iterations number of iterations to perform, <0 == infinite
    */
   public BlinkIpAddress(int iterations) {
      this.iterations = iterations;
   }

   public String getMac() {
      return macaddr;
   }
   public char[] getFullIpAddress() {
      return fullIpAddress;
   }
   public int getFullIpAddressIndex() {
      return fullIpAddressIndex;
   }

   public Exception getErrorState() {
      return errorState;
   }

   public boolean isStopped() {
      return stopped;
   }

   public void run() {
      try {
         errorState = null;
         stopped = false;
         start();
      } catch (Exception e) {
         stopped = true;
         logger.error("Failed to start blinking", e);
         errorState = e;
      }
   }

   public void init() throws SocketException {
      Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
      NetworkInterface nonloopback = null;
      // Choose the first non-loopback interface
      while(ifaces.hasMoreElements()) {
         NetworkInterface iface = ifaces.nextElement();
         logger.infof("Checking iface: %s\n", iface.getDisplayName());
         if(iface.isUp() && iface.isLoopback() == false) {
            nonloopback = iface;
         }
      }

      if(nonloopback == null) {
         throw new IllegalStateException("Failed to find an nonloopback interface that is up");
      }
      byte[] mac = nonloopback.getHardwareAddress();
      StringBuilder macaddr = new StringBuilder();
      for(int n = 0; n < mac.length; n ++) {
         String x = String.format("%x", mac[n]);
         macaddr.append(x);
         macaddr.append(':');
      }
      macaddr.setLength(macaddr.length()-1);
      this.macaddr = macaddr.toString();
      Enumeration<InetAddress> inetAddresses = nonloopback.getInetAddresses();
      InetAddress inetAddress = null;
      while(inetAddresses.hasMoreElements()) {
         inetAddress = inetAddresses.nextElement();
         if(inetAddress instanceof Inet4Address)
            break;
      }
      // Print out interface name, ip address and mac address
      logger.infof("Selected %s, %s[%s]\n", nonloopback.getDisplayName(), inetAddress, macaddr);

      String iterStr = iterations < 0 ? "infinite" : ""+iterations;
      logger.infof("Doing (%s) iterations of the link sequence for IPAddress(%s); -1==infinite\n", iterStr,
         inetAddress.getHostAddress());

      // Build the fully padded xxx.xxx.xxx.xxx address so we can represent every digit in the address
      StringBuilder fullyPaddedAddress = new StringBuilder();
      fullIpAddress = new char[12];
      byte[] baddr = inetAddress.getAddress();
      char[][] octets = {{'0', '0', '0'},{'0', '0', '0'}, {'0', '0', '0'}, {'0', '0', '0'}};
      for(int i = 0, j = 0; j < baddr.length; i+= 3, j ++) {
         int octet = baddr[j] < 0 ? 256 + baddr[j] : baddr[j];
         logger.infof("%d: %03d(%d)\n", j, octet, baddr[j]);
         String tmp = String.format("%03d", octet);
         octets[j][0] = tmp.charAt(0);
         octets[j][1] = tmp.charAt(1);
         octets[j][2] = tmp.charAt(2);
         fullIpAddress[i] = tmp.charAt(0);
         fullIpAddress[i+1] = tmp.charAt(1);
         fullIpAddress[i+2] = tmp.charAt(2);
         fullyPaddedAddress.append(octets[j]);
         fullyPaddedAddress.append('.');
      }
      fullyPaddedAddress.setLength(fullyPaddedAddress.length()-1);
      fullIpAddressString = fullyPaddedAddress.toString();
   }

   /**
    * Run the blink sequence on LED0(ACT)
    * @throws Exception
    */
   public void start() throws Exception {
      logger.debugf(new Exception("Trace"), "start called from here\n");

      // Begin flashing the address code
      led0 = new FileOutputStream(LED0);
      int count = 0;
      while(!stopped && (count < iterations || iterations < 0)) {
         logger.infof("Starting %s display\n", fullIpAddressString);
         display_start();
         fullIpAddressIndex = 0;
         while(fullIpAddressIndex < fullIpAddress.length) {
            char c = fullIpAddress[fullIpAddressIndex];
            logger.infof("%c\n", c);
            if(c == '0')
               display_zero();
            else
               display_blink(c, 500, 500);
            Thread.sleep(1000);
            fullIpAddressIndex ++;
         }
         logger.infof("------------\n");
      }
   }
   public void stop() {
      stopped = true;
   }

   /**
    * Flashes the led on/off 50 times sleeping 10ms between flashes
    * @throws Exception
    */
   private void display_start() throws Exception {
       for (int i = 0; i < 50; i ++) {
           Thread.sleep(100);
           led0.write(Character.forDigit(1, 10));
          Thread.sleep(100);
           led0.write(Character.forDigit(0, 10));
       }
       Thread.sleep(4000);
   }

   /**
    * Flashes the led on/off 10 times sleeping 10ms between flashes
    * @throws Exception
    */
   private void display_zero() throws Exception {
      for (int i = 0; i < 10; i ++) {
         Thread.sleep(100);
         led0.write(Character.forDigit(1, 10));
         Thread.sleep(100);
         led0.write(Character.forDigit(0, 10));
       }
   }

   /**
    * Flash the led on/off digit times with the leg on dwell ms and off delay ms
    * @param c
    * @param delay
    * @param dwell
    * @throws Exception
    */
   private void display_blink(char c, int delay, int dwell) throws Exception {
      int digit = Character.digit(c, 10);
      for (int i = 0; i < digit; i ++) {
         Thread.sleep(dwell);
         led0.write(Character.forDigit(1, 10));
         Thread.sleep(delay);
         led0.write(Character.forDigit(0, 10));
       }
   }

   private static void deleteOrExit(File trigger) {
      if(trigger.exists()) {
         System.err.printf("%s exists, attempting to delete...", trigger.getAbsolutePath());
         boolean removed = trigger.delete();
         System.err.printf("%s\n", (removed ? "Success" : "Failed"));
         if(!removed) {
            System.exit(1);
         }
      }
   }
   /**
    * Allow this to run standalone.
    * @param args
    */
   public static void main(String[] args) throws Exception {
      ExecutorService service = Executors.newFixedThreadPool(1);
      BlinkIpAddress blink = new BlinkIpAddress();
      blink.init();
      Future blinkFuture = service.submit(blink);

      // Validate the filesystem triggers
      File shutdown = new File(SHUTDOWN);
      deleteOrExit(shutdown);
      File stop = new File(STOP);
      deleteOrExit(stop);
      File start = new File(START);
      deleteOrExit(start);
      RandomAccessFile index = new RandomAccessFile(INDEX, "rwd");

      // Loop, monitoring the /var/local/* file system triggers
      while(shutdown.exists() == false) {
         try {
            Thread.sleep(500);
            index.writeInt(blink.getFullIpAddressIndex());
            index.seek(0);

            // Check for a stop trigger
            if(stop.exists()) {
               System.out.printf("STOP marker seen, cancelling blinking\n");
               deleteOrExit(stop);
               if(blinkFuture != null)
                  blinkFuture.cancel(true);
               else
                  System.out.printf("Ignored, no blink task exists\n");
               blinkFuture = null;
            }
            // Check for a shutdown trigger
            if(shutdown.exists()) {
               System.out.printf("SHUTDOWN marker seen, exiting\n");
               break;
            }
            // Check for a start trigger
            if(start.exists()) {
               System.out.printf("START marker seen, resuming blinking\n");
               deleteOrExit(start);
               if(blinkFuture == null)
                  blinkFuture = service.submit(blink);
               else
                  System.out.printf("Ignored, blink task exists\n");
            }
         } catch (Exception e) {
            System.err.printf("Exiting on Exception, msg=%s\n", e.getMessage());
            break;
         }
      }
      System.out.printf("Shutting down...\n");
      if(blinkFuture != null)
         blinkFuture.cancel(true);
      service.shutdownNow();
      shutdown.delete();
   }
}
