package com.getbase.lint;

import com.android.tools.lint.client.api.IssueRegistry;
import com.android.tools.lint.detector.api.Issue;
import com.getbase.lint.issues.AncientMinSdkDetector;
import com.getbase.lint.issues.MicroOrmDetector;

import java.util.Arrays;
import java.util.List;

public class CustomIssueRegistry extends IssueRegistry {
  @Override
  public List<Issue> getIssues() {
    return Arrays.asList(
        AncientMinSdkDetector.ISSUE,
        MicroOrmDetector.NO_DEFAULT_CONSTRUCTOR_ISSUE
    );
  }
}
