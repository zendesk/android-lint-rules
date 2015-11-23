package com.getbase.lint.utils;

import com.android.annotations.NonNull;
import com.android.tools.lint.detector.api.Context;
import com.android.tools.lint.detector.api.DefaultPosition;
import com.android.tools.lint.detector.api.Location;
import com.android.utils.Pair;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.TupleExpression;

import java.util.List;

public final class GradleUtils {
  private GradleUtils() {
  }

  @NonNull
  private static Pair<Integer, Integer> getOffsets(ASTNode node, Context context) {
    if (node.getLastLineNumber() == -1 && node instanceof TupleExpression) {
      // Workaround: TupleExpressions yield bogus offsets, so use its
      // children instead
      TupleExpression exp = (TupleExpression) node;
      List<Expression> expressions = exp.getExpressions();
      if (!expressions.isEmpty()) {
        return Pair.of(
            getOffsets(expressions.get(0), context).getFirst(),
            getOffsets(expressions.get(expressions.size() - 1), context).getSecond());
      }
    }
    String source = context.getContents();
    assert source != null; // because we successfully parsed
    int start = 0;
    int end = source.length();
    int line = 1;
    int startLine = node.getLineNumber();
    int startColumn = node.getColumnNumber();
    int endLine = node.getLastLineNumber();
    int endColumn = node.getLastColumnNumber();
    int column = 1;
    for (int index = 0, len = end; index < len; index++) {
      if (line == startLine && column == startColumn) {
        start = index;
      }
      if (line == endLine && column == endColumn) {
        end = index;
        break;
      }

      char c = source.charAt(index);
      if (c == '\n') {
        line++;
        column = 1;
      } else {
        column++;
      }
    }

    return Pair.of(start, end);
  }

  public static Location createLocation(@NonNull Context context, @NonNull ASTNode node) {
    Pair<Integer, Integer> offsets = getOffsets(node, context);
    int fromLine = node.getLineNumber() - 1;
    int fromColumn = node.getColumnNumber() - 1;
    int toLine = node.getLastLineNumber() - 1;
    int toColumn = node.getLastColumnNumber() - 1;
    return Location.create(context.file,
        new DefaultPosition(fromLine, fromColumn, offsets.getFirst()),
        new DefaultPosition(toLine, toColumn, offsets.getSecond()));
  }
}
