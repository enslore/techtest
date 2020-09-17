package com.db.dataplatform.techtest.client.component.impl;

import com.db.dataplatform.techtest.client.api.model.DataEnvelope;
import com.db.dataplatform.techtest.client.api.model.DataHeader;
import com.db.dataplatform.techtest.client.component.Client;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ClientImplTest {

    private Client client;

    @Mock
    RestTemplate restTemplate;
    @Mock
    DataEnvelope dataEnvelope;
    @Mock
    DataHeader dataHeader;

    @Before
    public void Setup()
    {
        initMocks(this);
        client = new ClientImpl(restTemplate);
        when(dataEnvelope.getDataHeader()).thenReturn(dataHeader);
        when(dataHeader.getName()).thenReturn("name");
    }

    @Test
    public void pushData() throws Exception {
        client.pushData(dataEnvelope,"checksum");
        verify(restTemplate).postForObject(anyString(),eq(dataEnvelope),eq(Boolean.class),anyMap());
    }

    @Test
    public void getData() throws Exception
    {
        when(restTemplate.getForObject(any(),any())).thenReturn(new DataEnvelope[] {});
        client.getData("blockType");
        verify(restTemplate).getForObject(any(),any());
    }

    //todo - test the update

}