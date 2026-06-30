package com.hireme.matchingservice.domain.entities;

import com.hireme.matchingservice.domain.common.Auditable;
import com.hireme.matchingservice.domain.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Entité pivot de la plateforme (§5.5) : une candidature relie un candidat, un CV et une offre,
 * chacun appartenant à un autre contexte borné. Conformément au pattern Database-per-Service,
 * les références externes sont des <b>UUID applicatifs</b>, sans clé étrangère inter-bases.
 *
 * <p>Le {@code matchScore} calculé par le moteur ML est <b>persisté et historisé</b> ici, dans
 * la base {@code hireme_matching} dont ce service a la responsabilité.</p>
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "applications")
public class Application extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "candidate_id", nullable = false)
    private UUID candidateId;

    @Column(name = "resume_id", nullable = false)
    private UUID resumeId;

    @Column(name = "job_offer_id", nullable = false)
    private UUID jobOfferId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.SUBMITTED;

    /** Score de pertinence calibré dans [0, 1] retourné par le moteur ML. */
    @Column(name = "match_score")
    private Double matchScore;

    /** Origine de la candidature : MANUAL (candidat) ou AUTO (auto-apply). */
    private String source;

    @Column(columnDefinition = "TEXT")
    private String note;
}
