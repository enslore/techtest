package com.db.dataplatform.techtest.server.api.controller;

import com.db.dataplatform.techtest.server.api.model.DataEnvelope;
import com.db.dataplatform.techtest.server.component.Server;
import com.db.dataplatform.techtest.server.persistence.BlockTypeEnum;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/dataserver")
@RequiredArgsConstructor
@Validated
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class ServerController {

    private final Server server;

    //TODO add swagger or other api doc annotations
    @PostMapping(value = "/pushdata", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> pushData(@Valid @RequestBody DataEnvelope dataEnvelope,
                                            @RequestParam @NotNull String checksum
                                            ) throws IOException, NoSuchAlgorithmException {

        log.info("Data envelope received: {}", dataEnvelope.getDataHeader().getName());
        if (!server.validateChecksum(dataEnvelope,checksum)) {return ResponseEntity.status(HttpStatus.CONFLICT).body(false);}

        log.info("Data envelope persisted. Attribute name: {}", dataEnvelope.getDataHeader().getName());
        return ResponseEntity.ok(server.saveDataEnvelope(dataEnvelope));
    }

    @GetMapping(value="/data/{blockType}",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<DataEnvelope>> getDataByType(@PathVariable @NotNull BlockTypeEnum blockType)
    {
        return ResponseEntity.ok(server.getDataByBlockType(blockType));
    }

    @PatchMapping(value="/update/{name}/{newBlockType}",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> updateBlockType(@PathVariable @NotNull String name,
                                                   @PathVariable @NotNull BlockTypeEnum newBlockType)
    {
        try
        {
        return ResponseEntity.ok(server.updateBlockType(name,newBlockType));
        }
        catch (NotFoundException exception)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
        }
    }
}
