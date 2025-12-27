package com.hireme.authservice.domain.entities;

import com.hireme.authservice.domain.enums.TypeRole;
import com.hireme.authservice.domain.common.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
@Builder
public class User  extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String lastName;

    @Column(length = 100)
    private String firstName;

    @Column(nullable = false, unique=true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Transient
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeRole role;

    public String getFullName() {
        return firstName + " " + lastName;
    }

}
