package com.db.dataplatform.techtest.server.component.impl;

import com.db.dataplatform.techtest.server.api.model.DataBody;
import com.db.dataplatform.techtest.server.api.model.DataEnvelope;
import com.db.dataplatform.techtest.server.api.model.DataHeader;
import com.db.dataplatform.techtest.server.persistence.BlockTypeEnum;
import com.db.dataplatform.techtest.server.persistence.model.DataBodyEntity;
import com.db.dataplatform.techtest.server.persistence.model.DataHeaderEntity;
import com.db.dataplatform.techtest.server.service.DataBodyService;
import com.db.dataplatform.techtest.server.component.Server;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServerImpl implements Server {

    private final DataBodyService dataBodyServiceImpl;
    private final ModelMapper modelMapper;

    /**
     * @param envelope represents a block of data
     * @return true if there is a match with the client provided checksum.
     */
    @Override
    public boolean saveDataEnvelope(DataEnvelope envelope) {

        // Save to persistence.
        persist(envelope);

        log.info("Data persisted successfully, data name: {}", envelope.getDataHeader().getName());
        return true;
    }

    @Override
    public boolean validateChecksum(DataEnvelope dataEnvelope,String checksum) throws NoSuchAlgorithmException
    {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(dataEnvelope.getDataBody().getDataBody().getBytes());
        byte[] digest = md.digest();
        String myHash = DatatypeConverter
                .printHexBinary(digest).toUpperCase();
        log.info("Provided checksum: {}, Checksum on data: {}",checksum.toUpperCase(),myHash);
        return myHash.equals(checksum.toUpperCase());
    }

    @Override
    public List<DataEnvelope> getDataByBlockType(BlockTypeEnum blockType)
    {
        return dataBodyServiceImpl.getDataByBlockType(blockType).stream()
                .map(block->new DataEnvelope(modelMapper.map(block.getDataHeaderEntity(),DataHeader.class),
                        new DataBody(block.getDataBody())))
                .collect(Collectors.toList());
    }

    @Override
    public Boolean updateBlockType(String name, BlockTypeEnum blockType) throws NotFoundException {

     DataBodyEntity dataBodyEntity = dataBodyServiceImpl.getDataByBlockName(name)
             .orElseThrow(()->new NotFoundException("block with name " + name + " not found."));

        dataBodyEntity.getDataHeaderEntity().setBlockType(blockType);
        saveData(dataBodyEntity);
        log.info("updated block type for block {} to {}",name,blockType);
        return true;

    }

    private void persist(DataEnvelope envelope) {
        log.info("Persisting data with attribute name: {}", envelope.getDataHeader().getName());
        DataHeaderEntity dataHeaderEntity = modelMapper.map(envelope.getDataHeader(), DataHeaderEntity.class);

        DataBodyEntity dataBodyEntity = modelMapper.map(envelope.getDataBody(), DataBodyEntity.class);
        dataBodyEntity.setDataHeaderEntity(dataHeaderEntity);

        saveData(dataBodyEntity);
    }

    private void saveData(DataBodyEntity dataBodyEntity) {
        dataBodyServiceImpl.saveDataBody(dataBodyEntity);
    }

}
