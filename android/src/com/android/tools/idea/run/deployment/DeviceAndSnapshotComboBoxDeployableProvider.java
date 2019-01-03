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

import com.android.ddmlib.Client;
import com.android.ddmlib.IDevice;
import com.android.sdklib.AndroidVersion;
import com.android.sdklib.internal.avd.AvdInfo;
import com.android.tools.idea.run.ApkProvisionException;
import com.android.tools.idea.run.ApplicationIdProvider;
import com.android.tools.idea.run.deployable.Deployable;
import com.android.tools.idea.run.deployable.DeployableProvider;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.project.Project;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DeviceAndSnapshotComboBoxDeployableProvider implements DeployableProvider {
  @NotNull private final Project myProject;
  @NotNull private final ApplicationIdProvider myApplicationIdProvider;

  public DeviceAndSnapshotComboBoxDeployableProvider(@NotNull Project project, @NotNull ApplicationIdProvider applicationIdProvider) {
    myApplicationIdProvider = applicationIdProvider;
    myProject = project;
  }

  @Override
  public boolean isDependentOnUserInput() {
    return false;
  }

  @Nullable
  @Override
  public Deployable getDeployable() throws ApkProvisionException {
    ActionManager manager = ActionManager.getInstance();
    Device device = ((DeviceAndSnapshotComboBoxAction)manager.getAction("DeviceAndSnapshotComboBox")).getSelectedDevice(myProject);

    if (device == null) {
      return null;
    }

    if (device instanceof VirtualDevice) {
      return new VirtualDeployable((VirtualDevice)device, myApplicationIdProvider.getPackageName());
    }
    else if (device instanceof PhysicalDevice) {
      return new PhysicalDeployable((PhysicalDevice)device, myApplicationIdProvider.getPackageName());
    }
    else {
      throw new AssertionError("Unknown device type: " + device.getClass().getCanonicalName());
    }
  }

  private static class VirtualDeployable implements Deployable {
    @NotNull private final VirtualDevice myDevice;
    @NotNull private final String myPackageName;

    private VirtualDeployable(@NotNull VirtualDevice virtualDevice, @NotNull String packageName) {
      myDevice = virtualDevice;
      myPackageName = packageName;
    }

    @NotNull
    @Override
    public AndroidVersion getVersion() {
      AvdInfo info = myDevice.getAvdInfo();
      assert info != null;
      return info.getAndroidVersion();
    }

    @Override
    public boolean isApplicationRunningOnDeployable() {
      if (!myDevice.isConnected()) {
        return false;
      }
      IDevice device = myDevice.getDdmlibDevice();
      if (device == null || !device.isOnline()) {
        return false;
      }
      List<Client> clients = Deployable.searchClientsForPackage(device, myPackageName);
      return !clients.isEmpty();
    }
  }

  private static class PhysicalDeployable implements Deployable {
    @NotNull private final PhysicalDevice myPhysicalDevice;
    @NotNull private final String myPackageName;

    private PhysicalDeployable(@NotNull PhysicalDevice physicalDevice, @NotNull String packageName) {
      myPhysicalDevice = physicalDevice;
      myPackageName = packageName;
    }

    @NotNull
    @Override
    public AndroidVersion getVersion() {
      IDevice device = myPhysicalDevice.getDdmlibDevice();
      assert device != null;
      return device.getVersion();
    }

    @Override
    public boolean isApplicationRunningOnDeployable() {
      IDevice device = myPhysicalDevice.getDdmlibDevice();
      assert device != null;
      if (!device.isOnline()) {
        return false;
      }
      List<Client> clients = Deployable.searchClientsForPackage(device, myPackageName);
      return !clients.isEmpty();
    }
  }
}