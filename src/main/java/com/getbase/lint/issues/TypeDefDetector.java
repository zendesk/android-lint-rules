package com.getbase.lint.issues;

import com.android.SdkConstants;
import com.android.tools.lint.client.api.JavaParser.ResolvedNode;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Detector.JavaScanner;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.google.common.base.Preconditions;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.List;

import lombok.ast.Annotation;
import lombok.ast.AnnotationDeclaration;
import lombok.ast.AstVisitor;
import lombok.ast.ForwardingAstVisitor;
import lombok.ast.Node;
import lombok.ast.Select;

public class TypeDefDetector extends Detector implements JavaScanner {
  public static final Issue WRONG_RETENTION_POLICY = Issue.create(
      "WrongTypeDefRetentionPolicy",
      "Wrong or missing RetentionPolicy on @IntDef or @StringDef exposed by library",
      "The RetentionPolicy.SOURCE is required to correctly detect misuse " +
          "of methods annotated with this @IntDef and @StringDef.",
      Category.CORRECTNESS,
      1,
      Severity.ERROR,
      new Implementation(TypeDefDetector.class, Scope.JAVA_FILE_SCOPE));

  @Override
  public List<Class<? extends Node>> getApplicableNodeTypes() {
    return Collections.<Class<? extends Node>>singletonList(AnnotationDeclaration.class);
  }

  @Override
  public AstVisitor createJavaVisitor(final JavaContext context) {
    if (!context.getProject().isLibrary()) return null;

    return new ForwardingAstVisitor() {
      @Override
      public boolean visitAnnotationDeclaration(final AnnotationDeclaration node) {
        AnnotationDeclarationVisitor visitor = new AnnotationDeclarationVisitor(context);
        node.accept(visitor);
        if (visitor.reportWrongRetentionPolicy()) {
          context.report(
              WRONG_RETENTION_POLICY,
              context.getLocation(node),
              "Use @Retention(RetentionPolicy.SOURCE) for @IntDef and @StringDef annotations"
          );
        }

        return super.visitAnnotationDeclaration(node);
      }
    };
  }

  private static class AnnotationDeclarationVisitor extends ForwardingAstVisitor {
    private final JavaContext mContext;

    private boolean mIsTypeDef;
    private boolean mIsRetentionPolicyCorrect;

    private AnnotationDeclarationVisitor(JavaContext context) {
      mContext = context;
    }

    @Override
    public boolean visitAnnotation(Annotation annotationNode) {
      ResolvedNode resolve = mContext.resolve(annotationNode);

      String signature = resolve.getSignature();
      if (signature.equals(SdkConstants.INT_DEF_ANNOTATION) ||
          signature.equals(SdkConstants.STRING_DEF_ANNOTATION)) {
        mIsTypeDef = true;
      } else if (signature.equals(Retention.class.getName())) {
        List<Node> valueValues = annotationNode.getValueValues();
        Preconditions.checkArgument(valueValues.size() == 1);

        Node value = valueValues.get(0);
        value.accept(new ForwardingAstVisitor() {
          @Override
          public boolean visitSelect(Select node) {
            String retentionPolicy = node.astIdentifier().astValue();
            if (retentionPolicy.equals(RetentionPolicy.SOURCE.toString())) {
              mIsRetentionPolicyCorrect = true;
            }
            return super.visitSelect(node);
          }
        });
      }

      return super.visitAnnotation(annotationNode);
    }

    public boolean reportWrongRetentionPolicy() {
      return mIsTypeDef && !mIsRetentionPolicyCorrect;
    }
  }
}
