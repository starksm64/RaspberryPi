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
   static final int LED_COUNT = 18;
   /**
    * The numbers of the LEDs in the PiGlow device for the 3 spiral arms
    */
   static byte[][] ARMS = {
      {6,  7,  8,  5,  4,  9},
      {17, 16, 15, 13, 11, 10},
      {0,  1,  2,  3, 14, 12},
   };
   /**
    * A ramp
    */
   static byte[] ramp = {0x01, 0x02,0x04,0x08,0x10,0x18,0x20,0x30,0x40,0x50,0x60,0x70,(byte)0x80,
      (byte)0x90,(byte)0xA0,(byte)0xC0,(byte)0xE0, (byte)0xf0};
   // command register addresses for the SN3218 IC used in PiGlow
   static int CMD_ENABLE_OUTPUT = 0x00;
   static int EN_ARM1_ADDR = 0x13;
   static int EN_ARM2_ADDR = 0x14;
   static int EN_ARM3_ADDR = 0x15;
   static int CMD_SET_PWM_VALUES = 0x01;
   static int CMD_UPDATE = 0x16;
   // Fixed i2c address of SN3218 ic
   static int i2c_addr = 0x54;
   static byte ff = (byte)0xFF;

   /**
    * Write the individual LED values as given
    * @param device - the PiGlow I2C device
    * @param values - the individual intensities of the 18 LEDs
    * @throws IOException
    */
   static void update_leds(I2CDevice device, byte[] values) throws IOException {
      device.write(CMD_SET_PWM_VALUES, values, 0, values.length);
      device.write(CMD_UPDATE, ff);
   }

   /**
    * Write the given intensity value to all LEDs
    * @param device - the PiGlow I2C device
    * @param intensity - the LED intensity value to set
    * @throws IOException
    */
   static void set_leds(I2CDevice device, byte intensity) throws IOException {
      byte[] values = new byte[LED_COUNT];
      Arrays.fill(values, intensity);
      device.write(CMD_SET_PWM_VALUES, values, 0, values.length);
      device.write(CMD_UPDATE, ff);
   }

   /**
    * Sets the leds of the given arm to the given intensity.
    * @param device - the PiGlow I2C device
    * @param arm - the arm used to index into the
    * @param intensity - the LED intensity value to set
    * @throws IOException
    */
   static void set_arm(I2CDevice device, int arm, byte intensity) throws IOException {
      byte[] values = new byte[LED_COUNT];
      Arrays.fill(values, (byte)0);
      for(int n = 0; n < ARMS[arm].length; n ++)
         values[ARMS[arm][n]] = intensity;
      device.write(CMD_SET_PWM_VALUES, values, 0, values.length);
      device.write(CMD_UPDATE, ff);
   }

   /**
    *
    * @param device
    * @param led
    * @param intensity
    * @throws IOException
    */
   static void set_led(I2CDevice device, int led, byte intensity) throws IOException {
      byte[] values = new byte[LED_COUNT];
      Arrays.fill(values, (byte)0);
      values[led] = intensity;
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
      // Clear the leds
      set_leds(device, (byte)0);

      // Apply the intensity ramp
      update_leds(device, ramp);

      for(short value = 0; value < 255; value += 2) {
         set_leds(device, (byte) value);
         System.out.printf("set_leds(%d)\n", value);
         Thread.sleep(100);
      }
      set_leds(device, (byte)0);
      Thread.sleep(1000);
      for(int arm = 0; arm < ARMS.length; arm ++) {
         for (short value = 0; value < 255; value += 2) {
            set_arm(device, arm, (byte) value);
            System.out.printf("set_arm(%d,%d)\n", arm, value);
            Thread.sleep(100);
         }
      }
      Thread.sleep(1000);

      // Clear the leds
      set_leds(device, (byte)0);

      //
      for(int arm = 0; arm < ARMS.length; arm ++) {
         for(int n = 0; n < ARMS[arm].length; n ++) {
            int led = ARMS[arm][n];
            System.out.printf("set_led(%d,0xff)\n", led);
            set_led(device, led, ff);
            Thread.sleep(500);
         }
      }
      // Clear the leds
      set_leds(device, (byte)0);
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
