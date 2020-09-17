package com.db.dataplatform.techtest.server.persistence.repository;

import com.db.dataplatform.techtest.server.persistence.BlockTypeEnum;
import com.db.dataplatform.techtest.server.persistence.model.DataBodyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DataStoreRepository extends JpaRepository<DataBodyEntity, Long> {

    @Query("select dbe from DataBodyEntity dbe join DataHeaderEntity dhe on dbe.dataHeaderEntity=dhe where dhe.blockType = :blockType")
    List<DataBodyEntity> findByBlockType(@Param("blockType") BlockTypeEnum blockType);

    @Query("select dbe from DataBodyEntity dbe join DataHeaderEntity dhe on dbe.dataHeaderEntity=dhe where dhe.name = :name")
    Optional<DataBodyEntity> findByName(@Param("name") String name);
}
