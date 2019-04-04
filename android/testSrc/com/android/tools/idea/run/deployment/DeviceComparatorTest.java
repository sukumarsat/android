/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.tools.idea.run.deployment;

import static org.junit.Assert.assertTrue;

import com.android.tools.idea.run.AndroidDevice;
import org.junit.Test;
import org.mockito.Mockito;

public final class DeviceComparatorTest {
  @Test
  public void compareConnected() {
    Device device1 = new VirtualDevice.Builder()
      .setName("Pixel 3 API 28")
      .setKey("Pixel_3_API_28")
      .setAndroidDevice(Mockito.mock(AndroidDevice.class))
      .setConnected(true)
      .build();

    Device device2 = new VirtualDevice.Builder()
      .setName("Pixel 2 XL API 28")
      .setKey("Pixel_2_XL_API_28")
      .setAndroidDevice(Mockito.mock(AndroidDevice.class))
      .build();

    assertTrue(new DeviceComparator().compare(device1, device2) < 0);
  }

  @Test
  public void compareValid() {
    Device device1 = new VirtualDevice.Builder()
      .setName("Pixel 3 API 28")
      .setKey("Pixel_3_API_28")
      .setAndroidDevice(Mockito.mock(AndroidDevice.class))
      .build();

    Device device2 = new VirtualDevice.Builder()
      .setName("Pixel 2 XL API 28")
      .setValid(false)
      .setKey("Pixel_2_XL_API_28")
      .setAndroidDevice(Mockito.mock(AndroidDevice.class))
      .build();

    assertTrue(new DeviceComparator().compare(device1, device2) < 0);
  }

  @Test
  public void compareType() {
    Device device1 = new VirtualDevice.Builder()
      .setName("Pixel 3 API 28")
      .setKey("Pixel_3_API_28")
      .setAndroidDevice(Mockito.mock(AndroidDevice.class))
      .setConnected(true)
      .build();

    Device device2 = new PhysicalDevice.Builder()
      .setName("LGE Nexus 5X")
      .setKey("00fff9d2279fa601")
      .setAndroidDevice(Mockito.mock(AndroidDevice.class))
      .build();

    assertTrue(new DeviceComparator().compare(device1, device2) < 0);
  }

  @Test
  public void compareName() {
    Device device1 = new VirtualDevice.Builder()
      .setName("Pixel 2 XL API 28")
      .setKey("Pixel_2_XL_API_28")
      .setAndroidDevice(Mockito.mock(AndroidDevice.class))
      .build();

    Device device2 = new VirtualDevice.Builder()
      .setName("Pixel 3 API 28")
      .setKey("Pixel_3_API_28")
      .setAndroidDevice(Mockito.mock(AndroidDevice.class))
      .build();

    assertTrue(new DeviceComparator().compare(device1, device2) < 0);
  }
}
