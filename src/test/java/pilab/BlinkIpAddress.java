package pilab;
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

import java.io.FileOutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * Based on perl script blick_ip.pl:
 * https://gist.github.com/chrismeyersfsu/2858824
 * Must be run as root.
 *
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class BlinkIpAddress {
   private static final String LED0 = "/sys/class/leds/led0/brightness";
   private static FileOutputStream outb;

   public static void main(String[] args) throws Exception {
      Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
      NetworkInterface nonloopback = null;
      // Choose the first non-loopback interface
      while(ifaces.hasMoreElements()) {
         NetworkInterface iface = ifaces.nextElement();
         System.out.printf("Checking iface: %s\n", iface.getDisplayName());
         if(iface.isUp() && iface.isLoopback() == false) {
            nonloopback = iface;
         }
      }

      if(nonloopback == null) {
         throw new IllegalStateException("Failed to find an nonloopback interface that is up");
      }
      byte[] mac = nonloopback.getHardwareAddress();
      System.out.printf("mac.length=%d\n", mac.length);
      StringBuilder macaddr = new StringBuilder();
      for(int n = 0; n < mac.length; n ++) {
         String x = String.format("%x", mac[n]);
         macaddr.append(x);
         macaddr.append(':');
      }
      macaddr.setLength(macaddr.length()-1);
      Enumeration<InetAddress> inetAddresses = nonloopback.getInetAddresses();
      InetAddress inetAddress = null;
      while(inetAddresses.hasMoreElements()) {
         inetAddress = inetAddresses.nextElement();
         if(inetAddress instanceof Inet4Address)
            break;
      }
      System.out.printf("Selected %s, %s[%s]\n", nonloopback.getDisplayName(), inetAddress, macaddr);

      outb = new FileOutputStream(LED0);
      int iterations = -1;
      if(args.length > 0)
         iterations = Integer.parseInt(args[0]);
      String iterStr = iterations < 0 ? "infinite" : ""+iterations;
      System.out.printf("Doing (%s) iterations of the link sequence for IPAddress(%s); -1==infinite\n", iterStr,
         inetAddress.getHostAddress());

      StringBuilder fullyPaddedAddress = new StringBuilder();
      byte[] baddr = inetAddress.getAddress();
      char[][] octets = {{'0', '0', '0'},{'0', '0', '0'}, {'0', '0', '0'}, {'0', '0', '0'}};
      for(int n = 0; n < baddr.length; n ++) {
         int octet = baddr[n] < 0 ? 256 + baddr[n] : baddr[n];
         System.out.printf("%d: %03d(%d)\n", n, octet, baddr[n]);
         String tmp = String.format("%03d", octet);
         octets[n][0] = tmp.charAt(0);
         octets[n][1] = tmp.charAt(1);
         octets[n][2] = tmp.charAt(2);
         fullyPaddedAddress.append(octets[n]);
         fullyPaddedAddress.append('.');
      }
      fullyPaddedAddress.setLength(fullyPaddedAddress.length()-1);

      // Begin flashing the address code
      int count = 0;
      while(count < iterations || iterations < 0) {
         System.out.printf("Starting %s display\n", fullyPaddedAddress.toString());
         display_start();
         for(int i = 0; i < 4; i ++) {
            for(int j = 0; j < 3; j ++) {
               System.out.printf("%c\n", octets[i][j]);
               if(octets[i][j] == '0')
                  display_zero();
               else
                  display_blink(octets[i][j], 500, 500);
               Thread.sleep(1000);
            }
         }
         System.out.printf("------------\n");
      }
   }

   /**
    * Flashes the led on/off 50 times sleeping 10ms between flashes
    * @throws Exception
    */
   static void display_start() throws Exception {
       for (int i = 0; i < 50; i ++) {
           Thread.sleep(100);
           outb.write(Character.forDigit(1, 10));
          Thread.sleep(100);
           outb.write(Character.forDigit(0, 10));
       }
       Thread.sleep(4000);
   }

   /**
    * Flashes the led on/off 10 times sleeping 10ms between flashes
    * @throws Exception
    */
   static void display_zero() throws Exception {
      for (int i = 0; i < 10; i ++) {
         Thread.sleep(100);
         outb.write(Character.forDigit(1, 10));
         Thread.sleep(100);
         outb.write(Character.forDigit(0, 10));
       }
   }

   /**
    * Flash the led on/off digit times with the leg on dwell ms and off delay ms
    * @param c
    * @param delay
    * @param dwell
    * @throws Exception
    */
   static void display_blink(char c, int delay, int dwell) throws Exception {
      int digit = Character.digit(c, 10);
      for (int i = 0; i < digit; i ++) {
         Thread.sleep(dwell);
         outb.write(Character.forDigit(1, 10));
         Thread.sleep(delay);
         outb.write(Character.forDigit(0, 10));
       }

   }
}
