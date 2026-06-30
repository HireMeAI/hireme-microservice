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
            seedCandidate();
            seedRecruiter();

            log.info("Database seeded successfully.");
            log.info("╔══════════════════════════════════════════════════════╗");
            log.info("║              COMPTES DE TEST DISPONIBLES             ║");
            log.info("╠══════════════╦═══════════════════════════╦═══════════╣");
            log.info("║ RÔLE         ║ EMAIL                     ║ MOT PASSE ║");
            log.info("╠══════════════╬═══════════════════════════╬═══════════╣");
            log.info("║ ADMIN        ║ admin@hireme.com          ║ Admin1!   ║");
            log.info("║ CANDIDAT     ║ candidate@hireme.com      ║ Cand1!    ║");
            log.info("║ RECRUTEUR    ║ recruiter@hireme.com      ║ Rec1!     ║");
            log.info("╚══════════════╩═══════════════════════════╩═══════════╝");
        };
    }

    private void seedAdmin() {
        User admin = User.builder()
                .firstName("Super")
                .lastName("Admin")
                .email("admin@hireme.com")
                .password(passwordEncoder.encode("Admin1!"))
                .role(TypeRole.ADMIN)
                .confirmedAt(LocalDateTime.now())
                .build();

        userRepository.save(admin);
    }

    private void seedCandidate() {
        Candidate candidate = Candidate.builder()
                .firstName("Alice")
                .lastName("Martin")
                .email("candidate@hireme.com")
                .password(passwordEncoder.encode("Cand1!"))
                .role(TypeRole.CANDIDATE)
                .confirmedAt(LocalDateTime.now())
                .bio("Développeuse full-stack passionnée par le web et l'IA.")
                .availability(AvailabilityStatus.IMMEDIATE)
                .contractPreferences(Set.of(ContractType.FULL_TIME, ContractType.FREELANCE))
                .desiredJobTitle("Développeuse Full-Stack")
                .autoApplyEnabled(false)
                .openToRelocate(true)
                .build();

        candidateRepository.save(candidate);
    }

    private void seedRecruiter() {
        Recruiter recruiter = Recruiter.builder()
                .firstName("Sophie")
                .lastName("Legrand")
                .email("recruiter@hireme.com")
                .password(passwordEncoder.encode("Rec1!"))
                .role(TypeRole.RECRUITER)
                .confirmedAt(LocalDateTime.now())
                .actualPosition("Responsable RH")
                .phoneNumber(612345678)
                .build();

        recruiterRepository.save(recruiter);
    }
}
