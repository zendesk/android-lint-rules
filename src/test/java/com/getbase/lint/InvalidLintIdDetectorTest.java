package com.getbase.lint;

import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Issue;
import com.getbase.lint.issues.InvalidLintIdDetector;
import com.google.common.collect.ImmutableList;

import java.util.List;

public class InvalidLintIdDetectorTest extends BaseLintDetectorTest {
  @Override
  protected Detector getDetector() {
    return new InvalidLintIdDetector();
  }

  @Override
  protected List<Issue> getIssues() {
    return ImmutableList.of(InvalidLintIdDetector.ISSUE);
  }

  public void testSingleGradleFile() throws Exception {
    test("invalid_lint_id_single_gradle_file");
  }

  private void test(String resourceDirectory) throws Exception {
    assertEquals(getExpectedError(resourceDirectory + "/expected"), lintFiles(resourceDirectory + "/build.gradle"));
  }
}
