package com.agri.silo.controller;

import com.agri.silo.dto.ContactDTO;
import com.agri.silo.enums.ContactType;
import com.agri.silo.service.MasterDataService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private final MasterDataService masterDataService;

    public ContactController(MasterDataService masterDataService) {
        this.masterDataService = masterDataService;
    }

    @GetMapping
    public ResponseEntity<List<ContactDTO>> getContacts(@RequestParam(required = false) ContactType type) {
        return ResponseEntity.ok(masterDataService.getContacts(type));
    }

    @PostMapping
    public ResponseEntity<ContactDTO> createContact(@Valid @RequestBody ContactDTO dto) {
        ContactDTO created = masterDataService.createContact(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
}
