package org.jboss.devnation.pilab.web;/*
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

import org.jboss.devnation.pilab.web.view.SystemInfoView;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Top level backing bean for web application home page
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
@Named
@ApplicationScoped
public class PiApp {

   @Inject
   BlinkStatus blinkStatus;

   public SystemInfoView getSystemInfoView() {
      return new SystemInfoView();
   }

   public String getIpAddress() {
      return blinkStatus.getCurrentIPString();
   }

}
