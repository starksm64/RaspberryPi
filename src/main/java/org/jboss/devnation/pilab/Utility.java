package org.jboss.devnation.pilab;/*
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

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class Utility {
   public static class NetworkInfo {
      NetworkInterface iface;
      InterfaceAddress ifaceAddress;
      InetAddress inetAddress;

      public NetworkInfo(NetworkInterface iface, InterfaceAddress ifaceAddress) {
         this.iface = iface;
         this.ifaceAddress = ifaceAddress;
      }

      public InetAddress getInetAddress() {
         return ifaceAddress.getAddress();
      }
      public InetAddress getBroadcastAddress() {
         return ifaceAddress.getBroadcast();
      }
      public String getDisplayName() {
         return iface.getDisplayName();
      }
      public String getMAC() throws SocketException {
         byte[] mac = iface.getHardwareAddress();
         StringBuilder macaddr = new StringBuilder();
         for(int n = 0; n < mac.length; n ++) {
            String x = String.format("%x", mac[n]);
            macaddr.append(x);
            macaddr.append(':');
         }
         macaddr.setLength(macaddr.length()-1);
         return macaddr.toString();
      }
   }

   public static NetworkInfo determineIPAddress() throws SocketException {
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
         throw new SocketException("Failed to find an nonloopback interface that is up");
      }
      List<InterfaceAddress> ifaceAddresses = nonloopback.getInterfaceAddresses();
      InterfaceAddress ifaceAddress = null;
      for(InterfaceAddress ia : ifaceAddresses) {
         InetAddress inetAddress = ia.getAddress();
         if(inetAddress instanceof Inet4Address) {
            ifaceAddress = ia;
            break;
         }
      }
      // Print out interface name, ip address and mac address
      NetworkInfo netInfo = new NetworkInfo(nonloopback, ifaceAddress);
      System.out.printf("Selected %s, %s/%s[%s]\n", netInfo.getDisplayName(), netInfo.getInetAddress(),
         netInfo.getBroadcastAddress(), netInfo.getMAC());
      return netInfo;
   }
}
