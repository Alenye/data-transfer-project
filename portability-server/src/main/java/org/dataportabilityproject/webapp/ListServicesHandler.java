package org.dataportabilityproject.webapp;

import static org.apache.axis.transport.http.HTTPConstants.HEADER_CONTENT_TYPE;

import com.google.common.base.Enums;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.json.Json;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.dataportabilityproject.ServiceProviderRegistry;
import org.dataportabilityproject.shared.PortableDataType;
import org.dataportabilityproject.webapp.JsonKeys;

public class ListServicesHandler implements HttpHandler {
  private ServiceProviderRegistry serviceProviderRegistry;

  public ListServicesHandler(ServiceProviderRegistry serviceProviderRegistry) {
    this.serviceProviderRegistry = serviceProviderRegistry;
  }

  public void handle(HttpExchange exchange) throws IOException {
    // TODO: check method set on HttpExchange, this should only support GET
    // Set response as type json
    Headers headers = exchange.getResponseHeaders();
    headers.set(HEADER_CONTENT_TYPE, "application/json; charset="+ StandardCharsets.UTF_8.name());

    // grab the dataType from the request parameter
    URIBuilder builder = new URIBuilder(exchange.getRequestURI());
    List<NameValuePair> queryParamPairs= builder.getQueryParams();
    Map<String, String> params = new HashMap<String, String>();
    for(NameValuePair pair : queryParamPairs){
      params.put(pair.getName(), pair.getValue());
    }
    String dataTypeParam = params.get(JsonKeys.DATA_TYPE);
    Preconditions.checkArgument(!Strings.isNullOrEmpty(dataTypeParam), "Missing data type");

    // TODO: use LogUtils.log instead of system out
    System.out.println("ListServicesHandler: using data type param: "+  dataTypeParam);

    // Validate incoming data type parameter
    PortableDataType dataType = getDataType(dataTypeParam);

    List<String> exportServices = new ArrayList<String>();
    List<String> importServices = new ArrayList<String>();

    try {
      exportServices = serviceProviderRegistry.getServiceProvidersThatCanExport(dataType);
      importServices = serviceProviderRegistry.getServiceProvidersThatCanImport(dataType);
    } catch (Exception e) {
      System.err.println("Encountered error with getServiceProviders...() " + e);
    }

    if (exportServices.isEmpty() || importServices.isEmpty()) {
      System.err.println(
          "Empty service list found, export size: " + exportServices.size() + ", import size: "
              + importServices.size());
    }

    // Construct Json.
    // TODO: use Json library and instead of manual construction
    String exportString= String.join(", ", exportServices);
    String importString = String.join(", ", importServices);
    String response = JsonKeys.EXPORT + ": { " + exportString + " } " + JsonKeys.IMPORT + ": { " + importString + " }";
    System.out.println("Response is: " + response);

    // Send response
    exchange.sendResponseHeaders(200, 0);
    OutputStream responseBody = exchange.getResponseBody();
    responseBody.write(response.getBytes());
    responseBody.close();
  }

  /**
   * Parse and validate the data type .
   */
  private PortableDataType getDataType(String dataType) {
    Optional<PortableDataType> dataTypeOption = Enums
        .getIfPresent(PortableDataType.class, dataType);
    Preconditions.checkState(dataTypeOption.isPresent(), "Data type not found: %s", dataType);
    return dataTypeOption.get();
  }
}
