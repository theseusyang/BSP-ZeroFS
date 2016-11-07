/**
 * Copyright 2016 Bloom Corp. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package com.bloom.zerofs.tools.admin;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import com.bloom.zerofs.api.clustermap.ClusterMap;
import com.bloom.zerofs.api.clustermap.ReplicaId;
import com.bloom.zerofs.api.config.ClusterMapConfig;
import com.bloom.zerofs.api.config.ConnectionPoolConfig;
import com.bloom.zerofs.api.config.SSLConfig;
import com.bloom.zerofs.api.config.VerifiableProperties;
import com.bloom.zerofs.api.messageformat.BlobProperties;
import com.bloom.zerofs.api.messageformat.MessageFormatFlags;
import com.bloom.zerofs.api.network.ConnectedChannel;
import com.bloom.zerofs.api.network.ConnectionPool;
import com.bloom.zerofs.api.network.ConnectionPoolTimeoutException;
import com.bloom.zerofs.api.network.Port;
import com.bloom.zerofs.clustermap.ClusterMapManager;
import com.bloom.zerofs.commons.BlobId;
import com.bloom.zerofs.commons.ServerErrorCode;
import com.bloom.zerofs.messageformat.BlobData;
import com.bloom.zerofs.messageformat.MessageFormatException;
import com.bloom.zerofs.messageformat.MessageFormatRecord;
import com.bloom.zerofs.network.BlockingChannelConnectionPool;
import com.bloom.zerofs.protocol.GetOptions;
import com.bloom.zerofs.protocol.GetRequest;
import com.bloom.zerofs.protocol.GetResponse;
import com.bloom.zerofs.protocol.PartitionRequestInfo;
import com.bloom.zerofs.tools.Utils;
import com.bloom.zerofs.tools.util.ToolUtils;
import com.codahale.metrics.MetricRegistry;


/**
 * Tool to support admin related operations
 * Operations supported so far:
 * List Replicas for a given blobid
 */
public class AdminTool {
  private final ConnectionPool connectionPool;
  private final ArrayList<String> sslEnabledDatacentersList;

  public AdminTool(ConnectionPool connectionPool, ArrayList<String> sslEnabledDatacentersList) {
    this.connectionPool = connectionPool;
    this.sslEnabledDatacentersList = sslEnabledDatacentersList;
  }

  public static void main(String args[]) {
    ConnectionPool connectionPool = null;
    try {
      OptionParser parser = new OptionParser();

      ArgumentAcceptingOptionSpec<String> hardwareLayoutOpt =
          parser.accepts("hardwareLayout", "The path of the hardware layout file").withRequiredArg()
              .describedAs("hardware_layout").ofType(String.class);

      ArgumentAcceptingOptionSpec<String> partitionLayoutOpt =
          parser.accepts("partitionLayout", "The path of the partition layout file").withRequiredArg()
              .describedAs("partition_layout").ofType(String.class);

      ArgumentAcceptingOptionSpec<String> typeOfOperationOpt = parser.accepts("typeOfOperation",
          "The type of operation to execute - LIST_REPLICAS/GET_BLOB/GET_BLOB_PROPERTIES/GET_USERMETADATA")
          .withRequiredArg().describedAs("The type of file").ofType(String.class).defaultsTo("GET");

      ArgumentAcceptingOptionSpec<String> AmberBlobIdOpt =
          parser.accepts("AmberBlobId", "The blob id to execute get on").withRequiredArg().describedAs("The blob id")
              .ofType(String.class);

      ArgumentAcceptingOptionSpec<String> includeExpiredBlobsOpt =
          parser.accepts("includeExpiredBlob", "Included expired blobs too").withRequiredArg()
              .describedAs("Whether to include expired blobs while querying or not").defaultsTo("false")
              .ofType(String.class);

      ArgumentAcceptingOptionSpec<String> sslEnabledDatacentersOpt =
          parser.accepts("sslEnabledDatacenters", "Datacenters to which ssl should be enabled").withOptionalArg()
              .describedAs("Comma separated list").defaultsTo("").ofType(String.class);

      ArgumentAcceptingOptionSpec<String> sslKeystorePathOpt =
          parser.accepts("sslKeystorePath", "SSL key store path").withOptionalArg()
              .describedAs("The file path of SSL key store").defaultsTo("").ofType(String.class);

      ArgumentAcceptingOptionSpec<String> sslKeystoreTypeOpt =
          parser.accepts("sslKeystoreType", "SSL key store type").withOptionalArg()
              .describedAs("The type of SSL key store").defaultsTo("").ofType(String.class);

      ArgumentAcceptingOptionSpec<String> sslTruststorePathOpt =
          parser.accepts("sslTruststorePath", "SSL trust store path").withOptionalArg()
              .describedAs("The file path of SSL trust store").defaultsTo("").ofType(String.class);

      ArgumentAcceptingOptionSpec<String> sslKeystorePasswordOpt =
          parser.accepts("sslKeystorePassword", "SSL key store password").withOptionalArg()
              .describedAs("The password of SSL key store").defaultsTo("").ofType(String.class);

      ArgumentAcceptingOptionSpec<String> sslKeyPasswordOpt =
          parser.accepts("sslKeyPassword", "SSL key password").withOptionalArg()
              .describedAs("The password of SSL private key").defaultsTo("").ofType(String.class);

      ArgumentAcceptingOptionSpec<String> sslTruststorePasswordOpt =
          parser.accepts("sslTruststorePassword", "SSL trust store password").withOptionalArg()
              .describedAs("The password of SSL trust store").defaultsTo("").ofType(String.class);

      ArgumentAcceptingOptionSpec<String> sslCipherSuitesOpt =
          parser.accepts("sslCipherSuites", "SSL enabled cipher suites").withOptionalArg()
              .describedAs("Comma separated list").defaultsTo("TLS_RSA_WITH_AES_128_CBC_SHA").ofType(String.class);

      OptionSet options = parser.parse(args);

      ArrayList<OptionSpec<?>> listOpt = new ArrayList<OptionSpec<?>>();
      listOpt.add(hardwareLayoutOpt);
      listOpt.add(partitionLayoutOpt);
      listOpt.add(typeOfOperationOpt);
      listOpt.add(AmberBlobIdOpt);
      for (OptionSpec opt : listOpt) {
        if (!options.has(opt)) {
          System.err.println("Missing required argument \"" + opt + "\"");
          parser.printHelpOn(System.err);
          System.exit(1);
        }
      }

      ToolUtils.validateSSLOptions(options, parser, sslEnabledDatacentersOpt, sslKeystorePathOpt, sslKeystoreTypeOpt,
          sslTruststorePathOpt, sslKeystorePasswordOpt, sslKeyPasswordOpt, sslTruststorePasswordOpt);
      String sslEnabledDatacenters = options.valueOf(sslEnabledDatacentersOpt);
      Properties sslProperties;
      if (sslEnabledDatacenters.length() != 0) {
        sslProperties = ToolUtils.createSSLProperties(sslEnabledDatacenters, options.valueOf(sslKeystorePathOpt),
            options.valueOf(sslKeystoreTypeOpt), options.valueOf(sslKeystorePasswordOpt),
            options.valueOf(sslKeyPasswordOpt), options.valueOf(sslTruststorePathOpt),
            options.valueOf(sslTruststorePasswordOpt), options.valueOf(sslCipherSuitesOpt));
      } else {
        sslProperties = new Properties();
      }
      Properties connectionPoolProperties = ToolUtils.createConnectionPoolProperties();
      SSLConfig sslConfig = new SSLConfig(new VerifiableProperties(sslProperties));
      ConnectionPoolConfig connectionPoolConfig =
          new ConnectionPoolConfig(new VerifiableProperties(connectionPoolProperties));
      connectionPool = new BlockingChannelConnectionPool(connectionPoolConfig, sslConfig, new MetricRegistry());
      String hardwareLayoutPath = options.valueOf(hardwareLayoutOpt);
      String partitionLayoutPath = options.valueOf(partitionLayoutOpt);
      ClusterMap map = new ClusterMapManager(hardwareLayoutPath, partitionLayoutPath,
          new ClusterMapConfig(new VerifiableProperties(new Properties())));

      String blobIdStr = options.valueOf(AmberBlobIdOpt);
      ArrayList<String> sslEnabledDatacentersList = Utils.splitString(sslEnabledDatacenters, ",");
      AdminTool adminTool = new AdminTool(connectionPool, sslEnabledDatacentersList);
      BlobId blobId = new BlobId(blobIdStr, map);
      String typeOfOperation = options.valueOf(typeOfOperationOpt);
      boolean includeExpiredBlobs = Boolean.parseBoolean(options.valueOf(includeExpiredBlobsOpt));
      if (typeOfOperation.equalsIgnoreCase("LIST_REPLICAS")) {
        List<ReplicaId> replicaIdList = adminTool.getReplicas(blobId);
        for (ReplicaId replicaId : replicaIdList) {
          System.out.println(replicaId);
        }
      } else if (typeOfOperation.equalsIgnoreCase("GET_BLOB")) {
        adminTool.getBlob(blobId, map, includeExpiredBlobs);
      } else if (typeOfOperation.equalsIgnoreCase("GET_BLOB_PROPERTIES")) {
        adminTool.getBlobProperties(blobId, map, includeExpiredBlobs);
      } else if (typeOfOperation.equalsIgnoreCase("GET_USERMETADATA")) {
        adminTool.getUserMetadata(blobId, map, includeExpiredBlobs);
      } else {
        System.out.println("Invalid Type of Operation ");
        System.exit(1);
      }
    } catch (Exception e) {
      System.out.println("Closed with error " + e);
    } finally {
      if (connectionPool != null) {
        connectionPool.shutdown();
      }
    }
  }

  public List<ReplicaId> getReplicas(BlobId blobId) {
    return blobId.getPartition().getReplicaIds();
  }

  public BlobProperties getBlobProperties(BlobId blobId, ClusterMap map, boolean expiredBlobs) {
    List<ReplicaId> replicas = blobId.getPartition().getReplicaIds();
    BlobProperties blobProperties = null;
    for (ReplicaId replicaId : replicas) {
      try {
        blobProperties = getBlobProperties(blobId, map, replicaId, expiredBlobs);
        break;
      } catch (Exception e) {
        System.out.println("Get blob properties error ");
        e.printStackTrace();
      }
    }
    return blobProperties;
  }

  public BlobProperties getBlobProperties(BlobId blobId, ClusterMap clusterMap, ReplicaId replicaId,
      boolean expiredBlobs)
      throws MessageFormatException, IOException, ConnectionPoolTimeoutException, InterruptedException {
    ArrayList<BlobId> blobIds = new ArrayList<BlobId>();
    blobIds.add(blobId);
    ConnectedChannel connectedChannel = null;
    AtomicInteger correlationId = new AtomicInteger(1);

    PartitionRequestInfo partitionRequestInfo = new PartitionRequestInfo(blobId.getPartition(), blobIds);
    ArrayList<PartitionRequestInfo> partitionRequestInfos = new ArrayList<PartitionRequestInfo>();
    partitionRequestInfos.add(partitionRequestInfo);

    GetOptions getOptions = (expiredBlobs) ? GetOptions.Include_Expired_Blobs : GetOptions.None;

    try {
      Port port = replicaId.getDataNodeId().getPortToConnectTo(sslEnabledDatacentersList);
      connectedChannel = connectionPool.checkOutConnection(replicaId.getDataNodeId().getHostname(), port, 10000);

      GetRequest getRequest =
          new GetRequest(correlationId.incrementAndGet(), "readverifier", MessageFormatFlags.BlobProperties,
              partitionRequestInfos, getOptions);
      System.out.println("Get Request to verify replica blob properties : " + getRequest);
      GetResponse getResponse = null;

      getResponse = BlobValidator.getGetResponseFromStream(connectedChannel, getRequest, clusterMap);
      if (getResponse == null) {
        System.out.println(" Get Response from Stream to verify replica blob properties is null ");
        System.out.println(blobId + " STATE FAILED");
        throw new IOException(" Get Response from Stream to verify replica blob properties is null ");
      }
      ServerErrorCode serverResponseCode = getResponse.getPartitionResponseInfoList().get(0).getErrorCode();
      System.out.println("Get Response from Stream to verify replica blob properties : " + getResponse.getError());
      if (getResponse.getError() != ServerErrorCode.No_Error || serverResponseCode != ServerErrorCode.No_Error) {
        System.out.println("getBlobProperties error on response " + getResponse.getError() +
            " error code on partition " + serverResponseCode +
            " AmberReplica " + replicaId.getDataNodeId().getHostname() + " port " + port.toString() +
            " blobId " + blobId);
        if (serverResponseCode == ServerErrorCode.Blob_Not_Found) {
          return null;
        } else if (serverResponseCode == ServerErrorCode.Blob_Deleted) {
          return null;
        } else {
          return null;
        }
      } else {
        BlobProperties properties = MessageFormatRecord.deserializeBlobProperties(getResponse.getInputStream());
        System.out.println(
            "Blob Properties : Content Type : " + properties.getContentType() + ", OwnerId : " + properties.getOwnerId()
                +
                ", Size : " + properties.getBlobSize() + ", CreationTimeInMs : " + properties.getCreationTimeInMs() +
                ", ServiceId : " + properties.getServiceId() + ", TTL : " + properties.getTimeToLiveInSeconds());
        return properties;
      }
    } catch (MessageFormatException mfe) {
      System.out.println("MessageFormat Exception Error " + mfe);
      connectionPool.destroyConnection(connectedChannel);
      connectedChannel = null;
      throw mfe;
    } catch (IOException e) {
      System.out.println("IOException " + e);
      connectionPool.destroyConnection(connectedChannel);
      connectedChannel = null;
      throw e;
    } finally {
      if (connectedChannel != null) {
        connectionPool.checkInConnection(connectedChannel);
      }
    }
  }

  public BlobData getBlob(BlobId blobId, ClusterMap map, boolean expiredBlobs)
      throws MessageFormatException, IOException {
    List<ReplicaId> replicas = blobId.getPartition().getReplicaIds();
    BlobData blobData = null;
    for (ReplicaId replicaId : replicas) {
      try {
        blobData = getBlob(blobId, map, replicaId, expiredBlobs);
        break;
      } catch (Exception e) {
        System.out.println("Get blob error ");
        e.printStackTrace();
      }
    }
    return blobData;
  }

  public BlobData getBlob(BlobId blobId, ClusterMap clusterMap, ReplicaId replicaId, boolean expiredBlobs)
      throws MessageFormatException, IOException, ConnectionPoolTimeoutException, InterruptedException {
    ArrayList<BlobId> blobIds = new ArrayList<BlobId>();
    blobIds.add(blobId);
    ConnectedChannel connectedChannel = null;
    AtomicInteger correlationId = new AtomicInteger(1);

    PartitionRequestInfo partitionRequestInfo = new PartitionRequestInfo(blobId.getPartition(), blobIds);
    ArrayList<PartitionRequestInfo> partitionRequestInfos = new ArrayList<PartitionRequestInfo>();
    partitionRequestInfos.add(partitionRequestInfo);

    GetOptions getOptions = (expiredBlobs) ? GetOptions.Include_Expired_Blobs : GetOptions.None;

    try {
      Port port = replicaId.getDataNodeId().getPortToConnectTo(sslEnabledDatacentersList);
      connectedChannel = connectionPool.checkOutConnection(replicaId.getDataNodeId().getHostname(), port, 10000);

      GetRequest getRequest = new GetRequest(correlationId.incrementAndGet(), "readverifier", MessageFormatFlags.Blob,
          partitionRequestInfos, getOptions);
      System.out.println("Get Request to get blob : " + getRequest);
      GetResponse getResponse = null;
      getResponse = BlobValidator.getGetResponseFromStream(connectedChannel, getRequest, clusterMap);
      if (getResponse == null) {
        System.out.println(" Get Response from Stream to verify replica blob is null ");
        System.out.println(blobId + " STATE FAILED");
        throw new IOException(" Get Response from Stream to verify replica blob properties is null ");
      }
      System.out.println("Get Response to get blob : " + getResponse.getError());
      ServerErrorCode serverResponseCode = getResponse.getPartitionResponseInfoList().get(0).getErrorCode();
      if (getResponse.getError() != ServerErrorCode.No_Error || serverResponseCode != ServerErrorCode.No_Error) {
        System.out.println("blob get error on response " + getResponse.getError() +
            " error code on partition " + serverResponseCode +
            " AmberReplica " + replicaId.getDataNodeId().getHostname() + " port " + port.toString() +
            " blobId " + blobId);
        if (serverResponseCode == ServerErrorCode.Blob_Not_Found) {
          return null;
        } else if (serverResponseCode == ServerErrorCode.Blob_Deleted) {
          return null;
        } else {
          return null;
        }
      } else {
        return MessageFormatRecord.deserializeBlob(getResponse.getInputStream());
      }
    } catch (MessageFormatException mfe) {
      System.out.println("MessageFormat Exception Error " + mfe);
      connectionPool.destroyConnection(connectedChannel);
      connectedChannel = null;
      throw mfe;
    } catch (IOException e) {
      System.out.println("IOException " + e);
      connectionPool.destroyConnection(connectedChannel);
      connectedChannel = null;
      throw e;
    } finally {
      if (connectedChannel != null) {
        connectionPool.checkInConnection(connectedChannel);
      }
    }
  }

  public ByteBuffer getUserMetadata(BlobId blobId, ClusterMap map, boolean expiredBlobs)
      throws MessageFormatException, IOException {
    List<ReplicaId> replicas = blobId.getPartition().getReplicaIds();
    ByteBuffer userMetadata = null;
    for (ReplicaId replicaId : replicas) {
      try {
        userMetadata = getUserMetadata(blobId, map, replicaId, expiredBlobs);
        break;
      } catch (Exception e) {
        System.out.println("Get user metadata error ");
        e.printStackTrace();
      }
    }
    return userMetadata;
  }

  public ByteBuffer getUserMetadata(BlobId blobId, ClusterMap clusterMap, ReplicaId replicaId, boolean expiredBlobs)
      throws MessageFormatException, IOException, ConnectionPoolTimeoutException, InterruptedException {
    ArrayList<BlobId> blobIds = new ArrayList<BlobId>();
    blobIds.add(blobId);
    ConnectedChannel connectedChannel = null;
    AtomicInteger correlationId = new AtomicInteger(1);

    PartitionRequestInfo partitionRequestInfo = new PartitionRequestInfo(blobId.getPartition(), blobIds);
    ArrayList<PartitionRequestInfo> partitionRequestInfos = new ArrayList<PartitionRequestInfo>();
    partitionRequestInfos.add(partitionRequestInfo);

    GetOptions getOptions = (expiredBlobs) ? GetOptions.Include_Expired_Blobs : GetOptions.None;

    try {
      Port port = replicaId.getDataNodeId().getPortToConnectTo(sslEnabledDatacentersList);
      connectedChannel = connectionPool.checkOutConnection(replicaId.getDataNodeId().getHostname(), port, 10000);

      GetRequest getRequest =
          new GetRequest(correlationId.incrementAndGet(), "readverifier", MessageFormatFlags.BlobUserMetadata,
              partitionRequestInfos, getOptions);
      System.out.println("Get Request to check blob usermetadata : " + getRequest);
      GetResponse getResponse = null;
      getResponse = BlobValidator.getGetResponseFromStream(connectedChannel, getRequest, clusterMap);
      if (getResponse == null) {
        System.out.println(" Get Response from Stream to verify replica blob usermetadata is null ");
        System.out.println(blobId + " STATE FAILED");
        throw new IOException(" Get Response from Stream to verify replica blob properties is null ");
      }
      System.out.println("Get Response to check blob usermetadata : " + getResponse.getError());

      ServerErrorCode serverResponseCode = getResponse.getPartitionResponseInfoList().get(0).getErrorCode();
      if (getResponse.getError() != ServerErrorCode.No_Error || serverResponseCode != ServerErrorCode.No_Error) {
        System.out.println("usermetadata get error on response " + getResponse.getError() +
            " error code on partition " + serverResponseCode +
            " AmberReplica " + replicaId.getDataNodeId().getHostname() + " port " + port.toString() +
            " blobId " + blobId);
        if (serverResponseCode == ServerErrorCode.Blob_Not_Found) {
          return null;
        } else if (serverResponseCode == ServerErrorCode.Blob_Deleted) {
          return null;
        } else {
          return null;
        }
      } else {
        ByteBuffer userMetadata = MessageFormatRecord.deserializeUserMetadata(getResponse.getInputStream());
        System.out.println("Usermetadata deserialized. Size " + userMetadata.capacity());
        return userMetadata;
      }
    } catch (MessageFormatException mfe) {
      System.out.println("MessageFormat Exception Error " + mfe);
      connectionPool.destroyConnection(connectedChannel);
      connectedChannel = null;
      throw mfe;
    } catch (IOException e) {
      System.out.println("IOException " + e);
      connectionPool.destroyConnection(connectedChannel);
      connectedChannel = null;
      throw e;
    } finally {
      if (connectedChannel != null) {
        connectionPool.checkInConnection(connectedChannel);
      }
    }
  }
}
