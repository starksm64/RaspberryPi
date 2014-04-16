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
import org.jboss.logging.Logger;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Broadcasts the IP address on the current interface
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class AddressSender {
   private static Logger logger = Logger.getLogger(AddressSender.class);

   private volatile boolean running;
   private NetworkInfo networkInfo;
   private InetSocketAddress serverAddress;

   public static void main(String[] args) throws Exception {

      AddressSender sender = new AddressSender();
      sender.initNetwork();
      sender.running = true;
      if(args.length > 0) {
         int port = Integer.parseInt(args[1]);
         sender.initNetwork(args[0], port);
         sender.doSocket();
      } else {
         sender.doBroadcast();
      }
   }

   public void initNetwork(String address, int port) throws UnknownHostException {
      InetAddress inetAddress = InetAddress.getByName(address);
      serverAddress = new InetSocketAddress(inetAddress, port);

   }
   public void initNetwork() throws SocketException {
      networkInfo = Utility.determineIPAddress();
   }
   public void initNetwork(NetworkInfo networkInfo) {
      this.networkInfo = networkInfo;
   }
   public void run() {
      running = true;
      try {
         if(serverAddress != null)
            doSocket();
         else
            doBroadcast();
      } catch (Throwable e) {
         logger.warn("Failed to run address send", e);
      }
   }
   public void stop() {
      running = false;
   }
   /**
    * Send TCP msg using given the server address
    */
   private void doSocket() throws Exception {
      logger.info("Begin doSocket()");
      InetAddress localAddress = networkInfo.getInetAddress();
      Socket client = new Socket(serverAddress.getAddress(), serverAddress.getPort(), localAddress, 0);
      OutputStream os = client.getOutputStream();
      DataOutputStream dos = new DataOutputStream(os);
      String msg = String.format("%s%s", AddressListener.PI_ADDRESS, localAddress);
      dos.writeUTF(msg);
      dos.flush();
      InputStream is = client.getInputStream();
      DataInputStream dis = new DataInputStream(is);
      String reply = dis.readUTF();
      int myID = dis.readInt();
      logger.infof("End doSocket(), %s, Client received id: %d", reply, myID);
   }

   /**
    * Broadcast UDP msg using the given network broadcast interface
    * @throws Exception
    */
   private void doBroadcast() throws Exception {
      logger.infof("Begin doBroadcast(%s)", running);
      byte[] recvBuf = new byte[15000];
      DatagramSocket ds = new DatagramSocket();
      ds.setBroadcast(true);
      ds.setSoTimeout(5000);
      while (running) {
         String msg = String.format("%s:%s[%s]", AddressListener.PI_ADDRESS, networkInfo.getInetAddress(), networkInfo.getMAC());
         byte[] sendData = msg.getBytes();

         InetAddress broadcast = networkInfo.getBroadcastAddress();
         logger.infof(">>> Sending: %s; to: %s\n", msg, broadcast);
         try {
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, AddressListener.DATAGRAM_PORT);
            ds.send(sendPacket);
         } catch (Exception e) {
            e.printStackTrace();
         }

         logger.infof(">>> Request packet sent to: %s; Interface: %s\n", broadcast, networkInfo.getDisplayName());


         //Wait for a response
         DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
         try {
            ds.receive(receivePacket);
         } catch (SocketTimeoutException e) {
            continue;
         }

         logger.info(">>> Broadcast response from server: " + receivePacket.getAddress());
         ByteArrayInputStream bais = new ByteArrayInputStream(receivePacket.getData());
         DataInputStream dis = new DataInputStream(bais);
         String message = dis.readUTF();
         int myID = dis.readInt();
         logger.infof("%s, %d\n", message, myID);
         Thread.sleep(10000);
      }
      ds.close();
      logger.info("End doBroadcast()");
   }
}
