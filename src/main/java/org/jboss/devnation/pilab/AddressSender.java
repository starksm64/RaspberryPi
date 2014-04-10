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

import org.jboss.devnation.pilab.Utility.NetworkInfo;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class AddressSender {
   public static void main(String[] args) throws Exception {

      NetworkInfo networkInfo = Utility.determineIPAddress();
      if(args.length > 0) {
         InetAddress serverAddress = InetAddress.getByName(args[0]);
         int port = Integer.parseInt(args[1]);
         doSocket(serverAddress, networkInfo, port);
      } else {
         doBroadcast(networkInfo);
      }
   }

   /**
    * Send TCP given the server address
    * @param serverAddress
    */
   private static void doSocket(InetAddress serverAddress, NetworkInfo networkInfo, int port) {
      InetAddress localAddress = networkInfo.getInetAddress();
      try {
         Socket client = new Socket(serverAddress, port, localAddress, 0);
         OutputStream os = client.getOutputStream();
         DataOutputStream dos = new DataOutputStream(os);
         String msg = String.format("%s%s", AddressListener.PI_ADDRESS, localAddress);
         dos.writeUTF(msg);
         dos.flush();
         InputStream is = client.getInputStream();
         DataInputStream dis = new DataInputStream(is);
         String reply = dis.readUTF();
         int myID = dis.readInt();
         System.out.printf("%s, Client received id: %d", reply, myID);
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   /**
    * Broadcast UDP using the given network broadcast interface
    * @param networkInfo
    * @throws Exception
    */
   private static void doBroadcast(NetworkInfo networkInfo) throws Exception {
      MulticastSocket socket = new MulticastSocket(4446);
      socket.setNetworkInterface(networkInfo.iface);

      byte[] recvBuf = new byte[15000];
      DatagramSocket ds = new DatagramSocket();
      ds.setBroadcast(true);
      ds.setSoTimeout(5000);
      while (true) {
         // Notify the server using UDP broadcast
         //Open a random port to send the package

         String msg = String.format("%s:%s", AddressListener.PI_ADDRESS, networkInfo.getInetAddress());
         byte[] sendData = msg.getBytes();

         InetAddress broadcast = networkInfo.getBroadcastAddress();
         // Send the broadcast package!
         try {
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, 8888);
            ds.send(sendPacket);
         } catch (Exception e) {
            e.printStackTrace();
         }

         System.out.printf(">>> Request packet sent to: %s; Interface: %s\n", broadcast, networkInfo.getDisplayName());

         System.out.println(">>> Done looping over all network interfaces. Now waiting for a reply!");

         //Wait for a response
         DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
         try {
            ds.receive(receivePacket);
         } catch (SocketTimeoutException e) {
            System.out.printf("Timed out waiting for reply, resending...");
            continue;
         }

         //We have a response
         System.out.println(">>> Broadcast response from server: " + receivePacket.getAddress());

         //Check if the message is correct
         ByteArrayInputStream bais = new ByteArrayInputStream(receivePacket.getData());
         DataInputStream dis = new DataInputStream(bais);
         String message = dis.readUTF();
         int myID = dis.readInt();
         System.out.printf("%s, %d\n", message, myID);
         ds.close();
         System.exit(0);
      }
   }
}
