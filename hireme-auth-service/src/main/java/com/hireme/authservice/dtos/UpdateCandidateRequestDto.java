package com.hireme.authservice.dtos;

import com.hireme.authservice.domain.enums.AvailabilityStatus;
import com.hireme.authservice.domain.enums.ContractType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UpdateCandidateRequestDto extends UpdateUserRequestDto {
    private String bio;
    private AvailabilityStatus availability;
    private Set<ContractType> contractPreferences;
    private boolean autoApplyEnabled;
    private String desiredJobTitle;
    private boolean openToRelocate;
}
