package com.hireme.resumeservice.dtos.contact;

public record ContactRequest(
        String phone,
        String email,
        String address,
        String city,
        String postalCode,
        String linkedin
) {}
