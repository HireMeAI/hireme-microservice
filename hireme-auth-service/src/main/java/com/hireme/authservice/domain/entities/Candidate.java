package com.hireme.authservice.domain.entities;

import com.hireme.authservice.configs.security.PiiAttributeConverter;
import com.hireme.authservice.domain.enums.AvailabilityStatus;
import com.hireme.authservice.domain.enums.ContractType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "candidates")
@PrimaryKeyJoinColumn(name = "user_id") // <--- La clé qui fait le lien avec la table Users
@SuperBuilder // <--- Indispensable pour construire l'objet complet
@EqualsAndHashCode(callSuper = true)
public class Candidate extends User {
    // PII en texte libre chiffrée au repos en AES-256-GCM (§7.3, RGPD Art. 32).
    // Champ non indexé : le chiffrement non déterministe (IV aléatoire) ne casse aucune requête.
    @Convert(converter = PiiAttributeConverter.class)
    @Column(columnDefinition = "TEXT")
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability_status")
    private AvailabilityStatus availability;

    @ElementCollection(targetClass = ContractType.class, fetch = FetchType.EAGER)
    @CollectionTable(
            name = "candidate_contract_preferences",
            joinColumns = @JoinColumn(name = "candidate_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type")
    private Set<ContractType> contractPreferences;

    @Column(name = "auto_apply_enabled")
    private boolean autoApplyEnabled;

    private String desiredJobTitle;

    @Column(name = "open_to_relocate")
    private boolean openToRelocate;
}
