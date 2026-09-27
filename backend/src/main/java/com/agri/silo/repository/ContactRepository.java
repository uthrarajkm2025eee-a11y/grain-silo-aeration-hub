package com.agri.silo.repository;

import com.agri.silo.entity.Contact;
import com.agri.silo.enums.ContactType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, Long> {
    List<Contact> findByType(ContactType type);
}
