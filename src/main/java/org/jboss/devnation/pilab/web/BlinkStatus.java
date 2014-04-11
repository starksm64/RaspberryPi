package org.jboss.devnation.pilab.web;
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

import org.jboss.devnation.pilab.ejb.BlinkMyIP;

import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.io.IOException;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
@Named
@ApplicationScoped
public class BlinkStatus {
   @EJB
   private BlinkMyIP blinkMyIP;
   private boolean enabled = true;

   public boolean isEnabled() {
      return enabled;
   }


   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   public boolean isStopped() {
      return blinkMyIP.isStopped();
   }
   public boolean hasError() {
      return blinkMyIP.getErrorState() != null;
   }
   public String getErrorMsg() {
      String msg = null;
      Exception exception = blinkMyIP.getErrorState();
      if(exception != null)
         msg = exception.getLocalizedMessage();
      return msg;
   }
   public void toggleRunState() throws IOException {
      if(isStopped())
         start();
      else
         stop();
   }
   public void start() throws IOException {
      blinkMyIP.start();
   }
   public void stop() throws IOException {
      blinkMyIP.stop();
   }

   public String getCurrentIPString() {
      char[] addr = blinkMyIP.getFullIpAddress();

      StringBuilder tmp = new StringBuilder();
      for(int n = 0; n < addr.length; n ++) {
         tmp.append(addr[n]);
         if((n+1)%3 == 0)
            tmp.append('.');
      }
      tmp.setLength(tmp.length()-1);
      return tmp.toString();
   }
   public String getCurrentDigit() throws IOException {
      char[] addr = blinkMyIP.getFullIpAddress();
      int index = blinkMyIP.getFullIpAddressIndex();
      StringBuilder tmp = new StringBuilder();
      for(int n = 0; n < addr.length; n ++) {
         if(n == index)
            tmp.append('^');
         else
            tmp.append('-');
         if((n+1)%3 == 0)
            tmp.append('.');
      }
      tmp.setLength(tmp.length()-1);
      return tmp.toString();
   }
}
