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
import lombok.ast.EnumDeclaration;
import lombok.ast.ForwardingAstVisitor;
import lombok.ast.Node;

public class EnumDetector extends Detector implements JavaScanner {
  public static final Issue ENUMS_ARE_BAD_ISSUE = Issue.create(
      "EnumsAreBad",
      "M'kay?",
      "" +
          "Every byte is sacred,\n" +
          "Every byte is great.\n" +
          "If a byte is wasted,\n" +
          "Colt gets quite irate.",
      Category.PERFORMANCE,
      1,
      Severity.INFORMATIONAL,
      new Implementation(EnumDetector.class, Scope.JAVA_FILE_SCOPE));

  @Override
  public List<Class<? extends Node>> getApplicableNodeTypes() {
    return Collections.<Class<? extends Node>>singletonList(EnumDeclaration.class);
  }

  @Override
  public AstVisitor createJavaVisitor(final JavaContext context) {
    return new ForwardingAstVisitor() {
      @Override
      public boolean visitEnumDeclaration(EnumDeclaration node) {
        context.report(
            ENUMS_ARE_BAD_ISSUE,
            context.getLocation(node),
            String.format(
                "Consider using int constants instead of %1$s",
                node.astName().astValue()
            )
        );

        return super.visitEnumDeclaration(node);
      }
    };
  }
}
