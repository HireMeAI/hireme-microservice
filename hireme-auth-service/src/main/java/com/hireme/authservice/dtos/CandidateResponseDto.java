package com.hireme.authservice.dtos;

import com.hireme.authservice.domain.enums.AvailabilityStatus;
import com.hireme.authservice.domain.enums.ContractType;
import lombok.*;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CandidateResponseDto extends UserResponseDto {

    private String bio;
    private AvailabilityStatus availability;
    private Set<ContractType> contractPreferences;
    private boolean autoApplyEnabled;
    private String desiredJobTitle;
    private boolean openToRelocate;
}
