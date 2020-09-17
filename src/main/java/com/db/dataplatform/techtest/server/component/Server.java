package com.db.dataplatform.techtest.server.component;

import com.db.dataplatform.techtest.server.api.model.DataEnvelope;
import com.db.dataplatform.techtest.server.persistence.BlockTypeEnum;
import com.db.dataplatform.techtest.server.persistence.model.DataBodyEntity;
import javassist.NotFoundException;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface Server {
    boolean saveDataEnvelope(DataEnvelope envelope) throws IOException, NoSuchAlgorithmException;
    boolean validateChecksum(DataEnvelope envelope,String checksum) throws NoSuchAlgorithmException;
    List<DataEnvelope> getDataByBlockType(BlockTypeEnum blockType);
    Boolean updateBlockType(String name, BlockTypeEnum blockType) throws NotFoundException;
}
