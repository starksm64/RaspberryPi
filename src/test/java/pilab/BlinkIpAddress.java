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

/**
 * Based on perl script blink_ip.pl:
 * https://gist.github.com/chrismeyersfsu/2858824
 * Must be run as root.
 *
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class BlinkIpAddress {

   /**
    * Run the blink sequence on LED0
    * @param args args[0] = optional number of iterations to perform
    * @throws Exception
    */
   public static void main(String[] args) throws Exception {
      int iterations = -1;
      if (args.length > 0)
         iterations = Integer.parseInt(args[0]);
      org.jboss.devnation.pilab.BlinkIpAddress blinker = new org.jboss.devnation.pilab.BlinkIpAddress(iterations);
      blinker.start();
   }
}
