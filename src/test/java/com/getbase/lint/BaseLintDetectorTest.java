package com.getbase.lint;

import com.android.tools.lint.checks.infrastructure.LintDetectorTest;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Issue;
import com.getbase.lint.issues.EnumDetector;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public abstract class BaseLintDetectorTest extends LintDetectorTest {
  @Override
  protected InputStream getTestResource(String relativePath, boolean expectExists) {
    InputStream stream = getClass().getClassLoader().getResourceAsStream(relativePath);
    if (!expectExists && stream == null) {
      return null;
    }
    return stream;
  }

  protected String getExpectedError(String relativePath) throws IOException {
    URL resource = getClass().getClassLoader().getResource(relativePath);

    assertNotNull(resource);

    return Resources.toString(resource, Charsets.UTF_8);
  }
}
