package com.db.dataplatform.techtest.service;

import com.db.dataplatform.techtest.TestDataHelper;
import com.db.dataplatform.techtest.server.api.model.DataEnvelope;
import com.db.dataplatform.techtest.server.mapper.ServerMapperConfiguration;
import com.db.dataplatform.techtest.server.persistence.BlockTypeEnum;
import com.db.dataplatform.techtest.server.persistence.model.DataBodyEntity;
import com.db.dataplatform.techtest.server.persistence.model.DataHeaderEntity;
import com.db.dataplatform.techtest.server.service.DataBodyService;
import com.db.dataplatform.techtest.server.component.Server;
import com.db.dataplatform.techtest.server.component.impl.ServerImpl;
import javassist.NotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.modelmapper.ModelMapper;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.db.dataplatform.techtest.TestDataHelper.TEST_NAME;
import static com.db.dataplatform.techtest.TestDataHelper.createTestDataEnvelopeApiObject;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ServerServiceTests {

    @Mock
    private DataBodyService dataBodyServiceImplMock;

    private ModelMapper modelMapper;

    private DataBodyEntity expectedDataBodyEntity;
    private DataEnvelope testDataEnvelope;

    private Server server;

    @Before
    public void setup() {
        ServerMapperConfiguration serverMapperConfiguration = new ServerMapperConfiguration();
        modelMapper = serverMapperConfiguration.createModelMapperBean();

        testDataEnvelope = createTestDataEnvelopeApiObject();
        expectedDataBodyEntity = modelMapper.map(testDataEnvelope.getDataBody(), DataBodyEntity.class);
        expectedDataBodyEntity.setDataHeaderEntity(modelMapper.map(testDataEnvelope.getDataHeader(), DataHeaderEntity.class));

        server = new ServerImpl(dataBodyServiceImplMock, modelMapper);
    }

    @Test
    public void shouldSaveDataEnvelopeAsExpected() throws NoSuchAlgorithmException, IOException {
        boolean success = server.saveDataEnvelope(testDataEnvelope);

        assertThat(success).isTrue();
        verify(dataBodyServiceImplMock, times(1)).saveDataBody(eq(expectedDataBodyEntity));
    }

    @Test
    public void shouldReturnFalseIfChecksumInvalid() throws Exception
    {
        assertThat(server.validateChecksum(testDataEnvelope,"bleurgh")).isFalse();
    }

    @Test
    public void shouldReturnTrueIfChecksumValid()throws Exception
    {
        assertThat(server.validateChecksum(testDataEnvelope, TestDataHelper.DUMMY_HASH)).isTrue();
    }

    @Test
    public void shouldGetBlocksByType()
    {
        when(dataBodyServiceImplMock.getDataByBlockType(BlockTypeEnum.BLOCKTYPEA)).thenReturn(Collections.singletonList(expectedDataBodyEntity));
        List<DataEnvelope> returnedList = server.getDataByBlockType(BlockTypeEnum.BLOCKTYPEA);
        assertThat(returnedList.size()).isEqualTo(1);
        DataEnvelope returnedEnvelope = returnedList.iterator().next();
        assertThat(returnedEnvelope.getDataHeader()).isEqualTo(testDataEnvelope.getDataHeader());
        assertThat(returnedEnvelope.getDataBody()).isEqualToComparingFieldByField(testDataEnvelope.getDataBody());
    }

    @Test
    public void shouldGetEmptyListWhenNoBlocksOfThatType()
    {
        when(dataBodyServiceImplMock.getDataByBlockType(BlockTypeEnum.BLOCKTYPEA)).thenReturn(Collections.EMPTY_LIST);
        assertThat(server.getDataByBlockType(BlockTypeEnum.BLOCKTYPEA)).isEqualTo(Collections.EMPTY_LIST);
    }

    @Test
    public void updateBlockType() throws Exception
    {
        when(dataBodyServiceImplMock.getDataByBlockName(TEST_NAME)).thenReturn(Optional.of(expectedDataBodyEntity));

        DataBodyEntity updatedDataBodyEntity = expectedDataBodyEntity;
        updatedDataBodyEntity.getDataHeaderEntity().setBlockType(BlockTypeEnum.BLOCKTYPEB);

        assertThat(server.updateBlockType(TEST_NAME,BlockTypeEnum.BLOCKTYPEB)).isTrue();
        verify(dataBodyServiceImplMock).saveDataBody(updatedDataBodyEntity);
    }

    @Test(expected = NotFoundException.class)
    public void updateBlockTypeThrowsNotFoundGivenInvalidBlockName() throws Exception
    {
        when(dataBodyServiceImplMock.getDataByBlockName(TEST_NAME)).thenReturn(Optional.empty());


        server.updateBlockType(TEST_NAME,BlockTypeEnum.BLOCKTYPEB);
        verify(dataBodyServiceImplMock,times(0)).saveDataBody(any());
    }
}
