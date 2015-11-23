package com.getbase.lint.issues;

import static com.google.common.base.Preconditions.checkNotNull;

import com.android.annotations.NonNull;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Context;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Detector.GradleScanner;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.getbase.lint.utils.GradleUtils;
import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.builder.AstBuilder;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class InvalidLintIdDetector extends Detector implements GradleScanner {
  public static final Issue ISSUE = Issue.create(
      "InvalidLintId",
      "Referencing non-existing lint issue id in your config",
      "Configuration references lint rule which doesn't exist, probably because of typo",
      Category.CORRECTNESS,
      10,
      Severity.FATAL,
      new Implementation(
          InvalidLintIdDetector.class,
          Scope.GRADLE_SCOPE
      )
  );

  @Override
  public boolean appliesTo(@NonNull Context context, @NonNull File file) {
    return true;
  }

  @Override
  public void visitBuildScript(@NonNull Context context, Map<String, Object> sharedData) {
    BuildScriptVisitor visitor = new BuildScriptVisitor();

    String buildScriptSource = checkNotNull(context.getContents());

    Iterables
        .getOnlyElement(new AstBuilder().buildFromString(buildScriptSource))
        .visit(visitor);

    ImmutableSet<String> issueIds = FluentIterable
        .from(context.getDriver().getRegistry().getIssues())
        .transform(new Function<Issue, String>() {
          @Override
          public String apply(Issue input) {
            return input.getId();
          }
        })
        .toSet();

    for (Expression issueIdExpression : visitor.mIssueIdsExpressions) {
      if (!issueIds.contains(issueIdExpression.getText())) {
        context.report(
            ISSUE,
            GradleUtils.createLocation(context, issueIdExpression),
            String.format(
                "Unknown lint issue id: %1$s",
                issueIdExpression.getText()
            )
        );
      }
    }
  }

  private static class BuildScriptVisitor extends CodeVisitorSupport {
    private final Stack<String> mScopeStack = new Stack<>();

    public final List<Expression> mIssueIdsExpressions = new ArrayList<>();

    @Override
    public void visitMethodCallExpression(MethodCallExpression call) {
      int stackSize = mScopeStack.size();

      call.getObjectExpression().visit(this);
      call.getMethod().visit(this);

      if (parseLintIssuesId()) {
        if (call.getArguments() instanceof ArgumentListExpression) {
          extractConstantExpressions((ArgumentListExpression) call.getArguments()).copyInto(mIssueIdsExpressions);
        }
      } else {
        call.getArguments().visit(this);
      }

      mScopeStack.setSize(stackSize);
    }

    private FluentIterable<Expression> extractConstantExpressions(ArgumentListExpression expressions) {
      return FluentIterable
          .from(expressions.getExpressions())
          .filter(Predicates.instanceOf(ConstantExpression.class));
    }

    @Override
    public void visitTupleExpression(TupleExpression expression) {
      super.visitTupleExpression(expression);
    }

    private static final ImmutableList<String> LINT_OPTIONS_SCOPE = ImmutableList.of("android", "lintOptions");
    private static final ImmutableSet<String> ISSUE_CONFIG_METHODS = ImmutableSet.of(
        "check",
        "disable",
        "enable",
        "error",
        "fatal",
        "ignore",
        "warning"
    );

    private boolean parseLintIssuesId() {
      return ISSUE_CONFIG_METHODS.contains(mScopeStack.peek()) && scopeStackInitMatches(LINT_OPTIONS_SCOPE);
    }

    private boolean scopeStackInitMatches(Iterable<String> expectedMatch) {
      Iterable<String> init = Iterables.limit(mScopeStack, mScopeStack.size() - 1);
      return Iterables.elementsEqual(init, expectedMatch);
    }

    @Override
    public void visitVariableExpression(VariableExpression expression) {
      updateScope(expression);
    }

    @Override
    public void visitConstantExpression(ConstantExpression expression) {
      updateScope(expression);
    }

    private void updateScope(Expression expression) {
      String text = expression.getText();
      if (!text.equals("this")) {
        mScopeStack.push(text);
      }
    }
  }
}
