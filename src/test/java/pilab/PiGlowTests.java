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

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import java.io.IOException;
import java.util.Arrays;

/**
 * A little driver program for the PiGlow led array device
 *
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class PiGlowTests {
   static byte[][] ARMS = {
      {6,  7,  8,  5,  4,  9},
      {17, 16, 15, 13, 11, 10},
      {0,  1,  2,  3, 14, 12},
   };
   // command register addresses for the SN3218 IC used in PiGlow
   static int CMD_ENABLE_OUTPUT = 0x00;
   static int EN_ARM1_ADDR = 0x13;
   static int EN_ARM2_ADDR = 0x14;
   static int EN_ARM3_ADDR = 0x15;
   static int CMD_SET_PWM_VALUES = 0x01;
   static int CMD_UPDATE = 0x16;
   static int i2c_addr = 0x54; // fixed i2c address of SN3218 ic
   static byte ff = (byte)0xFF;
   static byte[] ramp = {0x01, 0x02,0x04,0x08,0x10,0x18,0x20,0x30,0x40,0x50,0x60,0x70,(byte)0x80,
      (byte)0x90,(byte)0xA0,(byte)0xC0,(byte)0xE0,ff};
   static byte[] LEDS = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18};

   public void testAccess() throws Exception {
      I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);
      System.out.println("Connected to bus OK!");

      //get device itself
      I2CDevice device = bus.getDevice(0x54);
      System.out.println("Connected to device OK!");
  }

   static void update_leds(I2CDevice device, byte[] values) throws IOException {
      device.write(CMD_SET_PWM_VALUES, ramp, 0, ramp.length);
      device.write(CMD_UPDATE, ff);
   }
   static void set_leds(I2CDevice device, byte value) throws IOException {
      byte[] values = new byte[ramp.length];
      Arrays.fill(values, value);
      device.write(CMD_SET_PWM_VALUES, values, 0, values.length);
      device.write(CMD_UPDATE, ff);
   }
   static void set_arm(I2CDevice device, int arm, byte value) throws IOException {
      byte[] values = new byte[ramp.length];
      Arrays.fill(values, (byte)0);
      for(int n = 0; n < ARMS[arm].length; n ++)
         values[ARMS[arm][n]] = value;
      device.write(CMD_SET_PWM_VALUES, values, 0, values.length);
      device.write(CMD_UPDATE, ff);
   }

   public static void main(String[] args) throws Exception {
      I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);
      System.out.println("Connected to bus OK!");

      //get device itself
      I2CDevice device = bus.getDevice(i2c_addr);
      System.out.println("Connected to device OK!");
      device.write(CMD_ENABLE_OUTPUT, (byte)1);
      device.write(EN_ARM1_ADDR, ff);
      device.write(EN_ARM2_ADDR, ff);
      device.write(EN_ARM3_ADDR, ff);
      byte[] zero = new byte[18];
      Arrays.fill(zero, (byte) 0);
      // Clear the leds
      set_leds(device, (byte)0);

      // Apply the intensity ramp
      update_leds(device, ramp);

      for(byte value = 0; value < 127; value ++) {
         set_leds(device, value);
         System.out.printf("set_leds(%d)\n", value);
         Thread.sleep(100);
      }
      set_leds(device, (byte)0);
      for(int arm = 0; arm < ARMS.length; arm ++) {
         for (byte value = 0; value < 127; value++) {
            set_arm(device, arm, value);
            System.out.printf("set_arm(%d,%d)\n", arm, value);
            Thread.sleep(100);
         }
      }

      /*
      // Cycle through the values for 1 minunte
      byte[] values = new byte[ramp.length];
      System.arraycopy(ramp, 0, values, 0, ramp.length);
      for(int n = 0; n < ramp.length; n ++) {
         byte first = values[0];
         System.arraycopy(values, 1, values, 0, values.length-1);
         values[values.length-1] = first;
         update_leds(device, values);
         System.out.printf("Iter#%d(%s)\n", n, toString(values));
         Thread.sleep(1000);
      }
      */
   }

   private static String toString(byte[] values) {
      StringBuilder tmp = new StringBuilder();
      for(int n = 0; n < values.length; n ++) {
         tmp.append(values[n]);
         tmp.append(',');
      }
      tmp.setLength(tmp.length()-1);
      return tmp.toString();
   }
}
