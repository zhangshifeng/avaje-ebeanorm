package com.avaje.tests.json.include;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.config.JsonConfig;
import com.avaje.ebean.text.json.JsonWriteOptions;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.OrderDetail;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class TestJsonExcludeEmptyList {

  @Test
  public void testToBeanToJson_NonNull() throws Exception {

    Order bean = new Order();
    bean.setId(99);
    bean.setStatus(null);
    bean.setOrderDate(null);
    bean.setDetails(new ArrayList<OrderDetail>());

    JsonWriteOptions options = new JsonWriteOptions();
    options.setInclude(JsonConfig.Include.NON_NULL);

    String asJson = Ebean.json().toJson(bean, options);

    String expectedJson = "{\"id\":99,\"details\":[]}";

    assertEquals(expectedJson, asJson);
  }

  @Test
  public void testToBeanToJson_NonEmpty() throws Exception {

    Order bean = new Order();
    bean.setId(99);
    bean.setStatus(null);
    bean.setOrderDate(null);
    bean.setDetails(new ArrayList<OrderDetail>());

    JsonWriteOptions options = new JsonWriteOptions();
    options.setInclude(JsonConfig.Include.NON_EMPTY);

    String asJson = Ebean.json().toJson(bean, options);

    String expectedJson = "{\"id\":99}";

    assertEquals(expectedJson, asJson);
  }
}
