package org.opentripplanner.routing.edgetype;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.opentripplanner.transit.model.basic.WheelchairAccessibility.NOT_POSSIBLE;
import static org.opentripplanner.transit.model.basic.WheelchairAccessibility.NO_INFORMATION;
import static org.opentripplanner.transit.model.basic.WheelchairAccessibility.POSSIBLE;

import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opentripplanner.routing.api.request.RouteRequest;
import org.opentripplanner.routing.api.request.preference.AccessibilityPreferences;
import org.opentripplanner.routing.api.request.preference.WheelchairPreferences;
import org.opentripplanner.routing.core.RoutingContext;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.vertextype.SimpleVertex;
import org.opentripplanner.routing.vertextype.TransitStopVertexBuilder;
import org.opentripplanner.transit.model._data.TransitModelForTest;
import org.opentripplanner.transit.model.basic.TransitMode;
import org.opentripplanner.transit.model.framework.Deduplicator;
import org.opentripplanner.transit.model.site.RegularStop;
import org.opentripplanner.transit.service.StopModel;
import org.opentripplanner.transit.service.TransitModel;

class StreetTransitEntityLinkTest {

  private static Graph graph;
  private static TransitModel transitModel;

  RegularStop inaccessibleStop = TransitModelForTest.stopForTest(
    "A:inaccessible",
    "wheelchair inaccessible stop",
    10.001,
    10.001,
    null,
    NOT_POSSIBLE
  );
  RegularStop accessibleStop = TransitModelForTest.stopForTest(
    "A:accessible",
    "wheelchair accessible stop",
    10.001,
    10.001,
    null,
    POSSIBLE
  );

  RegularStop unknownStop = TransitModelForTest.stopForTest(
    "A:unknown",
    "unknown",
    10.001,
    10.001,
    null,
    NO_INFORMATION
  );

  @BeforeAll
  static void setup() {
    var deduplicator = new Deduplicator();
    graph = new Graph(deduplicator);
    transitModel = new TransitModel(new StopModel(), deduplicator);
  }

  @Test
  void disallowInaccessibleStop() {
    var afterTraversal = traverse(inaccessibleStop, true);
    assertNull(afterTraversal);
  }

  @Test
  void allowAccessibleStop() {
    State afterTraversal = traverse(accessibleStop, true);

    assertNotNull(afterTraversal);
  }

  @Test
  void unknownStop() {
    var afterTraversal = traverse(unknownStop, false);
    assertNotNull(afterTraversal);

    var afterStrictTraversal = traverse(unknownStop, true);
    assertNull(afterStrictTraversal);
  }

  private State traverse(RegularStop stop, boolean onlyAccessible) {
    var from = new SimpleVertex(graph, "A", 10, 10);
    var to = new TransitStopVertexBuilder()
      .withGraph(graph)
      .withStop(stop)
      .withModes(Set.of(TransitMode.RAIL))
      .build();

    var req = new RouteRequest();
    AccessibilityPreferences feature;
    if (onlyAccessible) {
      feature = AccessibilityPreferences.ofOnlyAccessible();
    } else {
      feature = AccessibilityPreferences.ofCost(100, 100);
    }
    req.setWheelchair(true);
    req
      .preferences()
      .setWheelchair(new WheelchairPreferences(feature, feature, feature, 25, 8, 10, 25));

    var ctx = new RoutingContext(req, graph, from, to);
    var state = new State(ctx);

    var edge = new StreetTransitStopLink(from, to);

    return edge.traverse(state);
  }
}
