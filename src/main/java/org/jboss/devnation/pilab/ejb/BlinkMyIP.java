package org.jboss.devnation.pilab.ejb;
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

import org.jboss.devnation.pilab.BlinkIpAddress;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.SocketException;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
@Singleton
@Startup
public class BlinkMyIP {
   private static Logger logger = Logger.getLogger(BlinkMyIP.class);

   /* Old in process BlinkIpAddress usage
   @Resource(name="concurrent/LongRunningTasksExecutor")
   private ManagedExecutorService executorService;
   private Future blinkTask;
   */
   /** File system based usage */
   private BlinkIpAddress blinkIpAddress;

   @PostConstruct
   private void init() throws SocketException {
      logger.info("Initializing IP Address blinking");
      blinkIpAddress = new BlinkIpAddress();
      blinkIpAddress.init();
   }

   @PreDestroy
   private void destroy() {
   }

   public String getMac() {
      return blinkIpAddress.getMac();
   }

   public char[] getFullIpAddress() {
      return blinkIpAddress.getFullIpAddress();
   }

   public int getFullIpAddressIndex() throws IOException {
      RandomAccessFile indexFile = new RandomAccessFile(BlinkIpAddress.getIndex(), "r");
      int index = indexFile.readInt();
      return index;
   }
   public Exception getErrorState() {
      return blinkIpAddress.getErrorState();
   }
   public boolean isStopped() {
      return blinkIpAddress.isStopped();
   }

   public void start() throws IOException {
      logger.info("Starting IP Address blinking");
      File start = new File(BlinkIpAddress.getStart());
      start.createNewFile();
   }
   public void stop() throws IOException {
      logger.info("Stopping IP Address blinking");
      File stop = new File(BlinkIpAddress.getStop());
      stop.createNewFile();
   }
}
