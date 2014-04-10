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

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * A simple example of controlling the boards ACT led intensity using the file system interface
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class ActLedIntensityExample {
   /** Path to file system control of LED0 / ACT */
   private static final String LED0 = "/sys/class/leds/led0/brightness";

   /**
    * Simply turn the ACT LED on/off based on the command line argument.
    * @param args must specify either on or off
    */
   public static void main(String[] args) throws IOException {
      if(args.length == 0) {
         System.out.printf("Usage: ActLedIntensityExample on|off");
      }
      boolean on = args[0].equalsIgnoreCase("on");
      FileOutputStream led0 = new FileOutputStream(LED0);
      if(on)
         led0.write(Character.forDigit(1, 10));
      else
         led0.write(Character.forDigit(0, 10));
      led0.close();
   }
}
