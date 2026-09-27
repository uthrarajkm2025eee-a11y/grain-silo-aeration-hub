package com.agri.silo.service;

import com.agri.silo.dto.ContactDTO;
import com.agri.silo.dto.ProductDTO;
import com.agri.silo.entity.Contact;
import com.agri.silo.entity.Product;
import com.agri.silo.enums.ContactType;
import com.agri.silo.exception.ResourceNotFoundException;
import com.agri.silo.repository.ContactRepository;
import com.agri.silo.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MasterDataService {

    private final ContactRepository contactRepository;
    private final ProductRepository productRepository;

    public MasterDataService(ContactRepository contactRepository, ProductRepository productRepository) {
        this.contactRepository = contactRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<ContactDTO> getContacts(ContactType type) {
        List<Contact> contacts = (type != null) ? contactRepository.findByType(type) : contactRepository.findAll();
        return contacts.stream().map(this::mapToContactDTO).collect(Collectors.toList());
    }

    @Transactional
    public ContactDTO createContact(ContactDTO dto) {
        Contact contact = Contact.builder()
                .code(dto.getCode())
                .name(dto.getName())
                .type(dto.getType())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .build();
        Contact saved = contactRepository.save(contact);
        return mapToContactDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getProducts() {
        return productRepository.findAll().stream().map(this::mapToProductDTO).collect(Collectors.toList());
    }

    @Transactional
    public ProductDTO createProduct(ProductDTO dto) {
        Product product = Product.builder()
                .code(dto.getCode())
                .name(dto.getName())
                .type(dto.getType())
                .unitPrice(dto.getUnitPrice())
                .taxRate(dto.getTaxRate() != null ? dto.getTaxRate() : java.math.BigDecimal.ZERO)
                .build();
        Product saved = productRepository.save(product);
        return mapToProductDTO(saved);
    }

    private ContactDTO mapToContactDTO(Contact c) {
        return ContactDTO.builder()
                .id(c.getId())
                .code(c.getCode())
                .name(c.getName())
                .type(c.getType())
                .email(c.getEmail())
                .phone(c.getPhone())
                .address(c.getAddress())
                .build();
    }

    private ProductDTO mapToProductDTO(Product p) {
        return ProductDTO.builder()
                .id(p.getId())
                .code(p.getCode())
                .name(p.getName())
                .type(p.getType())
                .unitPrice(p.getUnitPrice())
                .taxRate(p.getTaxRate())
                .build();
    }
}
