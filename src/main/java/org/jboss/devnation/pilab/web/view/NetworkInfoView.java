package org.jboss.devnation.pilab.web.view;
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

import com.pi4j.system.NetworkInfo;

import javax.inject.Named;
import java.io.IOException;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
@Named
public class NetworkInfoView {

   public String getHostname() throws IOException, InterruptedException {
      return NetworkInfo.getHostname();
   }

   public String[] getIPAddresses() throws IOException, InterruptedException {
      return NetworkInfo.getIPAddresses();
   }

   public String getIPAddress() throws IOException, InterruptedException {
      return NetworkInfo.getIPAddress();
   }

   public String getFQDN() throws IOException, InterruptedException {
      return NetworkInfo.getFQDN();
   }

   public String[] getNameservers() throws IOException, InterruptedException {
      return NetworkInfo.getNameservers();
   }

   public String[] getFQDNs() throws IOException, InterruptedException {
      return NetworkInfo.getFQDNs();
   }
}
