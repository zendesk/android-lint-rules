package com.getbase.lint;

import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Issue;
import com.getbase.lint.issues.EnumDetector;
import com.google.common.collect.ImmutableList;

import java.util.List;

public class EnumDetectorTest extends BaseLintDetectorTest {
  @Override
  protected Detector getDetector() {
    return new EnumDetector();
  }

  @Override
  protected List<Issue> getIssues() {
    return ImmutableList.of(EnumDetector.ENUMS_ARE_BAD_ISSUE);
  }

  public void testHappyPath() throws Exception {
    test("enum");
  }

  private void test(String resourceDirectory) throws Exception {
    assertEquals(getExpectedError(resourceDirectory + "/expected"), lintFiles(resourceDirectory + "/TheEnum.java"));
  }
}
