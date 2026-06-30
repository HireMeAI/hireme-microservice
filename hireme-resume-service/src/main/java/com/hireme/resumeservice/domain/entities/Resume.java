package com.hireme.resumeservice.domain.entities;

import com.hireme.resumeservice.domain.common.Auditable;
import com.hireme.resumeservice.domain.enums.Visibility;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.ArrayList;
import java.util.HashSet;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "resumes")
public class Resume extends Auditable {

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private UUID id;

        @Column(name = "user_id", nullable = false)
        private UUID userId;

        @ManyToOne
        @JoinColumn(name = "contact_id")
        private Contact contact;

        @ManyToOne
        @JoinColumn(name = "template_id")
        private Template template;

        private String title;

        @Column(name = "portfolio_slug", unique = true)
        private String portfolioSlug;

        @Column(columnDefinition = "TEXT")
        private String summary;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private Visibility visibility;

        @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
        @Builder.Default
        private List<Experience> experiences = new ArrayList<>();

        @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
        @Builder.Default
        private List<Education> educations = new ArrayList<>();

        @ManyToMany
        @JoinTable(name = "resume_skills", joinColumns = @JoinColumn(name = "resume_id"), inverseJoinColumns = @JoinColumn(name = "skill_id"))
        @Builder.Default
        private Set<Skill> skills = new HashSet<>();

        @ManyToMany
        @JoinTable(name = "resume_languages", joinColumns = @JoinColumn(name = "resume_id"), inverseJoinColumns = @JoinColumn(name = "language_id"))
        @Builder.Default
        private Set<Language> languages = new HashSet<>();
}
