package com.hireme.resumeservice.services.impl;

import com.hireme.resumeservice.domain.entities.Contact;
import com.hireme.resumeservice.dtos.contact.ContactRequest;
import com.hireme.resumeservice.dtos.contact.ContactResponse;
import com.hireme.resumeservice.exception.ApiException;
import com.hireme.resumeservice.exception.ErrorCode;
import com.hireme.resumeservice.repositories.ContactRepository;
import com.hireme.resumeservice.services.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {

    private final ContactRepository contactRepository;

    @Override
    public List<ContactResponse> findAll() {
        return contactRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ContactResponse findById(UUID id) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.CONTACT_NOT_FOUND, "Contact with id " + id + " not found"));
        return toResponse(contact);
    }

    @Override
    @Transactional
    public ContactResponse create(ContactRequest request) {
        Contact contact = Contact.builder()
                .phone(request.phone())
                .email(request.email())
                .address(request.address())
                .city(request.city())
                .postalCode(request.postalCode())
                .linkedin(request.linkedin())
                .build();
        return toResponse(contactRepository.save(contact));
    }

    @Override
    @Transactional
    public ContactResponse update(UUID id, ContactRequest request) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.CONTACT_NOT_FOUND, "Contact with id " + id + " not found"));

        contact.setPhone(request.phone());
        contact.setEmail(request.email());
        contact.setAddress(request.address());
        contact.setCity(request.city());
        contact.setPostalCode(request.postalCode());
        contact.setLinkedin(request.linkedin());

        return toResponse(contactRepository.save(contact));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!contactRepository.existsById(id)) {
            throw new ApiException(ErrorCode.CONTACT_NOT_FOUND, "Contact with id " + id + " not found");
        }
        contactRepository.deleteById(id);
    }

    private ContactResponse toResponse(Contact contact) {
        return new ContactResponse(
                contact.getId(),
                contact.getPhone(),
                contact.getEmail(),
                contact.getAddress(),
                contact.getCity(),
                contact.getPostalCode(),
                contact.getLinkedin(),
                contact.getCreatedAt(),
                contact.getUpdatedAt()
        );
    }
}
