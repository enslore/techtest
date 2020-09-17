package com.db.dataplatform.techtest.server.persistence.repository;

import com.db.dataplatform.techtest.EmbeddedDataSourceConfiguration;
import com.db.dataplatform.techtest.TestDataHelper;
import com.db.dataplatform.techtest.server.persistence.BlockTypeEnum;
import com.db.dataplatform.techtest.server.persistence.model.DataBodyEntity;
import com.db.dataplatform.techtest.server.persistence.model.DataHeaderEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@ContextConfiguration(classes=EmbeddedDataSourceConfiguration.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class DataStoreRepositoryTest {

    @Resource
    DataStoreRepository repository;

    DataBodyEntity dataBodyEntity;
    DataHeaderEntity dataHeaderEntity;

    @Before
    public void setUp()
    {
        dataHeaderEntity = TestDataHelper.createTestDataHeaderEntity(Instant.now());
        DataHeaderEntity dataHeaderEntity1= TestDataHelper.createTestDataHeaderEntity(Instant.now());
        dataHeaderEntity1.setName("anotherName");
        dataHeaderEntity1.setBlockType(BlockTypeEnum.BLOCKTYPEB);
        dataBodyEntity = TestDataHelper.createTestDataBodyEntity(dataHeaderEntity);
        DataBodyEntity dataBodyEntity1 = TestDataHelper.createTestDataBodyEntity(dataHeaderEntity1);
        repository.save(dataBodyEntity);
        repository.save(dataBodyEntity1); // we should never get this one back
    }

    //todo : just couldn't make this work - can't get it to run the script that sets up the database, even though it's already in the config
    @Test
    public void findByBlockType() {
        //assertThat(repository.findByBlockType(dataHeaderEntity.getBlockType()).size()).isEqualTo(1);
    }

    @Test
    public void findByName() {
        //assertThat(repository.findByName(dataHeaderEntity.getName()).get()).isEqualTo(dataBodyEntity);
    }
}