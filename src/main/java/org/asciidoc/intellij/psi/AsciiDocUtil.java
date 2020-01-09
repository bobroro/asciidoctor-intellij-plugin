package org.asciidoc.intellij.psi;

import com.intellij.codeInsight.completion.CompletionUtilCore;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.text.CharArrayUtil;
import org.asciidoc.intellij.AsciiDocLanguage;
import org.asciidoc.intellij.file.AsciiDocFileType;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AsciiDocUtil {

  public static final String FAMILY_EXAMPLE = "example";
  public static final String FAMILY_ATTACHMENT = "attachment";
  public static final String FAMILY_PARTIAL = "partial";
  public static final String FAMILY_IMAGE = "image";
  public static final String FAMILY_PAGE = "page";
  public static final String ANTORA_YML = "antora.yml";

  static List<AsciiDocBlockId> findIds(Project project, String key) {
    List<AsciiDocBlockId> result = null;
    Collection<VirtualFile> virtualFiles =
      FileTypeIndex.getFiles(AsciiDocFileType.INSTANCE, GlobalSearchScope.allScope(project));
    ProjectFileIndex index = ProjectRootManager.getInstance(project).getFileIndex();
    for (VirtualFile virtualFile : virtualFiles) {
      if (index.isInLibrary(virtualFile)
        || index.isExcluded(virtualFile)
        || index.isInLibraryClasses(virtualFile)
        || index.isInLibrarySource(virtualFile)) {
        continue;
      }
      AsciiDocFile asciiDocFile = (AsciiDocFile) PsiManager.getInstance(project).findFile(virtualFile);
      if (asciiDocFile != null) {
        Collection<AsciiDocBlockId> properties = PsiTreeUtil.findChildrenOfType(asciiDocFile, AsciiDocBlockId.class);
        for (AsciiDocBlockId blockId : properties) {
          if (key.equals(blockId.getId())) {
            if (result == null) {
              result = new ArrayList<>();
            }
            result.add(blockId);
          }
        }
      }
    }
    return result != null ? result : Collections.emptyList();
  }

  public static List<AsciiDocBlockId> findIds(Project project, VirtualFile virtualFile, String key) {
    List<AsciiDocBlockId> result = null;
    AsciiDocFile asciiDocFile = (AsciiDocFile) PsiManager.getInstance(project).findFile(virtualFile);
    if (asciiDocFile != null) {
      Collection<AsciiDocBlockId> properties = PsiTreeUtil.findChildrenOfType(asciiDocFile, AsciiDocBlockId.class);
      for (AsciiDocBlockId blockId : properties) {
        if (key.equals(blockId.getId())) {
          if (result == null) {
            result = new ArrayList<>();
          }
          result.add(blockId);
        }
      }
    }
    return result != null ? result : Collections.emptyList();
  }

  static List<AsciiDocBlockId> findIds(Project project) {
    List<AsciiDocBlockId> result = new ArrayList<>();
    Collection<VirtualFile> virtualFiles =
      FileTypeIndex.getFiles(AsciiDocFileType.INSTANCE, GlobalSearchScope.allScope(project));
    ProjectFileIndex index = ProjectRootManager.getInstance(project).getFileIndex();
    for (VirtualFile virtualFile : virtualFiles) {
      if (index.isInLibrary(virtualFile)
        || index.isExcluded(virtualFile)
        || index.isInLibraryClasses(virtualFile)
        || index.isInLibrarySource(virtualFile)) {
        continue;
      }
      AsciiDocFile asciiDocFile = (AsciiDocFile) PsiManager.getInstance(project).findFile(virtualFile);
      if (asciiDocFile != null) {
        Collection<AsciiDocBlockId> properties = PsiTreeUtil.findChildrenOfType(asciiDocFile, AsciiDocBlockId.class);
        result.addAll(properties);
      }
    }
    return result;
  }

  static List<AsciiDocAttributeDeclaration> findAttributes(Project project, String key) {
    List<AsciiDocAttributeDeclaration> result = null;
    final GlobalSearchScope scope = GlobalSearchScope.projectScope(project);
    Collection<AsciiDocAttributeDeclaration> asciiDocAttributeDeclarations = AsciiDocAttributeDeclarationKeyIndex.getInstance().get(key, project, scope);
    ProjectFileIndex index = ProjectRootManager.getInstance(project).getFileIndex();
    for (AsciiDocAttributeDeclaration asciiDocAttributeDeclaration : asciiDocAttributeDeclarations) {
      VirtualFile virtualFile = asciiDocAttributeDeclaration.getContainingFile().getVirtualFile();
      if (index.isInLibrary(virtualFile)
        || index.isExcluded(virtualFile)
        || index.isInLibraryClasses(virtualFile)
        || index.isInLibrarySource(virtualFile)) {
        continue;
      }
      if (result == null) {
        result = new ArrayList<>();
      }
      result.add(asciiDocAttributeDeclaration);
    }
    return result != null ? result : Collections.emptyList();
  }

  static List<AsciiDocAttributeDeclaration> findAttributes(Project project) {
    List<AsciiDocAttributeDeclaration> result = new ArrayList<>();
    ProjectFileIndex index = ProjectRootManager.getInstance(project).getFileIndex();
    Collection<String> keys = AsciiDocAttributeDeclarationKeyIndex.getInstance().getAllKeys(project);
    final GlobalSearchScope scope = GlobalSearchScope.projectScope(project);
    for (String key : keys) {
      Collection<AsciiDocAttributeDeclaration> asciiDocAttributeDeclarations = AsciiDocAttributeDeclarationKeyIndex.getInstance().get(key, project, scope);
      for (AsciiDocAttributeDeclaration asciiDocAttributeDeclaration : asciiDocAttributeDeclarations) {
        VirtualFile virtualFile = asciiDocAttributeDeclaration.getContainingFile().getVirtualFile();
        if (index.isInLibrary(virtualFile)
          || index.isExcluded(virtualFile)
          || index.isInLibraryClasses(virtualFile)
          || index.isInLibrarySource(virtualFile)) {
          continue;
        }
        result.add(asciiDocAttributeDeclaration);
      }
    }
    return result;
  }

  static List<AttributeDeclaration> findAttributes(Project project, String key, PsiElement current) {
    List<AttributeDeclaration> result = new ArrayList<>(findAttributes(project, key));

    if (key.equals("snippets")) {
      augmentList(result, AsciiDocUtil.findSpringRestDocSnippets(current), "snippets");
    }

    if (key.equals(FAMILY_PARTIAL + "sdir")) {
      augmentList(result, AsciiDocUtil.findAntoraPartials(current), FAMILY_PARTIAL + "sdir");
    }
    if (key.equals(FAMILY_IMAGE + "sdir")) {
      augmentList(result, AsciiDocUtil.findAntoraImagesDir(current), FAMILY_IMAGE + "sdir");
    }
    if (key.equals(FAMILY_ATTACHMENT + "sdir")) {
      augmentList(result, AsciiDocUtil.findAntoraAttachmentsDir(current), FAMILY_ATTACHMENT + "sdir");
    }
    if (key.equals(FAMILY_EXAMPLE + "sdir")) {
      augmentList(result, AsciiDocUtil.findAntoraExamplesDir(current), FAMILY_EXAMPLE + "sdir");
    }
    return result;
  }


  static List<AttributeDeclaration> findAttributes(Project project, PsiElement current) {
    List<AttributeDeclaration> result = new ArrayList<>(findAttributes(project));

    augmentList(result, AsciiDocUtil.findSpringRestDocSnippets(current), "snippets");

    augmentList(result, AsciiDocUtil.findAntoraPartials(current), FAMILY_PARTIAL + "sdir");
    augmentList(result, AsciiDocUtil.findAntoraImagesDir(current), FAMILY_IMAGE + "sdir");
    augmentList(result, AsciiDocUtil.findAntoraAttachmentsDir(current), FAMILY_ATTACHMENT + "sdir");
    augmentList(result, AsciiDocUtil.findAntoraExamplesDir(current), FAMILY_EXAMPLE + "sdir");

    return result;
  }

  static void augmentList(List<AttributeDeclaration> list, VirtualFile file, String attributeName) {
    if (file != null) {
      String value = file.getPath();
      value = value.replaceAll("\\\\", "/");
      list.add(new AsciiDocAttributeDeclarationDummy(attributeName, value));
    }
  }

  @Nullable
  public static PsiElement getStatementAtCaret(@NotNull Editor editor, @NotNull PsiFile psiFile) {
    int caret = editor.getCaretModel().getOffset();

    final Document doc = editor.getDocument();
    CharSequence chars = doc.getCharsSequence();
    int offset = caret == 0 ? 0 : CharArrayUtil.shiftBackward(chars, caret - 1, " \t");
    if (offset < 0) {
      // happens if spaces and tabs at beginning of file
      offset = 0;
    }
    if (doc.getLineNumber(offset) < doc.getLineNumber(caret)) {
      offset = CharArrayUtil.shiftForward(chars, caret, " \t");
    }

    return psiFile.findElementAt(offset);
  }

  @NotNull
  public static AsciiDocFile createFileFromText(@NotNull Project project, @NotNull String text) {
    return (AsciiDocFile) PsiFileFactory.getInstance(project).createFileFromText("a.adoc", AsciiDocLanguage.INSTANCE, text);
  }

  public static VirtualFile findAntoraPartials(VirtualFile projectBasePath, VirtualFile fileBaseDir) {
    VirtualFile dir = fileBaseDir;
    while (dir != null) {
      if (dir.getParent() != null && dir.getParent().getName().equals("modules") &&
        dir.getParent().getParent().findChild(ANTORA_YML) != null) {
        VirtualFile antoraPartials = dir.findChild(FAMILY_PARTIAL + "s");
        if (antoraPartials != null) {
          return antoraPartials;
        }
        VirtualFile antoraPages = dir.findChild(FAMILY_PAGE + "s");
        if (antoraPages != null) {
          VirtualFile antoraPagePartials = antoraPages.findChild("_" + FAMILY_PARTIAL + "s");
          if (antoraPagePartials != null) {
            return antoraPagePartials;
          }
        }
      }
      if (projectBasePath.equals(dir)) {
        break;
      }
      dir = dir.getParent();
    }
    return null;
  }

  public static VirtualFile findAntoraAttachmentsDir(VirtualFile projectBasePath, VirtualFile fileBaseDir) {
    VirtualFile dir = fileBaseDir;
    while (dir != null) {
      if (dir.getParent() != null && dir.getParent().getName().equals("modules") &&
        dir.getParent().getParent().findChild(ANTORA_YML) != null) {
        VirtualFile assets = dir.findChild("assets");
        if (assets != null) {
          VirtualFile attachments = assets.findChild(FAMILY_ATTACHMENT + "s");
          if (attachments != null) {
            return attachments;
          }
        }
        VirtualFile attachments = dir.findChild(FAMILY_ATTACHMENT + "s");
        if (attachments != null) {
          return attachments;
        }
      }
      if (projectBasePath.equals(dir)) {
        break;
      }
      dir = dir.getParent();
    }
    return null;
  }

  public static VirtualFile findAntoraPagesDir(VirtualFile projectBasePath, VirtualFile fileBaseDir) {
    VirtualFile dir = fileBaseDir;
    while (dir != null) {
      if (dir.getParent() != null && dir.getParent().getName().equals("modules") &&
        dir.getParent().getParent().findChild(ANTORA_YML) != null) {
        VirtualFile pages = dir.findChild(FAMILY_PAGE + "s");
        if (pages != null) {
          return pages;
        }
      }
      if (projectBasePath.equals(dir)) {
        break;
      }
      dir = dir.getParent();
    }
    return null;
  }

  public static VirtualFile findAntoraModuleDir(VirtualFile projectBasePath, VirtualFile fileBaseDir) {
    VirtualFile dir = fileBaseDir;
    while (dir != null) {
      if (dir.getParent() != null && dir.getParent().getName().equals("modules") &&
        dir.getParent().getParent().findChild(ANTORA_YML) != null) {
        return dir;
      }
      if (projectBasePath.equals(dir)) {
        break;
      }
      dir = dir.getParent();
    }
    return null;
  }

  public static String findAntoraImagesDirRelative(VirtualFile projectBasePath, VirtualFile fileBaseDir) {
    VirtualFile dir = fileBaseDir;
    StringBuilder imagesDir = new StringBuilder();
    while (dir != null) {
      if (dir.getParent() != null && dir.getParent().getName().equals("modules") &&
        dir.getParent().getParent().findChild(ANTORA_YML) != null) {
        VirtualFile assets = dir.findChild("assets");
        if (assets != null) {
          VirtualFile images = assets.findChild(FAMILY_IMAGE + "s");
          if (images != null) {
            return imagesDir + "assets/" + FAMILY_IMAGE + "s";
          }
        }
        VirtualFile images = dir.findChild(FAMILY_IMAGE + "s");
        if (images != null) {
          return imagesDir + FAMILY_IMAGE + "s";
        }
      }
      if (projectBasePath.equals(dir)) {
        break;
      }
      dir = dir.getParent();
      imagesDir.insert(0, "../");
    }
    return null;
  }

  public static String findAntoraAttachmentsDirRelative(VirtualFile projectBasePath, VirtualFile fileBaseDir) {
    VirtualFile dir = fileBaseDir;
    StringBuilder attachmentsDir = new StringBuilder();
    while (dir != null) {
      if (dir.getParent() != null && dir.getParent().getName().equals("modules") &&
        dir.getParent().getParent().findChild(ANTORA_YML) != null) {
        VirtualFile assets = dir.findChild("assets");
        if (assets != null) {
          VirtualFile attachments = assets.findChild(FAMILY_ATTACHMENT + "s");
          if (attachments != null) {
            return attachmentsDir + "assets/" + FAMILY_ATTACHMENT + "s";
          }
        }
        VirtualFile attachments = dir.findChild(FAMILY_ATTACHMENT + "s");
        if (attachments != null) {
          return attachmentsDir + FAMILY_ATTACHMENT + "s";
        }
      }
      if (projectBasePath.equals(dir)) {
        break;
      }
      dir = dir.getParent();
      attachmentsDir.insert(0, "../");
    }
    return null;
  }

  public static VirtualFile findAntoraImagesDir(VirtualFile projectBasePath, VirtualFile fileBaseDir) {
    VirtualFile dir = fileBaseDir;
    while (dir != null) {
      if (dir.getParent() != null && dir.getParent().getName().equals("modules") &&
        dir.getParent().getParent().findChild(ANTORA_YML) != null) {
        VirtualFile assets = dir.findChild("assets");
        if (assets != null) {
          VirtualFile images = assets.findChild(FAMILY_IMAGE + "s");
          if (images != null) {
            return images;
          }
        }
        VirtualFile images = dir.findChild(FAMILY_IMAGE + "s");
        if (images != null) {
          return images;
        }
      }
      if (projectBasePath.equals(dir)) {
        break;
      }
      dir = dir.getParent();
    }
    return null;
  }

  public static VirtualFile findAntoraExamplesDir(VirtualFile projectBasePath, VirtualFile fileBaseDir) {
    VirtualFile dir = fileBaseDir;
    while (dir != null) {
      if (dir.getParent() != null && dir.getParent().getName().equals("modules") &&
        dir.getParent().getParent().findChild(ANTORA_YML) != null) {
        VirtualFile examples = dir.findChild(FAMILY_EXAMPLE + "s");
        if (examples != null) {
          return examples;
        }
      }
      if (projectBasePath.equals(dir)) {
        break;
      }
      dir = dir.getParent();
    }
    return null;
  }

  public static VirtualFile findSpringRestDocSnippets(VirtualFile projectBasePath, VirtualFile fileBaseDir) {
    VirtualFile dir = fileBaseDir;
    while (dir != null) {
      VirtualFile pom = dir.findChild("pom.xml");
      if (pom != null) {
        VirtualFile targetDir = dir.findChild("target");
        if (targetDir != null) {
          VirtualFile snippets = targetDir.findChild("generated-snippets");
          if (snippets != null) {
            return snippets;
          }
        }
      }
      VirtualFile buildGradle = dir.findChild("build.gradle");
      if (buildGradle != null) {
        VirtualFile buildDir = dir.findChild("build");
        if (buildDir != null) {
          VirtualFile snippets = buildDir.findChild("generated-snippets");
          if (snippets != null) {
            return snippets;
          }
        }
      }
      VirtualFile buildGradleKts = dir.findChild("build.gradle.kts");
      if (buildGradleKts != null) {
        VirtualFile buildDir = dir.findChild("build");
        if (buildDir != null) {
          VirtualFile snippets = buildDir.findChild("generated-snippets");
          if (snippets != null) {
            return snippets;
          }
        }
      }
      if (projectBasePath.equals(dir)) {
        break;
      }
      dir = dir.getParent();
    }

    return null;
  }

  public static VirtualFile findSpringRestDocSnippets(PsiElement element) {
    VirtualFile springRestDocSnippets = null;
    VirtualFile vf;
    vf = element.getContainingFile().getVirtualFile();
    if (vf == null) {
      // when running autocomplete, there is only an original file
      vf = element.getContainingFile().getOriginalFile().getVirtualFile();
    }
    if (vf != null) {
      springRestDocSnippets = findSpringRestDocSnippets(element.getProject().getBaseDir(), vf);
    }
    return springRestDocSnippets;
  }

  public static VirtualFile findAntoraPartials(PsiElement element) {
    VirtualFile antoraPartials = null;
    VirtualFile vf;
    vf = element.getContainingFile().getVirtualFile();
    if (vf == null) {
      // when running autocomplete, there is only an original file
      vf = element.getContainingFile().getOriginalFile().getVirtualFile();
    }
    if (vf != null) {
      antoraPartials = findAntoraPartials(element.getProject().getBaseDir(), vf);
    }
    return antoraPartials;
  }

  public static VirtualFile findAntoraImagesDir(PsiElement element) {
    VirtualFile antoraImagesDir = null;
    VirtualFile vf;
    vf = element.getContainingFile().getVirtualFile();
    if (vf == null) {
      // when running autocomplete, there is only an original file
      vf = element.getContainingFile().getOriginalFile().getVirtualFile();
    }
    if (vf != null) {
      antoraImagesDir = findAntoraImagesDir(element.getProject().getBaseDir(), vf);
    }
    return antoraImagesDir;
  }

  public static VirtualFile findAntoraExamplesDir(PsiElement element) {
    VirtualFile antoraExamplesDir = null;
    VirtualFile vf;
    vf = element.getContainingFile().getVirtualFile();
    if (vf == null) {
      // when running autocomplete, there is only an original file
      vf = element.getContainingFile().getOriginalFile().getVirtualFile();
    }
    if (vf != null) {
      antoraExamplesDir = findAntoraExamplesDir(element.getProject().getBaseDir(), vf);
    }
    return antoraExamplesDir;
  }

  public static VirtualFile findAntoraAttachmentsDir(PsiElement element) {
    VirtualFile antoraAttachmentsDir = null;
    VirtualFile vf;
    vf = element.getContainingFile().getVirtualFile();
    if (vf == null) {
      // when running autocomplete, there is only an original file
      vf = element.getContainingFile().getOriginalFile().getVirtualFile();
    }
    if (vf != null) {
      antoraAttachmentsDir = findAntoraAttachmentsDir(element.getProject().getBaseDir(), vf);
    }
    return antoraAttachmentsDir;
  }

  public static VirtualFile findAntoraPagesDir(PsiElement element) {
    VirtualFile antoraPagesDir = null;
    VirtualFile vf;
    vf = element.getContainingFile().getVirtualFile();
    if (vf == null) {
      // when running autocomplete, there is only an original file
      vf = element.getContainingFile().getOriginalFile().getVirtualFile();
    }
    if (vf != null) {
      antoraPagesDir = findAntoraPagesDir(element.getProject().getBaseDir(), vf);
    }
    return antoraPagesDir;
  }

  public static VirtualFile findAntoraModuleDir(PsiElement element) {
    VirtualFile antoraModuleDir = null;
    VirtualFile vf;
    vf = element.getContainingFile().getVirtualFile();
    if (vf == null) {
      // when running autocomplete, there is only an original file
      vf = element.getContainingFile().getOriginalFile().getVirtualFile();
    }
    if (vf != null) {
      antoraModuleDir = findAntoraModuleDir(element.getProject().getBaseDir(), vf);
    }
    return antoraModuleDir;
  }

  public static final Pattern ANTORA_PREFIX_AND_FAMILY_PATTERN = Pattern.compile("^[a-zA-Z0-9:._-]*(" + CompletionUtilCore.DUMMY_IDENTIFIER + "[a-zA-Z0-9:._-]*)?[$:]");

  public static final Pattern ANTORA_PREFIX_PATTERN = Pattern.compile("^[a-zA-Z0-9:._-]*(" + CompletionUtilCore.DUMMY_IDENTIFIER + "[a-zA-Z0-9:._-]*)?[:]");

  public static final Pattern ANTORA_FAMILY_PATTERN = Pattern.compile("^[a-z]*(" + CompletionUtilCore.DUMMY_IDENTIFIER + "[a-z]*)?[$]");

  @Language("RegExp")
  private static final String FAMILIES = "(" + FAMILY_EXAMPLE + "|" + FAMILY_ATTACHMENT + "|" + FAMILY_PARTIAL + "|" + FAMILY_IMAGE + "|" + FAMILY_PAGE + ")";

  // component:module:
  private static final Pattern COMPONENT_MODULE = Pattern.compile("^(?<component>[a-zA-Z0-9._-]*):(?<module>[a-zA-Z0-9._-]*):");
  // module:
  private static final Pattern MODULE = Pattern.compile("^(?<module>[a-zA-Z0-9._-]*):");
  // family$
  private static final Pattern FAMILY = Pattern.compile("^(?<family>" + FAMILIES + ")\\$");

  public static String replaceAntoraPrefix(PsiElement myElement, String key, String defaultFamily) {
    VirtualFile antoraModuleDir = findAntoraModuleDir(myElement);
    if (antoraModuleDir != null) {
      return replaceAntoraPrefix(myElement.getProject(), antoraModuleDir, key, defaultFamily);
    } else {
      return key;
    }
  }

  public static String replaceAntoraPrefix(Project project, VirtualFile moduleDir, String originalKey, String defaultFamily) {
    if (moduleDir != null) {
      return ApplicationManager.getApplication().runReadAction((Computable<String>) () -> {
        VirtualFile antoraModuleDir = moduleDir;
        String key = originalKey;
        String myModuleName = antoraModuleDir.getName();
        VirtualFile antoraFile = antoraModuleDir.getParent().getParent().findChild(ANTORA_YML);
        if (antoraFile == null) {
          return originalKey;
        }
        Document document = FileDocumentManager.getInstance().getDocument(antoraFile);
        if (document == null) {
          return originalKey;
        }
        Yaml yaml = new Yaml();
        Map<String, Object> antora = yaml.load(document.getText());
        String myComponentName = (String) antora.get("name");
        String myComponentVersion = (String) antora.get("version");

        String otherComponentName = null;
        String otherModuleName = null;
        String otherFamily = null;

        Matcher componentModule = COMPONENT_MODULE.matcher(key);
        if (componentModule.find()) {
          otherComponentName = componentModule.group("component");
          otherModuleName = componentModule.group("module");
          key = componentModule.replaceFirst("");
        } else {
          Matcher module = MODULE.matcher(key);
          if (module.find()) {
            otherModuleName = module.group("module");
            key = module.replaceFirst("");
          }
        }
        Matcher family = FAMILY.matcher(key);
        if (family.find()) {
          otherFamily = family.group("family");
          key = family.replaceFirst("");
        } else {
          if (defaultFamily == null) {
            return originalKey;
          }
        }

        if (otherFamily == null || otherFamily.length() == 0) {
          otherFamily = defaultFamily;
        }

        if (myComponentName.equals(otherComponentName)) {
          otherComponentName = null;
        }
        if (otherComponentName == null && myModuleName.equals(otherModuleName)) {
          otherModuleName = null;
        }

        if (otherModuleName != null && otherComponentName == null) {
          antoraModuleDir = antoraModuleDir.getParent().findChild(otherModuleName);
          if (antoraModuleDir == null) {
            // might be a module in another component with the same name
            otherComponentName = myComponentName;
          }
        }

        if (otherComponentName != null) {
          if (otherModuleName == null || otherModuleName.length() == 0) {
            otherModuleName = myModuleName;
          }
          PsiFile[] files =
            FilenameIndex.getFilesByName(project, ANTORA_YML, GlobalSearchScope.projectScope(project));
          // sort by path proximity
          Arrays.sort(files,
            Comparator.comparingInt(value -> countNumberOfSameStartingCharacters(value, moduleDir.getPath()) * -1));
          ProjectFileIndex index = ProjectRootManager.getInstance(project).getFileIndex();
          for (PsiFile file : files) {
            if (index.isInLibrary(file.getVirtualFile())
              || index.isExcluded(file.getVirtualFile())
              || index.isInLibraryClasses(file.getVirtualFile())
              || index.isInLibrarySource(file.getVirtualFile())) {
              continue;
            }
            antora = yaml.load(file.getText());
            if (!otherComponentName.equals(antora.get("name"))) {
              continue;
            }
            if (!myComponentVersion.equals(antora.get("version"))) {
              continue;
            }
            PsiDirectory parent = file.getParent();
            if (parent == null) {
              continue;
            }
            PsiDirectory antoraModulesDir = parent.findSubdirectory("modules");
            if (antoraModulesDir == null) {
              continue;
            }
            PsiDirectory antoraModule = antoraModulesDir.findSubdirectory(otherModuleName);
            if (antoraModule == null) {
              continue;
            }
            antoraModuleDir = antoraModule.getVirtualFile();
            break;
          }
        }

        VirtualFile baseDir = project.getBaseDir();

        VirtualFile target;
        switch (otherFamily) {
          case FAMILY_EXAMPLE:
            target = AsciiDocUtil.findAntoraExamplesDir(baseDir, antoraModuleDir);
            break;
          case FAMILY_ATTACHMENT:
            target = AsciiDocUtil.findAntoraAttachmentsDir(baseDir, antoraModuleDir);
            break;
          case FAMILY_PAGE:
            target = AsciiDocUtil.findAntoraPagesDir(baseDir, antoraModuleDir);
            break;
          case FAMILY_PARTIAL:
            target = AsciiDocUtil.findAntoraPartials(baseDir, antoraModuleDir);
            break;
          case FAMILY_IMAGE:
            target = AsciiDocUtil.findAntoraImagesDir(baseDir, antoraModuleDir);
            break;
          default:
            return originalKey;
        }
        if (target == null) {
          return originalKey;
        }
        if (key.length() != 0) {
          key = "/" + key;
        }
        String value = target.getPath();
        value = value.replaceAll("\\\\", "/");
        key = value + key;
        return key;
      });
    }
    return originalKey;
  }

  public static List<AntoraModule> collectPrefixes(Project project, VirtualFile moduleDir) {
    return ApplicationManager.getApplication().runReadAction((Computable<List<AntoraModule>>) () -> {
      PsiFile[] files =
        FilenameIndex.getFilesByName(project, ANTORA_YML, GlobalSearchScope.projectScope(project));
      List<AntoraModule> result = new ArrayList<>();
      // sort by path proximity
      Arrays.sort(files,
        Comparator.comparingInt(value -> countNumberOfSameStartingCharacters(value, moduleDir.getPath()) * -1));
      ProjectFileIndex index = ProjectRootManager.getInstance(project).getFileIndex();
      VirtualFile antoraModuleDir = moduleDir;
      String myModuleName = antoraModuleDir.getName();
      VirtualFile antoraFile = antoraModuleDir.getParent().getParent().findChild(ANTORA_YML);
      if (antoraFile == null) {
        return result;
      }
      Document document = FileDocumentManager.getInstance().getDocument(antoraFile);
      if (document == null) {
        return result;
      }
      Yaml yaml = new Yaml();
      Map<String, Object> antora = yaml.load(document.getText());
      String myComponentName = (String) antora.get("name");
      String myComponentVersion = (String) antora.get("version");
      Map<String, String> componentTitles = new HashMap<>();
      for (PsiFile file : files) {
        if (index.isInLibrary(file.getVirtualFile())
          || index.isExcluded(file.getVirtualFile())
          || index.isInLibraryClasses(file.getVirtualFile())
          || index.isInLibrarySource(file.getVirtualFile())) {
          continue;
        }
        antora = yaml.load(file.getText());
        if (!myComponentVersion.equals(antora.get("version"))) {
          continue;
        }
        String otherComponentName = (String) antora.get("name");
        String title = (String) antora.get("title");
        if (title != null && componentTitles.get(otherComponentName) == null) {
          componentTitles.put(otherComponentName, title);
        }
        VirtualFile md = file.getVirtualFile().getParent().findChild("modules");
        if (md != null) {
          VirtualFile[] modules = md.getChildren();
          for (VirtualFile module : modules) {
            if (MODULE.matcher(module.getName() + ":").matches()) {
              if (myComponentName.equals(otherComponentName)) {
                result.add(new AntoraModule(module.getName() + ":", otherComponentName, module.getName(), title, module));
              }
              if (!myComponentName.equals(otherComponentName) && myModuleName.equals(module.getName())) {
                result.add(new AntoraModule(otherComponentName + "::", otherComponentName, module.getName(), title, module));
              }
              result.add(new AntoraModule(otherComponentName + ":" + module.getName() + ":", otherComponentName, module.getName(), title, module));
            }
          }
        }
      }
      for (AntoraModule antoraModule : result) {
        // title might not have been included on all modules, populate other if it has been set on some
        if (antoraModule.getTitle() == null) {
          antoraModule.setTitle(componentTitles.get(antoraModule.getComponent()));
        }
      }
      return result;
    });
  }

  public static VirtualFile resolvePrefix(Project project, VirtualFile moduleDir, String originalKey) {
    return ApplicationManager.getApplication().runReadAction((Computable<VirtualFile>) () -> {
      VirtualFile antoraModuleDir = moduleDir;
      String myModuleName = antoraModuleDir.getName();
      VirtualFile antoraFile = antoraModuleDir.getParent().getParent().findChild(ANTORA_YML);
      if (antoraFile == null) {
        return null;
      }
      Document document = FileDocumentManager.getInstance().getDocument(antoraFile);
      if (document == null) {
        return null;
      }
      Yaml yaml = new Yaml();
      Map<String, Object> antora = yaml.load(document.getText());
      String myComponentName = (String) antora.get("name");
      String myComponentVersion = (String) antora.get("version");

      String otherComponentName = null;
      String otherModuleName = null;

      Matcher componentModule = COMPONENT_MODULE.matcher(originalKey);
      if (componentModule.find()) {
        otherComponentName = componentModule.group("component");
        otherModuleName = componentModule.group("module");
      } else {
        Matcher module = MODULE.matcher(originalKey);
        if (module.find()) {
          otherModuleName = module.group("module");
        }
      }

      if (myComponentName.equals(otherComponentName)) {
        otherComponentName = null;
      }
      if (otherComponentName == null && myModuleName.equals(otherModuleName)) {
        otherModuleName = null;
      }

      if (otherModuleName != null && otherComponentName == null) {
        antoraModuleDir = antoraModuleDir.getParent().findChild(otherModuleName);
        if (antoraModuleDir == null) {
          // might be a module in another component with the same name
          otherComponentName = myComponentName;
        }
      }

      if (otherComponentName != null) {
        if (otherModuleName == null || otherModuleName.length() == 0) {
          otherModuleName = myModuleName;
        }
        PsiFile[] files =
          FilenameIndex.getFilesByName(project, ANTORA_YML, GlobalSearchScope.projectScope(project));
        // sort by path proximity
        Arrays.sort(files,
          Comparator.comparingInt(value -> countNumberOfSameStartingCharacters(value, moduleDir.getPath()) * -1));
        ProjectFileIndex index = ProjectRootManager.getInstance(project).getFileIndex();
        for (PsiFile file : files) {
          if (index.isInLibrary(file.getVirtualFile())
            || index.isExcluded(file.getVirtualFile())
            || index.isInLibraryClasses(file.getVirtualFile())
            || index.isInLibrarySource(file.getVirtualFile())) {
            continue;
          }
          antora = yaml.load(file.getText());
          if (!otherComponentName.equals(antora.get("name"))) {
            continue;
          }
          if (!myComponentVersion.equals(antora.get("version"))) {
            continue;
          }
          PsiDirectory parent = file.getParent();
          if (parent == null) {
            continue;
          }
          PsiDirectory antoraModulesDir = parent.findSubdirectory("modules");
          if (antoraModulesDir == null) {
            continue;
          }
          PsiDirectory antoraModule = antoraModulesDir.findSubdirectory(otherModuleName);
          if (antoraModule == null) {
            continue;
          }
          antoraModuleDir = antoraModule.getVirtualFile();
          break;
        }
      }

      return antoraModuleDir;
    });
  }

  private static int countNumberOfSameStartingCharacters(PsiFile value, String origin) {
    String path = value.getVirtualFile().getPath();
    int i = 0;
    for (; i < origin.length() && i < path.length(); ++i) {
      if (path.charAt(i) != origin.charAt(i)) {
        break;
      }
    }
    return i;
  }

}
