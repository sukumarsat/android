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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import com.android.ddmlib.IDevice;
import com.android.sdklib.ISystemImage;
import com.android.sdklib.internal.avd.AvdInfo;
import java.io.File;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public final class AsyncDevicesGetterTest {
  private VirtualDevice myVirtualDevice;

  private IDevice myConnectedDevice;
  private Collection<IDevice> myConnectedDevices;

  private Clock myClock;
  private ConnectionTimeService myService;

  @Before
  public void newVirtualDevice() {
    myVirtualDevice = VirtualDevice.newDisconnectedVirtualDevice(new AvdInfo(
      "Pixel_2_XL_API_27",
      new File("/usr/local/google/home/juancnuno/.android/avd/Pixel_2_XL_API_27.ini"),
      "/usr/local/google/home/juancnuno/.android/avd/Pixel_2_XL_API_27.avd",
      Mockito.mock(ISystemImage.class),
      null));
  }

  @Before
  public void newConnectedDevices() {
    myConnectedDevice = Mockito.mock(IDevice.class);

    myConnectedDevices = new ArrayList<>(1);
    myConnectedDevices.add(myConnectedDevice);
  }

  @Before
  public void newService() {
    myClock = Mockito.mock(Clock.class);
    myService = new ConnectionTimeService(myClock);
  }

  @Test
  public void newVirtualDeviceIfItsConnectedAvdNamesAreEqual() {
    Mockito.when(myConnectedDevice.getAvdName()).thenReturn("Pixel_2_XL_API_27");
    Mockito.when(myClock.instant()).thenReturn(Instant.parse("2018-11-28T01:15:27.000Z"));

    Object device = AsyncDevicesGetter.newVirtualDeviceIfItsConnected(myVirtualDevice, myConnectedDevices, myService);
    assertEquals(VirtualDevice.newConnectedVirtualDevice(myVirtualDevice, myService, myConnectedDevice), device);

    assertEquals(Collections.emptyList(), myConnectedDevices);
  }

  @Test
  public void newVirtualDeviceIfItsConnected() {
    Mockito.when(myConnectedDevice.getAvdName()).thenReturn("Pixel_2_XL_API_28");

    assertSame(myVirtualDevice, AsyncDevicesGetter.newVirtualDeviceIfItsConnected(myVirtualDevice, myConnectedDevices, myService));
    assertEquals(Collections.singletonList(myConnectedDevice), myConnectedDevices);
  }
}