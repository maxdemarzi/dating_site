package com.maxdemarzi.routes;

import com.google.common.collect.Lists;
import com.maxdemarzi.API;
import com.maxdemarzi.App;
import com.maxdemarzi.Neo4jApi;
import com.maxdemarzi.models.City;
import org.jooby.Mutant;
import org.jooby.Request;
import org.jooby.json.Jackson;
import org.jooby.test.JoobyRule;
import org.jooby.test.MockRouter;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import retrofit2.Call;
import retrofit2.Response;

import java.util.List;

import static io.restassured.RestAssured.get;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AutoCompletesTest {
  @ClassRule
  public static JoobyRule app = new JoobyRule(new AutoCompletes()
      .use(new Jackson())
      .use(new Neo4jApi())
  );

  @Test
  public void autoCompleteCity() {
    get("/autocomplete/city/Chicago")
        .then()
        .assertThat()
        .statusCode(200)
        .contentType("application/json;charset=UTF-8")
        .body("id", hasItems(89603, 89605, 89601));
  }

  @Test
  public void autoCompleteCityTest() throws Throwable {
    String q = "Chicago";
    Mutant query = mock(Mutant.class);
    when(query.value()).thenReturn(q);

    Request req = mock(Request.class);
    when(req.param("query")).thenReturn(query);

    City city = mock(City.class);

    List<City> cities = Lists.newArrayList(city);
    Response<List<City>> response = Response.success(cities);

    Call<List<City>> call = mock(Call.class);
    when(call.execute()).thenReturn(response);

    API api = mock(API.class);
    when(api.autoCompleteCity(q.toLowerCase(), "full_name")).thenReturn(call);

    List<City> result = new MockRouter(new AutoCompletes(), req)
        .set(api)
        .get("/autocomplete/city/Chicago");
    assertEquals(cities, result);
  }
}
