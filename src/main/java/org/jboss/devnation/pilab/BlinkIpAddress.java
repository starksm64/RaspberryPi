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

import java.io.FileOutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class BlinkIpAddress implements Runnable {
   private static Logger logger = Logger.getLogger(BlinkIpAddress.class);
   /** Path to file system control of LED0 */
   private static final String LED0 = "/sys/class/leds/led0/brightness";
   private FileOutputStream led0;
   private int iterations = -1;
   private char[] fullIpAddress;
   private String macaddr;
   private int fullIpAddressIndex;
   private Exception errorState;
   private volatile boolean stopped;

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

   /**
    * Run the blink sequence on LED0(ACT)
    * @throws Exception
    */
   public void start() throws Exception {
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

      // Begin flashing the address code
      led0 = new FileOutputStream(LED0);
      int count = 0;
      while(!stopped && (count < iterations || iterations < 0)) {
         logger.infof("Starting %s display\n", fullyPaddedAddress.toString());
         display_start();
         fullIpAddressIndex = 0;
         for(int i = 0; i < 4; i ++) {
            for(int j = 0; j < 3; j ++) {
               logger.infof("%c\n", octets[i][j]);
               if(octets[i][j] == '0')
                  display_zero();
               else
                  display_blink(octets[i][j], 500, 500);
               Thread.sleep(1000);
               fullIpAddressIndex ++;
            }
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
}
