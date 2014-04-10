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

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class AddressListener {
   private static int DATAGRAM_PORT = 8888;
   private static int SERVER_PORT = 8889;

   public static String PI_ADDRESS = "PI_ADDRESS";
   private static AtomicInteger piID = new AtomicInteger(0);
   private static ConcurrentHashMap<InetAddress, Integer> piAddressMap = new ConcurrentHashMap<>();
   static ExecutorService pool = Executors.newFixedThreadPool(4);

   public static void main(String[] args) throws Exception {
      final InetAddress address = InetAddress.getByName(args[0]);
      pool.submit(new Runnable() {
         @Override
         public void run() {
            runDatagramSocket();
         }
      });
      pool.submit(new Runnable() {
         @Override
         public void run() {
            runStandardSocket(address);
         }
      });
   }

   public static void runStandardSocket(final InetAddress address) {
      try {
         ServerSocket socket = new ServerSocket(SERVER_PORT, 10, address);
         while(true) {
            final Socket client = socket.accept();
            Runnable clientHandler = new Runnable() {
               @Override
               public void run() {
                  handleClient(client);
               }
            };
            pool.submit(clientHandler);
         }
      } catch(Exception e) {
         e.printStackTrace();
      }
   }

   public static void handleClient(final Socket client) {
      try {
         InetAddress piAddr = client.getInetAddress();
         InputStream is = client.getInputStream();
         DataInputStream dis = new DataInputStream(is);
         String message = dis.readUTF();
            if (message.startsWith(PI_ADDRESS)) {
               Integer id = piAddressMap.get(piAddr);
               String info = "Your new ID:";
               if (id == null) {
                  id = piID.incrementAndGet();
                  piAddressMap.put(piAddr, id);
               } else {
                  info = "Your existing ID:";
               }

               OutputStream os = client.getOutputStream();
               DataOutputStream dos = new DataOutputStream(os);
               dos.writeUTF(info);
               dos.writeInt(id);
               dos.flush();
               client.close();
               System.out.printf(">>>Sent packet to: %s\n", piAddr.getHostAddress());
            }
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   public static void runDatagramSocket() {
      try {
         DatagramSocket socket = new DatagramSocket(DATAGRAM_PORT, InetAddress.getByName("0.0.0.0"));
         socket.setBroadcast(true);

         byte[] recvBuf = new byte[15000];
         while (true) {
            System.out.println(">>>Ready to receive broadcast packets!");

            //Receive a packet
            DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
            socket.receive(packet);

            //Packet received
            System.out.printf(">>>Discovery packet received from: %s\n", packet.getAddress());
            String message = new String(packet.getData()).trim();
            System.out.printf(">>>Packet received; data: %s\n", message);

            //See if the packet holds the right command (message)
            if (message.startsWith(PI_ADDRESS)) {
               InetAddress piAddr = packet.getAddress();
               Integer id = piAddressMap.get(piAddr);
               String info = "Your new ID:";
               if (id == null) {
                  id = piID.incrementAndGet();
                  piAddressMap.put(piAddr, id);
               } else {
                  info = "Your existing ID:";
               }

               ByteArrayOutputStream baos = new ByteArrayOutputStream();
               DataOutputStream dos = new DataOutputStream(baos);
               dos.writeUTF(info);
               dos.writeInt(id);
               dos.close();
               byte[] sendData = baos.toByteArray();
               DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
               socket.send(sendPacket);
               System.out.printf(">>>Sent packet to: %s\n", piAddr.getHostAddress());
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

}
