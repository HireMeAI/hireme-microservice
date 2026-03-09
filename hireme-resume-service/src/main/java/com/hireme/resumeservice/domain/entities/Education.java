package com.hireme.resumeservice.domain.entities;

import com.hireme.resumeservice.domain.common.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "educations")
public class Education extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID code;

    @ManyToOne
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    private String degree;

    private String institution;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "end_date")
    private LocalDate endDate;
}
