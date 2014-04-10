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
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.concurrent.ManagedExecutorService;
import java.util.concurrent.Future;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
@Singleton
@Startup
public class BlinkMyIP {
   private static Logger logger = Logger.getLogger(BlinkMyIP.class);

   @Resource(name="concurrent/LongRunningTasksExecutor")
   private ManagedExecutorService executorService;
   private Future blinkTask;
   private BlinkIpAddress blinkIpAddress;

   @PostConstruct
   private void init() {
      logger.info("Initializing IP Address blinking");
      blinkIpAddress = new BlinkIpAddress();
      blinkTask = executorService.submit(blinkIpAddress);
   }
   @PreDestroy
   private void destroy() {
      stop();
   }

   public String getMac() {
      return blinkIpAddress.getMac();
   }

   public char[] getFullIpAddress() {
      return blinkIpAddress.getFullIpAddress();
   }

   public int getFullIpAddressIndex() {
      return blinkIpAddress.getFullIpAddressIndex();
   }
   public Exception getErrorState() {
      return blinkIpAddress.getErrorState();
   }
   public boolean isStopped() {
      return blinkIpAddress.isStopped();
   }

   public void start() {
      logger.info("Starting IP Address blinking");
      if(blinkTask != null) {
         stop();
      }
      blinkTask = executorService.submit(blinkIpAddress);
   }
   public void stop() {
      if(blinkTask != null) {
         logger.info("Stopping IP Address blinking");
         blinkIpAddress.stop();
         blinkTask.cancel(true);
         blinkTask = null;
      }
   }
}
