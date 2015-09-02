package com.getbase.lint.issues;

import static com.android.SdkConstants.CONSTRUCTOR_NAME;

import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.ClassContext;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Detector.ClassScanner;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.LintUtils;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

public class MicroOrmDetector extends Detector implements ClassScanner {

  public static final Issue NO_PUBLIC_DEFAULT_CONSTRUCTOR_ISSUE = Issue.create(
      "MicroOrmDefaultConstructor",
      "MicroOrm annotated class without public default constructor",
      "`MicroOrm.fromCursor()` and `MicroOrm.getFunctionFor()` needs a public default constructor.",
      Category.CORRECTNESS,
      7,
      Severity.WARNING,
      new Implementation(MicroOrmDetector.class, Scope.CLASS_FILE_SCOPE));

  @Override
  public void checkClass(ClassContext context, ClassNode classNode) {
    super.checkClass(context, classNode);

    if (!isInstantiable(classNode) && isAnnotatedByMicroOrm(classNode)) {

      context.report(
          NO_PUBLIC_DEFAULT_CONSTRUCTOR_ISSUE,
          context.getLocation(classNode),
          String.format(
              "This class should provide a public default constructor (a constructor with no arguments) (%1$s)",
              ClassContext.createSignature(classNode.name, null, null)
          )
      );
    }
  }

  private static final String COLUMN_ANNOTATION_DESC = "Lorg/chalup/microorm/annotations/Column;";
  private static final String EMBEDDED_ANNOTATION_DESC = "Lorg/chalup/microorm/annotations/Embedded;";

  private static boolean isAnnotatedByMicroOrm(ClassNode classNode) {
    for (Object f : classNode.fields) {
      FieldNode field = (FieldNode) f;
      if (field.visibleAnnotations != null) {
        for (Object a : field.visibleAnnotations) {
          AnnotationNode annotationNode = (AnnotationNode) a;
          if (COLUMN_ANNOTATION_DESC.equals(annotationNode.desc) || EMBEDDED_ANNOTATION_DESC.equals(annotationNode.desc)) {
            return true;
          }
        }
      }
    }

    return false;
  }

  private static boolean isInstantiable(ClassNode classNode) {
    if ((classNode.access & Opcodes.ACC_PUBLIC) == 0) {
      return false;
    }

    if (classNode.name.indexOf('$') != -1 && !LintUtils.isStaticInnerClass(classNode)) {
      return false;
    }

    @SuppressWarnings("rawtypes")
    List methodList = classNode.methods;
    for (Object m : methodList) {
      MethodNode method = (MethodNode) m;
      if (method.name.equals(CONSTRUCTOR_NAME)) {
        if (method.desc.equals("()V")) {
          return (method.access & Opcodes.ACC_PUBLIC) != 0;
        }
      }
    }
    return false;
  }
}
