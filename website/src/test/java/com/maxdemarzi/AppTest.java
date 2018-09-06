package com.maxdemarzi;

import static io.restassured.RestAssured.get;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.fizzed.rocker.runtime.DefaultRockerModel;
import org.jooby.test.JoobyRule;
import org.jooby.test.MockRouter;
import org.junit.ClassRule;
import org.junit.Test;

public class AppTest {

  /**
   * One app/server for all the test of this class. If you want to start/stop a new server per test,
   * remove the static modifier and replace the {@link ClassRule} annotation with {@link Rule}.
   */
  @ClassRule
  public static JoobyRule app = new JoobyRule(new App());

  /**
   * Simple test that hits the default route.
   */
  @Test
  public void indexPageIntegration() {
    get("/")
        .then()
        .assertThat()
        .statusCode(200)
        .contentType("text/html;charset=UTF-8")
        .content("html.head.title", equalTo("Fives"));
  }

  @Test
  public void indexPage() throws Throwable {
    DefaultRockerModel index = new MockRouter(new App()).get("/");
    assertNotNull(index);
  }

}
