package com.hireme.resumeservice.domain.entities;

import com.hireme.resumeservice.domain.common.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "contacts")
public class Contact extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String phone;

    private String email;

    private String address;

    private String city;

    @Column(name = "postal_code")
    private String postalCode;

    private String linkedin;
}
