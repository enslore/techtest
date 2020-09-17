package com.db.dataplatform.techtest.client.component.impl;

import com.db.dataplatform.techtest.client.api.model.DataEnvelope;
import com.db.dataplatform.techtest.client.component.Client;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClientImpl implements Client {

    private static final String URI_PUSHDATA = "http://localhost:8090/dataserver/pushdata";
    private static final UriTemplate URI_GETDATA = new UriTemplate("http://localhost:8090/dataserver/data/{blockType}");
    private static final UriTemplate URI_PATCHDATA = new UriTemplate("http://localhost:8090/dataserver/update/{name}/{newBlockType}");
    private final RestTemplate restTemplate;

    @Override
    public void pushData(DataEnvelope dataEnvelope,String checksum) {
        log.info("Pushing data {} to {}", dataEnvelope.getDataHeader().getName(), URI_PUSHDATA);
        String uriComponents = UriComponentsBuilder.fromUriString(URI_PUSHDATA)
                .queryParam("checksum",checksum)
                .build().toUriString();

        restTemplate.postForObject(uriComponents,dataEnvelope,Boolean.class,Collections.singletonMap("checksum",checksum));
    }

    @Override
    public List<DataEnvelope> getData(String blockType) {
        log.info("Query for data with header block type {}", blockType);
        return Arrays.asList(restTemplate.getForObject(URI_GETDATA.expand(blockType),DataEnvelope[].class));
    }

    @Override
    public boolean updateData(String blockName, String newBlockType) {
        log.info("Updating blocktype to {} for block with name {}", newBlockType, blockName);
        return restTemplate.patchForObject(URI_PATCHDATA.expand(blockName,newBlockType),null,Boolean.class);
    }


}
