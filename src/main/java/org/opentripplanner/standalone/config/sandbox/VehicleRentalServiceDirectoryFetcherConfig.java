package org.opentripplanner.standalone.config.sandbox;

import static org.opentripplanner.standalone.config.framework.json.OtpVersion.V2_0;
import static org.opentripplanner.standalone.config.framework.json.OtpVersion.V2_1;
import static org.opentripplanner.standalone.config.framework.json.OtpVersion.V2_4;

import java.util.List;
import org.opentripplanner.ext.vehiclerentalservicedirectory.api.NetworkParameters;
import org.opentripplanner.ext.vehiclerentalservicedirectory.api.VehicleRentalServiceDirectoryFetcherParameters;
import org.opentripplanner.standalone.config.framework.json.NodeAdapter;
import org.opentripplanner.standalone.config.routerconfig.updaters.HttpHeadersConfig;

public class VehicleRentalServiceDirectoryFetcherConfig {

  public static VehicleRentalServiceDirectoryFetcherParameters create(
    String parameterName,
    NodeAdapter root
  ) {
    var c = root
      .of(parameterName)
      .since(V2_0)
      .summary("Configuration for the vehicle rental service directory.")
      .asObject();

    if (c.isEmpty()) {
      return null;
    }

    return new VehicleRentalServiceDirectoryFetcherParameters(
      c.of("url").since(V2_1).summary("Endpoint for the VehicleRentalServiceDirectory").asUri(),
      c
        .of("sourcesName")
        .since(V2_1)
        .summary("Json tag name for updater sources.")
        .asString("systems"),
      c
        .of("updaterUrlName")
        .since(V2_1)
        .summary("Json tag name for endpoint urls for each source.")
        .asString("url"),
      c
        .of("updaterNetworkName")
        .since(V2_1)
        .summary("Json tag name for the network name for each source.")
        .asString("id"),
      c.of("language").since(V2_1).summary("Language code.").asString(null),
      HttpHeadersConfig.headers(c, V2_1),
      mapNetworkParameters("networks", c)
    );
  }

  private static List<NetworkParameters> mapNetworkParameters(
    String parameterName,
    NodeAdapter root
  ) {
    return root
      .of(parameterName)
      .since(V2_4)
      .summary("List all networks to include.")
      .asObjects(c ->
        new NetworkParameters(
          c.of("network").since(V2_4).summary("The network name").asString(),
          c
            .of("geofencingZones")
            .since(V2_4)
            .summary("Enables geofencingZones for the given network")
            .asBoolean(false)
        )
      );
  }
}
