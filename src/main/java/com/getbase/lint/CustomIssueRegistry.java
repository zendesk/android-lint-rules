package com.getbase.lint;

import com.android.tools.lint.client.api.IssueRegistry;
import com.android.tools.lint.detector.api.Issue;
import com.getbase.lint.issues.AncientMinSdkDetector;
import com.getbase.lint.issues.CursorWrapperDetector;
import com.getbase.lint.issues.EnumDetector;
import com.getbase.lint.issues.InvalidLintIdDetector;
import com.getbase.lint.issues.MicroOrmDetector;
import com.getbase.lint.issues.TypeDefDetector;

import java.util.Arrays;
import java.util.List;

public class CustomIssueRegistry extends IssueRegistry {
  @Override
  public List<Issue> getIssues() {
    return Arrays.asList(
        AncientMinSdkDetector.ISSUE,
        MicroOrmDetector.NO_PUBLIC_DEFAULT_CONSTRUCTOR_ISSUE,
        EnumDetector.ENUMS_ARE_BAD_ISSUE,
        TypeDefDetector.WRONG_RETENTION_POLICY,
        CursorWrapperDetector.USE_CROSS_PROCESS_CURSOR_WRAPPERS,
        InvalidLintIdDetector.ISSUE
    );
  }
}
