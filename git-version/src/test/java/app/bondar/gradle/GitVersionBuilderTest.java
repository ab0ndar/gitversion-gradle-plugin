package app.bondar.gradle;

import static org.testng.Assert.*;
import static org.mockito.Mockito.when;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class GitVersionBuilderTest {
  @Mock
  private GitVersionBuilder builder;

  @DataProvider(name = "git-version-good-data-provider")
  public Object[][] dpGitVersionGood(){
    return new Object[][] {
        {"main", "1.0.2-2-g85766c1-SNAPSHOT", "1.0.2"},
        {"main", "v1.0.2-2-g85766c1-SNAPSHOT", "1.0.2"},
        {"main", "v1.0.2-2-g85766c1", "1.0.2"},
        {"main", "1.0.2-2-g85766c1", "1.0.2"},
        {"develop", "v1.0.2-2-g85766c1-SNAPSHOT", "1.0.2-2-g85766c1-SNAPSHOT"},
        {"develop", "v1.0.2-2-g85766c1", "1.0.2-2-g85766c1"},
        {"develop", "1.0.2-2-g85766c1-SNAPSHOT", "1.0.2-2-g85766c1-SNAPSHOT"},
        {"feature/ticket-2010", "v1.0.2-2-g85766c1-SNAPSHOT", "1.0.2-2-g85766c1-SNAPSHOT"}
    };
  }

  @DataProvider(name = "git-version-bad-data-provider")
  public Object[][] dpGitVersionBad(){
    return new Object[][] {
        {"main", "abrakadabra", GitVersionException.class},
        {"main", "as.d.d-f-gjj", GitVersionException.class}
    };
  }

  @BeforeMethod
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test (dataProvider = "git-version-good-data-provider")
  public void testGetVersionGood(String branch, String describe, String ver) {
    when(builder.getBranchName()).thenReturn(branch);
    when(builder.describe()).thenReturn(describe);
    when(builder.getVersion()).thenCallRealMethod();
    assertEquals(builder.getVersion(), ver);
  }

  @Test (dataProvider = "git-version-bad-data-provider")
  public void testGetVersionBad(String branch, String describe, Class exception) {
    when(builder.getBranchName()).thenReturn(branch);
    when(builder.describe()).thenReturn(describe);
    when(builder.getVersion()).thenCallRealMethod();
    try {
      builder.getVersion();
      fail(String.format("Expected exception %s was not thrown", exception.getName()));
    } catch (Exception e) {
      if (!exception.isInstance(e)) {
        fail(String.format("Wrong exception %s was thrown", e.getClass().getName()));
      }
    }
  }
}