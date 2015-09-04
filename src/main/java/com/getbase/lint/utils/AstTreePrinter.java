package com.getbase.lint.utils;

import lombok.ast.ForwardingAstVisitor;
import lombok.ast.Node;

public class AstTreePrinter extends ForwardingAstVisitor {
  private int mIndentLevel = 0;

  private String getIndent() {
    return mIndentLevel > 0
        ? String.format("%1$" + 4*mIndentLevel + "s", "")
        : "";
  }

  @Override
  public boolean visitNode(Node node) {
    System.out.println(String.format("%1$s%2$s: %3$s", getIndent(), node.getClass(), node));
    mIndentLevel += 1;
    return super.visitNode(node);
  }

  @Override
  public void endVisit(Node node) {
    mIndentLevel -= 1;
    super.endVisit(node);
  }
}
