package com.hireme.authservice.configs;

import com.hireme.authservice.domain.entities.Candidate;
import com.hireme.authservice.domain.entities.Recruiter;
import com.hireme.authservice.domain.entities.User;
import com.hireme.authservice.domain.enums.AvailabilityStatus;
import com.hireme.authservice.domain.enums.ContractType;
import com.hireme.authservice.domain.enums.TypeRole;
import com.hireme.authservice.repositories.CandidateRepository;
import com.hireme.authservice.repositories.RecruiterRepository;
import com.hireme.authservice.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Set;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;
    private final RecruiterRepository recruiterRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner seedDatabase() {
        return args -> {
            if (userRepository.count() > 0) {
                log.info("Database already seeded, skipping.");
                return;
            }

            log.info("Seeding database...");

            seedAdmin();
            seedCandidates();
            seedRecruiters();

            log.info("Database seeded successfully.");
            log.info("=== Comptes de test ===");
            log.info("ADMIN      : admin@hireme.com / Admin1234!");
            log.info("CANDIDAT 1 : alice.martin@example.com / Password123!");
            log.info("CANDIDAT 2 : bob.dupont@example.com / Password123!");
            log.info("RECRUTEUR 1: sophie.legrand@techcorp.com / Password123!");
            log.info("RECRUTEUR 2: marc.bernard@startup.io / Password123!");
            log.info("=======================");
        };
    }

    private void seedAdmin() {
        User admin = User.builder()
                .firstName("Super")
                .lastName("Admin")
                .email("admin@hireme.com")
                .password(passwordEncoder.encode("Admin1234!"))
                .role(TypeRole.ADMIN)
                .confirmedAt(LocalDateTime.now())
                .build();

        userRepository.save(admin);
    }

    private void seedCandidates() {
        Candidate alice = Candidate.builder()
                .firstName("Alice")
                .lastName("Martin")
                .email("alice.martin@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .role(TypeRole.CANDIDATE)
                .confirmedAt(LocalDateTime.now())
                .bio("Développeuse full-stack passionnée par le web et l'IA.")
                .availability(AvailabilityStatus.IMMEDIATE)
                .contractPreferences(Set.of(ContractType.FULL_TIME, ContractType.FREELANCE))
                .desiredJobTitle("Développeuse Full-Stack")
                .autoApplyEnabled(false)
                .openToRelocate(true)
                .build();

        Candidate bob = Candidate.builder()
                .firstName("Bob")
                .lastName("Dupont")
                .email("bob.dupont@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .role(TypeRole.CANDIDATE)
                .confirmedAt(LocalDateTime.now())
                .bio("Ingénieur backend Java/Spring, 5 ans d'expérience.")
                .availability(AvailabilityStatus.OPEN_TO_TALK)
                .contractPreferences(Set.of(ContractType.FULL_TIME, ContractType.FIXED_TERM))
                .desiredJobTitle("Ingénieur Backend")
                .autoApplyEnabled(true)
                .openToRelocate(false)
                .build();

        candidateRepository.save(alice);
        candidateRepository.save(bob);
    }

    private void seedRecruiters() {
        Recruiter sophie = Recruiter.builder()
                .firstName("Sophie")
                .lastName("Legrand")
                .email("sophie.legrand@techcorp.com")
                .password(passwordEncoder.encode("Password123!"))
                .role(TypeRole.RECRUITER)
                .confirmedAt(LocalDateTime.now())
                .actualPosition("Responsable RH")
                .phoneNumber(612345678)
                .build();

        Recruiter marc = Recruiter.builder()
                .firstName("Marc")
                .lastName("Bernard")
                .email("marc.bernard@startup.io")
                .password(passwordEncoder.encode("Password123!"))
                .role(TypeRole.RECRUITER)
                .confirmedAt(LocalDateTime.now())
                .actualPosition("CTO & Co-fondateur")
                .phoneNumber(698765432)
                .build();

        recruiterRepository.save(sophie);
        recruiterRepository.save(marc);
    }
}
