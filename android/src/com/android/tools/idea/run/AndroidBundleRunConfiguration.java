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
package com.android.tools.idea.run;

import com.android.tools.idea.gradle.util.DynamicAppUtils;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.openapi.project.Project;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Run Configuration used for running Android Apps locally on a device/emulator via the "bundle" gradle tasks.
 */
public class AndroidBundleRunConfiguration extends AndroidAppRunConfigurationBase {
  public AndroidBundleRunConfiguration(Project project, ConfigurationFactory factory) {
    super(project, factory);
  }

  @NotNull
  @Override
  protected List<ValidationError> checkConfiguration(@NotNull AndroidFacet facet) {
    List<ValidationError> errors = new ArrayList<>();
    if (!DynamicAppUtils.supportsBundleTask(facet.getModule())) {
      ValidationError error = ValidationError.fatal("This configuration requires a newer version of the Android Gradle Plugin",
                                                    () -> DynamicAppUtils.promptUserForGradleUpdate(getProject()));
      errors.add(error);
    }
    errors.addAll(super.checkConfiguration(facet));
    return errors;
  }
}
