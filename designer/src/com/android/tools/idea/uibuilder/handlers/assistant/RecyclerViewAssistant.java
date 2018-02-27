/*
 * Copyright (C) 2017 The Android Open Source Project
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
package com.android.tools.idea.uibuilder.handlers.assistant;

import com.android.ide.common.resources.ResourceItem;
import com.android.resources.ResourceFolderType;
import com.android.resources.ResourceType;
import com.android.tools.adtui.HorizontalSpinner;
import com.android.tools.idea.common.model.NlComponent;
import com.android.tools.idea.res.AppResourceRepository;
import com.android.tools.idea.uibuilder.property.assistant.ComponentAssistant;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.components.JBList;
import com.intellij.util.ui.UIUtil;
import kotlin.Unit;
import org.intellij.lang.annotations.Language;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.facet.ResourceFolderManager;
import org.jetbrains.android.util.AndroidResourceUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

import static com.android.SdkConstants.*;

public class RecyclerViewAssistant extends JPanel implements ComponentAssistant.PanelFactory {
  private static Logger LOG = Logger.getInstance(RecyclerViewAssistant.class);

  @Language("XML")
  private static String EMAIL_TEMPLATE =
    "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
    "<android.support.constraint.ConstraintLayout\n" +
    "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
    "    xmlns:app=\"http://schemas.android.com/apk/res-auto\"\n" +
    "    xmlns:tools=\"http://schemas.android.com/tools\"\n" +
    "    android:layout_width=\"match_parent\"\n" +
    "    android:layout_height=\"wrap_content\">\n" +
    "\n" +
    "    <ImageView\n" +
    "        android:id=\"@+id/imageView2\"\n" +
    "        android:layout_width=\"50dp\"\n" +
    "        android:layout_height=\"50dp\"\n" +
    "        tools:src=\"@tools:sample/avatars\"\n" +
    "        app:layout_constraintStart_toStartOf=\"parent\"\n" +
    "        android:layout_marginStart=\"8dp\"\n" +
    "        app:layout_constraintTop_toTopOf=\"parent\"\n" +
    "        android:layout_marginTop=\"8dp\" />\n" +
    "\n" +
    "    <TextView\n" +
    "        android:id=\"@+id/textView\"\n" +
    "        android:layout_width=\"wrap_content\"\n" +
    "        android:layout_height=\"wrap_content\"\n" +
    "        tools:text=\"@tools:sample/full_names\"\n" +
    "        android:textSize=\"20sp\"\n" +
    "        android:textColor=\"@android:color/black\"\n" +
    "        app:layout_constraintTop_toTopOf=\"@+id/imageView2\"\n" +
    "        app:layout_constraintStart_toEndOf=\"@+id/imageView2\"\n" +
    "        android:layout_marginStart=\"8dp\"\n" +
    "        android:layout_marginBottom=\"8dp\"\n" +
    "        app:layout_constraintBottom_toTopOf=\"@+id/textView2\" />\n" +
    "\n" +
    "    <TextView\n" +
    "        android:id=\"@+id/textView2\"\n" +
    "        android:layout_width=\"285dp\"\n" +
    "        android:layout_height=\"20dp\"\n" +
    "        tools:text=\"@tools:sample/lorem[4:10]\"\n" +
    "        app:layout_constraintBottom_toBottomOf=\"@+id/imageView2\"\n" +
    "        app:layout_constraintStart_toEndOf=\"@+id/imageView2\"\n" +
    "        android:layout_marginStart=\"8dp\"\n" +
    "        app:layout_constraintEnd_toEndOf=\"parent\"\n" +
    "        android:layout_marginEnd=\"8dp\"\n" +
    "        app:layout_constraintHorizontal_bias=\"0.050\" />\n" +
    "\n" +
    "    <TextView\n" +
    "        android:id=\"@+id/textView3\"\n" +
    "        android:layout_width=\"wrap_content\"\n" +
    "        android:layout_height=\"wrap_content\"\n" +
    "        tools:text=\"@tools:sample/date/hhmm\"\n" +
    "        app:layout_constraintTop_toTopOf=\"@+id/imageView2\"\n" +
    "        app:layout_constraintEnd_toEndOf=\"parent\"\n" +
    "        android:layout_marginEnd=\"8dp\" />\n" +
    "</android.support.constraint.ConstraintLayout>";

  @Language("XML")
  private static String ONE_LINE_TEMPLATE =
    "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
    "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
    "    xmlns:tools=\"http://schemas.android.com/tools\"\n" +
    "    android:orientation=\"vertical\"\n" +
    "    android:padding=\"8dp\"\n" +
    "    android:layout_width=\"match_parent\"\n" +
    "    android:layout_height=\"wrap_content\">\n" +
    "    <TextView\n" +
    "        android:layout_width=\"wrap_content\"\n" +
    "        android:layout_height=\"wrap_content\"\n" +
    "        tools:text=\"@tools:sample/lorem\" />\n" +
    "</LinearLayout>";

  @Language("XML")
  private static String TWO_LINES_TEMPLATE =
    "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
    "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
    "    xmlns:tools=\"http://schemas.android.com/tools\"\n" +
    "    android:orientation=\"vertical\"\n" +
    "    android:padding=\"8dp\"\n" +
    "    android:layout_width=\"match_parent\"\n" +
    "    android:layout_height=\"wrap_content\">\n" +
    "    <TextView\n" +
    "        android:layout_width=\"wrap_content\"\n" +
    "        android:layout_height=\"wrap_content\"\n" +
    "        tools:text=\"@tools:sample/lorem\" />\n" +
    "    <TextView\n" +
    "        android:layout_width=\"wrap_content\"\n" +
    "        android:layout_height=\"wrap_content\"\n" +
    "        tools:text=\"@tools:sample/lorem\" />\n" +
    "</LinearLayout>";

  @Language("XML")
  private static String THREE_LINES_TEMPLATE =
    "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
    "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
    "    xmlns:tools=\"http://schemas.android.com/tools\"\n" +
    "    android:orientation=\"vertical\"\n" +
    "    android:padding=\"8dp\"\n" +
    "    android:layout_width=\"match_parent\"\n" +
    "    android:layout_height=\"wrap_content\">\n" +
    "    <TextView\n" +
    "        android:layout_width=\"wrap_content\"\n" +
    "        android:layout_height=\"wrap_content\"\n" +
    "        tools:text=\"@tools:sample/lorem\" />\n" +
    "    <TextView\n" +
    "        android:layout_width=\"wrap_content\"\n" +
    "        android:layout_height=\"wrap_content\"\n" +
    "        tools:text=\"@tools:sample/lorem\" />\n" +
    "    <TextView\n" +
    "        android:layout_width=\"wrap_content\"\n" +
    "        android:layout_height=\"wrap_content\"\n" +
    "        tools:text=\"@tools:sample/lorem\" />\n" +
    "</LinearLayout>";

  private static final ImmutableList<Template> TEMPLATES = ImmutableList.of(
    new Template("e-mail client template", EMAIL_TEMPLATE),
    new Template("One line template", ONE_LINE_TEMPLATE),
    new Template("Two lines template", TWO_LINES_TEMPLATE),
    new Template("Three lines template", THREE_LINES_TEMPLATE));

  private final NlComponent component;
  private final String originalListItemValue;
  @Nullable private PsiFile createdFile;

  public RecyclerViewAssistant(@NotNull ComponentAssistant.Context context) {
    super(new BorderLayout());

    component = context.getComponent();
    AndroidFacet facet = component.getModel().getFacet();
    VirtualFile resourceDir = ResourceFolderManager.getInstance(facet).getPrimaryFolder();
    assert resourceDir != null;
    Project project = facet.getModule().getProject();
    String resourceName = getTemplateName(facet, "recycler_view");

    HorizontalSpinner<Template> spinner = HorizontalSpinner.forModel(
      JBList.createDefaultListModel(TEMPLATES.toArray(new Template[0])));

    spinner.addListSelectionListener(event -> {
      if (event.getValueIsAdjusting()) {
        return;
      }
      Template template = spinner.getModel().getElementAt(spinner.getSelectedIndex());
      createdFile = setTemplate(project, component, resourceName, template.myTemplate);
    });
    add(spinner, BorderLayout.NORTH);

    setBackground(UIUtil.getListBackground());
    originalListItemValue = component.getAttribute(TOOLS_URI, ATTR_LISTITEM);

    context.setOnClose(this::onClosed);
  }

  @NotNull
  private static String getTemplateName(@NotNull AndroidFacet facet, @NotNull String templateRootName) {
    AppResourceRepository appResourceRepository = AppResourceRepository.getOrCreateInstance(facet);
    String resourceNameRoot = AndroidResourceUtil.getValidResourceFileName(templateRootName);

    String resourceName;
    List<ResourceItem> items;
    int index = 0;
    do {
      resourceName = resourceNameRoot + (index < 1 ? "" : "_" + index);
      items = appResourceRepository.getResourceItem(ResourceType.LAYOUT, resourceName);
      index++;
    } while (items != null && !items.isEmpty());
    return resourceName;
  }

  @Nullable
  private static PsiFile setTemplate(@NotNull Project project,
                                     @NotNull NlComponent component,
                                     @NotNull String resourceName,
                                     @NotNull String content) {
    AndroidFacet facet = component.getModel().getFacet();
    VirtualFile resourceDir = ResourceFolderManager.getInstance(facet).getPrimaryFolder();
    assert resourceDir != null;

    return WriteCommandAction.runWriteCommandAction(project, (Computable<PsiFile>)() -> {
      List<VirtualFile> files = AndroidResourceUtil.findOrCreateStateListFiles(
        project, resourceDir, ResourceFolderType.LAYOUT, ResourceType.LAYOUT, resourceName, Collections.singletonList(FD_RES_LAYOUT));
      if (files == null || files.isEmpty()) {
        return null;
      }

      VirtualFile file = files.get(0);
      CommandProcessor.getInstance().addAffectedFiles(project, file);
      try {
        try (OutputStream stream = file.getOutputStream(null)) {
          stream.write(content.getBytes(Charsets.UTF_8));
        }
      }
      catch (IOException e) {
        LOG.debug(e);
      }
      component.setAttribute(TOOLS_URI, ATTR_LISTITEM, LAYOUT_RESOURCE_PREFIX + resourceName);
      CommandProcessor.getInstance().addAffectedFiles(project, component.getTag().getContainingFile().getVirtualFile());

      return PsiManager.getInstance(project).findFile(file);
    });
  }

  /**
   * Method called if the user has closed the popup
   */
  @Nullable
  private Unit onClosed(Boolean cancelled) {
    if (createdFile == null || !cancelled) {
      // The user didn't create a file, nothing to undo
      return null;
    }

    AndroidFacet facet = component.getModel().getFacet();
    Project project = facet.getModule().getProject();
    // onClosed is invoked when the dialog is closed so we run the clean-up it later when the dialog has effectively closed
    ApplicationManager.getApplication().invokeLater(() -> WriteCommandAction.runWriteCommandAction(project, () -> {
      createdFile.delete();
      component.setAttribute(TOOLS_URI, ATTR_LISTITEM, originalListItemValue);
      CommandProcessor.getInstance().addAffectedFiles(project, component.getTag().getContainingFile().getVirtualFile());
    }));
    return null;
  }

  @NotNull
  @Override
  public JComponent createComponent(@NotNull ComponentAssistant.Context context) {
    return new RecyclerViewAssistant(context);
  }

  /**
   * Holder class for the templates information
   */
  private static class Template {
    final String myTemplateName;
    final String myTemplate;

    private Template(@NotNull String templateName, @NotNull String template) {
      myTemplateName = templateName;
      myTemplate = template;
    }

    @Override
    public String toString() {
      return myTemplateName;
    }
  }
}
