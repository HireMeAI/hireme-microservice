package com.hireme.resumeservice.services;

import com.hireme.resumeservice.dtos.contact.ContactRequest;
import com.hireme.resumeservice.dtos.contact.ContactResponse;

import java.util.List;
import java.util.UUID;

public interface ContactService {

    List<ContactResponse> findAll();

    ContactResponse findById(UUID id);

    ContactResponse create(ContactRequest request);

    ContactResponse update(UUID id, ContactRequest request);

    void delete(UUID id);
}
