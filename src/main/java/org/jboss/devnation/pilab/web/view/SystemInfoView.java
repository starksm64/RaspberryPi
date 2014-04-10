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

import com.pi4j.system.SystemInfo;

import javax.inject.Named;
import java.io.IOException;
import java.text.ParseException;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
@Named
public class SystemInfoView {
   private boolean enabled;

   public boolean isEnabled() {
      return enabled;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   public String getProcessor() throws IOException, InterruptedException {
      return SystemInfo.getProcessor();
   }

   public boolean getCodecMPG2Enabled() throws IOException, InterruptedException {
      return SystemInfo.getCodecMPG2Enabled();
   }

   public String getCpuPart() throws IOException, InterruptedException {
      return SystemInfo.getCpuPart();
   }

   public long getMemoryBuffers() throws IOException, InterruptedException {
      return SystemInfo.getMemoryBuffers();
   }

   public boolean isHardFloatAbi() {
      return SystemInfo.isHardFloatAbi();
   }

   public long getClockFrequencyHDMI() throws IOException, InterruptedException {
      return SystemInfo.getClockFrequencyHDMI();
   }

   public boolean getCodecWVC1Enabled() throws IOException, InterruptedException {
      return SystemInfo.getCodecWVC1Enabled();
   }

   public long getClockFrequencyISP() throws IOException, InterruptedException {
      return SystemInfo.getClockFrequencyISP();
   }

   public float getCpuVoltage() throws IOException, InterruptedException, NumberFormatException {
      return SystemInfo.getCpuVoltage();
   }

   public String getOsArch() {
      return SystemInfo.getOsArch();
   }

   public long getMemoryUsed() throws IOException, InterruptedException {
      return SystemInfo.getMemoryUsed();
   }

   public float getCpuTemperature() throws IOException, InterruptedException, NumberFormatException {
      return SystemInfo.getCpuTemperature();
   }

   public String getHardware() throws IOException, InterruptedException {
      return SystemInfo.getHardware();
   }

   public String getJavaVersion() {
      return SystemInfo.getJavaVersion();
   }

   public float getMemoryVoltageSDRam_I() throws IOException, InterruptedException, NumberFormatException {
      return SystemInfo.getMemoryVoltageSDRam_I();
   }

   public long getClockFrequencyPixel() throws IOException, InterruptedException {
      return SystemInfo.getClockFrequencyPixel();
   }

   public long getClockFrequencyCore() throws IOException, InterruptedException {
      return SystemInfo.getClockFrequencyCore();
   }

   public String getOsName() {
      return SystemInfo.getOsName();
   }

   public String getJavaRuntime() {
      return SystemInfo.getJavaRuntime();
   }

   public String getCpuRevision() throws IOException, InterruptedException {
      return SystemInfo.getCpuRevision();
   }

   public String getCpuArchitecture() throws IOException, InterruptedException {
      return SystemInfo.getCpuArchitecture();
   }

   public long getMemoryTotal() throws IOException, InterruptedException {
      return SystemInfo.getMemoryTotal();
   }

   public float getMemoryVoltageSDRam_P() throws IOException, InterruptedException, NumberFormatException {
      return SystemInfo.getMemoryVoltageSDRam_P();
   }

   public String getBoardType() throws IOException, InterruptedException {
      return SystemInfo.getBoardType().name();
   }

   public String getCpuImplementer() throws IOException, InterruptedException {
      return SystemInfo.getCpuImplementer();
   }

   public String getJavaVendorUrl() {
      return SystemInfo.getJavaVendorUrl();
   }

   public String getJavaVirtualMachine() {
      return SystemInfo.getJavaVirtualMachine();
   }

   public long getMemoryFree() throws IOException, InterruptedException {
      return SystemInfo.getMemoryFree();
   }

   public float getMemoryVoltageSDRam_C() throws IOException, InterruptedException, NumberFormatException {
      return SystemInfo.getMemoryVoltageSDRam_C();
   }

   public long getMemoryCached() throws IOException, InterruptedException {
      return SystemInfo.getMemoryCached();
   }

   public String getCpuVariant() throws IOException, InterruptedException {
      return SystemInfo.getCpuVariant();
   }

   public long getClockFrequencyEMMC() throws IOException, InterruptedException {
      return SystemInfo.getClockFrequencyEMMC();
   }

   public String getOsFirmwareBuild() throws IOException, InterruptedException {
      return SystemInfo.getOsFirmwareBuild();
   }

   public String getBogoMIPS() throws IOException, InterruptedException {
      return SystemInfo.getBogoMIPS();
   }

   public long getClockFrequencyDPI() throws IOException, InterruptedException {
      return SystemInfo.getClockFrequencyDPI();
   }

   public String getSerial() throws IOException, InterruptedException {
      return SystemInfo.getSerial();
   }

   public String getJavaVendor() {
      return SystemInfo.getJavaVendor();
   }

   public long getClockFrequencyPWM() throws IOException, InterruptedException {
      return SystemInfo.getClockFrequencyPWM();
   }

   public long getClockFrequencyUART() throws IOException, InterruptedException {
      return SystemInfo.getClockFrequencyUART();
   }

   public boolean getCodecH264Enabled() throws IOException, InterruptedException {
      return SystemInfo.getCodecH264Enabled();
   }

   public String getRevision() throws IOException, InterruptedException {
      return SystemInfo.getRevision();
   }

   public long getClockFrequencyVEC() throws IOException, InterruptedException {
      return SystemInfo.getClockFrequencyVEC();
   }

   public String[] getCpuFeatures() throws IOException, InterruptedException {
      return SystemInfo.getCpuFeatures();
   }

   public String getOsVersion() {
      return SystemInfo.getOsVersion();
   }

   public long getClockFrequencyH264() throws IOException, InterruptedException {
      return SystemInfo.getClockFrequencyH264();
   }

   public long getMemoryShared() throws IOException, InterruptedException {
      return SystemInfo.getMemoryShared();
   }

   public String getOsFirmwareDate() throws IOException, InterruptedException, ParseException {
      return SystemInfo.getOsFirmwareDate();
   }

   public long getClockFrequencyV3D() throws IOException, InterruptedException {
      return SystemInfo.getClockFrequencyV3D();
   }

   public long getClockFrequencyArm() throws IOException, InterruptedException {
      return SystemInfo.getClockFrequencyArm();
   }
}

