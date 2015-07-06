package com.getbase.lint.issues;

import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Context;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;

import java.util.Calendar;
import java.util.EnumSet;

public class AncientMinSdkDetector extends Detector {

  private static final int SUGGESTED_MIN_SDK_VERSION = 15;
  private static final int YEAR = Calendar.getInstance().get(Calendar.YEAR);

  public static final Issue ISSUE = Issue.create(
      "AncientMinSdk",
      "Supporting ancient Android versions",
      "It's " + YEAR + ", time to bump your minSdkVersion to " + SUGGESTED_MIN_SDK_VERSION,
      Category.CORRECTNESS,
      10,
      Severity.FATAL,
      new Implementation(AncientMinSdkDetector.class, EnumSet.noneOf(Scope.class)));

  @Override
  public void afterCheckProject(Context context) {
    super.afterCheckProject(context);

    int minSdk = context.getProject().getMinSdk();

    if (minSdk != -1 && minSdk < SUGGESTED_MIN_SDK_VERSION) {
      context.report(ISSUE, null, "Ancient minSdkVersion detected");
    }
  }
}
