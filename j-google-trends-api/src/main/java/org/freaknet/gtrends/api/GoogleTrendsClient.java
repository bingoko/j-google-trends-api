/**
 * Copyright (C) 2013 Marco Tizzoni <marco.tizzoni@gmail.com>
 *
 * This file is part of j-google-trends-api
 *
 *     j-google-trends-api is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     j-google-trends-api is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with j-google-trends-api.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freaknet.gtrends.api;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.freaknet.gtrends.api.exceptions.GoogleAuthenticatorException;
import org.freaknet.gtrends.api.exceptions.GoogleTrendsClientException;
import org.freaknet.gtrends.api.exceptions.GoogleTrendsRequestException;

/**
 * Implements a client for Google Trends https://www.google.com/trends/ .
 *
 * @author Marco Tizzoni <marco.tizzoni@gmail.com>
 */
public class GoogleTrendsClient {

  private final GoogleAuthenticator authenticator;
  private final HttpClient client;

  /**
   *
   * @param authenticator
   * @param client
   */
  public GoogleTrendsClient(GoogleAuthenticator authenticator, HttpClient client) {
    this.authenticator = authenticator;
    this.client = client;
  }

  /**
   * Execute the request.
   *
   * @param request
   * @return content The content of the response
   * @throws GoogleTrendsClientException
   */
  public String execute(GoogleTrendsRequest request) throws GoogleTrendsClientException {
    String html = null;
    try {
      if (!authenticator.isLoggedIn()) {
        authenticator.authenticate();
      }
      Logger.getLogger(GoogleConfigurator.getLoggerPrefix()).log(Level.FINE, "Query: {0}", request.build().toString());

      HttpRequestBase httpRequest = request.build();
      HttpResponse response = client.execute(httpRequest);
      html = GoogleUtils.toString(response.getEntity().getContent());
      httpRequest.releaseConnection();

      Pattern p = Pattern.compile(GoogleConfigurator.getConfiguration().getString("google.trends.client.reError"), Pattern.CASE_INSENSITIVE);
      Matcher matcher = p.matcher(html);
      if (matcher.find()) {
        throw new GoogleTrendsClientException("*** You are running too fast man! Looks like you reached your quota limit. Wait a while and slow it down with the '-S' option! *** ");
      }
    } catch (GoogleAuthenticatorException ex) {
      throw new GoogleTrendsClientException(ex);
    } catch (ClientProtocolException ex) {
      throw new GoogleTrendsClientException(ex);
    } catch (IOException ex) {
      throw new GoogleTrendsClientException(ex);
    } catch (ConfigurationException ex) {
      throw new GoogleTrendsClientException(ex);
    } catch (GoogleTrendsRequestException ex) {
      throw new GoogleTrendsClientException(ex);
    }

    return html;
  }
}
