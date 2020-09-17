package com.db.dataplatform.techtest.api.controller;

import com.db.dataplatform.techtest.TestDataHelper;
import com.db.dataplatform.techtest.server.api.controller.ServerController;
import com.db.dataplatform.techtest.server.api.model.DataEnvelope;
import com.db.dataplatform.techtest.server.exception.HadoopClientException;
import com.db.dataplatform.techtest.server.component.Server;
import com.db.dataplatform.techtest.server.persistence.BlockTypeEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import javassist.NotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.util.UriTemplate;

import java.io.IOException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;

import static com.db.dataplatform.techtest.TestDataHelper.TEST_NAME;
import static com.db.dataplatform.techtest.server.persistence.BlockTypeEnum.BLOCKTYPEB;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@RunWith(MockitoJUnitRunner.class)
public class ServerControllerComponentTest {

	public static final String URI_PUSHDATA = "http://localhost:8090/dataserver/pushdata";
	public static final UriTemplate URI_GETDATA = new UriTemplate("http://localhost:8090/dataserver/data/{blockType}");
	public static final UriTemplate URI_PATCHDATA = new UriTemplate("http://localhost:8090/dataserver/update/{name}/{newBlockType}");

	@Mock
	private Server serverMock;

	private DataEnvelope testDataEnvelope;
	private ObjectMapper objectMapper;
	private MockMvc mockMvc;
	private ServerController serverController;

	@Before
	public void setUp() throws HadoopClientException, NoSuchAlgorithmException, IOException {
		serverController = new ServerController(serverMock);
		mockMvc = standaloneSetup(serverController).build();
		objectMapper = Jackson2ObjectMapperBuilder
				.json()
				.build();

		testDataEnvelope = TestDataHelper.createTestDataEnvelopeApiObject();

		when(serverMock.saveDataEnvelope(any(DataEnvelope.class))).thenReturn(true);
	}

	@Test
	public void testPushDataPostCallWorksAsExpected() throws Exception {

		String testDataEnvelopeJson = objectMapper.writeValueAsString(testDataEnvelope);
		when(serverMock.validateChecksum(any(DataEnvelope.class),eq("bfc"))).thenReturn(true);

		MvcResult result =mockMvc.perform(post(URI_PUSHDATA).param("checksum","bfc")
				.content(testDataEnvelopeJson)
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk())
				.andReturn();

		assertThat(result.getResponse().getContentAsString()).isEqualTo("true");
		verify(serverMock).saveDataEnvelope(testDataEnvelope);
	}

	@Test
	public void returnsConflictGivenIncorrectChecksum() throws Exception
	{
		String testDataEnvelopeJson = objectMapper.writeValueAsString(testDataEnvelope);
		when(serverMock.validateChecksum(any(DataEnvelope.class),eq("bfc"))).thenReturn(false);

		mockMvc.perform(post(URI_PUSHDATA).param("checksum","bfc")
				.content(testDataEnvelopeJson)
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isConflict());

		verify(serverMock,times(0)).saveDataEnvelope(any());
	}

	@Test
	public void doesNotSaveIfChecksumMissing() throws Exception
	{
		String testDataEnvelopeJson = objectMapper.writeValueAsString(testDataEnvelope);

		mockMvc.perform(post(URI_PUSHDATA)
				.content(testDataEnvelopeJson)
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isBadRequest());

		verify(serverMock,times(0)).saveDataEnvelope(any());
	}

	@Test
	public void getBlocksByType() throws Exception
	{
		String testDataEnvelopeJson = objectMapper.writeValueAsString(Collections.singletonList(testDataEnvelope));
		when(serverMock.getDataByBlockType(BlockTypeEnum.BLOCKTYPEA)).thenReturn(Collections.singletonList(testDataEnvelope));

		URI url = URI_GETDATA.expand(BlockTypeEnum.BLOCKTYPEA);
		MvcResult result = mockMvc.perform(get(url.toASCIIString())
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk())
				.andReturn();

		assertThat(result.getResponse().getContentAsString()).isEqualTo(testDataEnvelopeJson);

	}

	@Test
	public void getBlocksByTypeReturnsEmptyListWhenNoData() throws Exception
	{
		URI url = URI_GETDATA.expand(BlockTypeEnum.BLOCKTYPEA);
		when(serverMock.getDataByBlockType(BlockTypeEnum.BLOCKTYPEA)).thenReturn(Collections.EMPTY_LIST);

		MvcResult result = mockMvc.perform(get(url.toASCIIString())
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk())
				.andReturn();

		assertThat(result.getResponse().getContentAsString()).isEqualTo(objectMapper.writeValueAsString(Collections.EMPTY_LIST));
	}

	@Test
	public void getBlockByTypeReturns400GivenInvalidBlockType() throws Exception
	{
		URI url = URI_GETDATA.expand("invalid");

		mockMvc.perform(get(url.toASCIIString())
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isBadRequest());

	}

	@Test
	public void updateBlockType() throws Exception
	{
		URI url = URI_PATCHDATA.expand(TEST_NAME,BLOCKTYPEB);
		when(serverMock.updateBlockType(TEST_NAME,BLOCKTYPEB)).thenReturn(true);
		MvcResult result = mockMvc.perform(patch(url.toASCIIString())
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk())
				.andReturn();

		assertThat(result.getResponse().getContentAsString()).isEqualTo("true");
	}

	@Test
	public void updateBlockTypeReturns404GivenInvalidName() throws Exception {
		URI url = URI_PATCHDATA.expand("invalid",BLOCKTYPEB);
		when(serverMock.updateBlockType("invalid",BLOCKTYPEB)).thenThrow(new NotFoundException("not found"));
		mockMvc.perform(patch(url.toASCIIString())
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isNotFound());
	}

	@Test
	public void updateBlockTypeReturns400GivenInvalidBlockType() throws Exception
	{
		URI url = URI_PATCHDATA.expand(TEST_NAME,"invalid");

		mockMvc.perform(patch(url.toASCIIString())
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isBadRequest());
	}

}
