package com.getbase.lint.issues;

import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Detector.JavaScanner;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;

import java.util.Collections;
import java.util.List;

import lombok.ast.AstVisitor;
import lombok.ast.ClassDeclaration;
import lombok.ast.ForwardingAstVisitor;
import lombok.ast.Node;
import lombok.ast.TypeReference;

public class CursorWrapperDetector extends Detector implements JavaScanner {
  public static final Issue USE_CROSS_PROCESS_CURSOR_WRAPPERS = Issue
      .create(
          "PreferCrossProcessCursorWrappers",
          "Prefer CrossProcessCursorWrapper over CursorWrapper",
          "" +
              "CrossProcessCursorWrapper provides better performance than " +
              "regular CursorWrapper when Cursor is used in the remote " +
              "process.",
          Category.PERFORMANCE,
          1,
          Severity.INFORMATIONAL,
          new Implementation(CursorWrapperDetector.class, Scope.JAVA_FILE_SCOPE))
      .addMoreInfo("http://developer.android.com/reference/android/database/CrossProcessCursor.html");

  @Override
  public List<Class<? extends Node>> getApplicableNodeTypes() {
    return Collections.<Class<? extends Node>>singletonList(ClassDeclaration.class);
  }

  private static final String CURSOR_WRAPPER = "android.database.CursorWrapper";

  @Override
  public AstVisitor createJavaVisitor(final JavaContext context) {
    if (context.getProject().getMinSdk() < 15) return null;

    return new ForwardingAstVisitor() {
      @Override
      public boolean visitClassDeclaration(ClassDeclaration node) {
        TypeReference extending = node.astExtending();
        if (extending != null) {
          if (CURSOR_WRAPPER.equals(context.resolve(extending).getSignature())) {
            context.report(
                USE_CROSS_PROCESS_CURSOR_WRAPPERS,
                node,
                context.getLocation(node.astName()),
                String.format(
                    "%1$s can extend CrossProcessCursorWrapper",
                    node.astName().astValue()
                )
            );
          }
        }

        return super.visitClassDeclaration(node);
      }
    };
  }
}
