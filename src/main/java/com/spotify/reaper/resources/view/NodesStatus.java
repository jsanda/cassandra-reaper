/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.spotify.reaper.resources.view;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

import jersey.repackaged.com.google.common.collect.Lists;

import java.util.Collection;
import java.util.regex.*;

public class NodesStatus {
  @JsonProperty
  public final Collection<GossipInfo> endpointStates;
  private static Pattern endpointNamePattern = Pattern.compile("^([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3})", Pattern.MULTILINE);
  private static Pattern endpointStatusPattern = Pattern.compile("(STATUS):([0-9]*):(\\w+)");
  private static Pattern endpointDcPattern = Pattern.compile("(DC):([0-9]*):(\\w+)");
  private static Pattern endpointRackPattern = Pattern.compile("(RACK):([0-9]*):(\\w+)");
  private static Pattern endpointLoadPattern = Pattern.compile("(LOAD):([0-9]*):([0-9.]+)");
  private static Pattern endpointReleasePattern = Pattern.compile("(RELEASE_VERSION):([0-9]*):([0-9.]+)");
  private static Pattern endpointSeverityPattern = Pattern.compile("(SEVERITY):([0-9]*):([0-9.]+)");
  private static Pattern endpointHostIdPattern = Pattern.compile("(HOST_ID):([0-9]*):([0-9a-z-]+)");
  private static Pattern endpointTokensPattern = Pattern.compile("(TOKENS):([0-9]*)");
  private Matcher matcher;
  
  public NodesStatus(Collection<GossipInfo> endpointStates) {
    this.endpointStates = endpointStates;
  }
  
  public NodesStatus(String sourceNode, String allEndpointStates) {
    this.endpointStates = Lists.newArrayList();
    this.endpointStates.add(parseEndpointStatesString(sourceNode, allEndpointStates)); 
  }

  private GossipInfo parseEndpointStatesString(String sourceNode, String allEndpointStates) {
    Collection<EndpointState> endpoints = Lists.newArrayList();
    
    String[] strEndpoints = allEndpointStates.split("/");
    
    for(int i=1;i<strEndpoints.length;i++){
      Optional<String> endpoint = Optional.absent();
      Optional<String> hostId = Optional.absent();
      Optional<String> dc = Optional.absent();
      Optional<String> rack = Optional.absent();
      Optional<String> status = Optional.absent();
      Optional<Double> severity = Optional.absent();
      Optional<String> releaseVersion = Optional.absent();
      Optional<String> tokens = Optional.absent();
      Optional<Double> load = Optional.absent();
      
      String endpointString = strEndpoints[i];
      
      matcher = endpointNamePattern.matcher(endpointString);
      if(matcher.find()) {
        endpoint = Optional.fromNullable(matcher.group(1));
      }
      
      matcher = endpointStatusPattern.matcher(endpointString);
      if(matcher.find()) {
        status = Optional.fromNullable(matcher.group(3));
      }
      
      matcher = endpointDcPattern.matcher(endpointString);
      if(matcher.find()) {
        dc = Optional.fromNullable(matcher.group(3));
      }
      
      matcher = endpointRackPattern.matcher(endpointString);
      if(matcher.find()) {
        rack = Optional.fromNullable(matcher.group(3));
      }
      
      matcher = endpointSeverityPattern.matcher(endpointString);
      if(matcher.find()) {
        severity = Optional.fromNullable(Double.parseDouble(matcher.group(3)));
      }
      
      matcher = endpointLoadPattern.matcher(endpointString);
      if(matcher.find()) {
        load = Optional.fromNullable(Double.parseDouble(matcher.group(3)));
      }
      
      matcher = endpointReleasePattern.matcher(endpointString);
      if(matcher.find()) {
        releaseVersion = Optional.fromNullable(matcher.group(3));
      }
      
      matcher = endpointHostIdPattern.matcher(endpointString);
      if(matcher.find()) {
        hostId = Optional.fromNullable(matcher.group(3));
      }
      
      matcher = endpointTokensPattern.matcher(endpointString);
      if(matcher.find()) {
        tokens = Optional.fromNullable(matcher.group(2));
      }
      
      
      
      EndpointState endpointState = new EndpointState(endpoint.or("Not available"), hostId.or("Not available"), dc.or("Not available"), rack.or("Not available"), status.or("Not available"), severity.or(0.0),
          releaseVersion.or("Not available"), tokens.or("Not available"), load.or(0.0));
      
      endpoints.add(endpointState);
    }
    
    GossipInfo gossipInfo = new GossipInfo(sourceNode ,endpoints);
    
    return gossipInfo;
  }

  public class GossipInfo {
    @JsonProperty
    public final String sourceNode;
    @JsonProperty
    public final Collection<EndpointState> endpoints;
    
    public GossipInfo(String sourceNode, Collection<EndpointState> endpoints) {
      this.sourceNode = sourceNode;
      this.endpoints = endpoints;
    } 
  }
  
  public class EndpointState {
    @JsonProperty
    public final String endpoint;
    @JsonProperty
    public final String hostId;
    @JsonProperty
    public final String dc;
    @JsonProperty
    public final String rack;
    @JsonProperty
    public final String status;
    @JsonProperty
    public final Double severity;
    @JsonProperty
    public final String releaseVersion;
    @JsonProperty
    public final String tokens;
    @JsonProperty
    public final Double load;
    
    public EndpointState(String endpoint, String hostId, String dc, String rack, String status, Double severity,
        String releaseVersion, String tokens, Double load) {
      this.endpoint = endpoint;
      this.hostId = hostId;
      this.dc = dc;
      this.rack = rack;
      this.status = status;
      this.severity = severity;
      this.releaseVersion = releaseVersion;
      this.tokens = tokens;
      this.load = load;
   }
    
   public String toString() {
     return "Endpoint : " + endpoint + " / " 
           + "Status : " + status + " / " 
           + "DC : " + dc + " / " 
           + "Rack : " + rack + " / " 
           + "Release version : " + releaseVersion + " / " 
           + "Load : " + load + " / " 
           + "Severity : " + severity + " / " 
           + "Host Id : " + hostId + " / " 
           + "Tokens : " + tokens;
   }
  } 
}
