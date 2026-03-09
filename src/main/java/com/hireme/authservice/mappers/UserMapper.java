package com.hireme.authservice.mappers;

import com.hireme.authservice.domain.entities.Candidate;
import com.hireme.authservice.domain.entities.User;
import com.hireme.authservice.dtos.CandidateResponseDto;
import com.hireme.authservice.dtos.UpdateCandidateRequestDto;
import com.hireme.authservice.dtos.UserResponseDto;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @SubclassMapping(source = Candidate.class, target = CandidateResponseDto.class)
    UserResponseDto mapToDto(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCandidateFromDto(UpdateCandidateRequestDto dto, @MappingTarget Candidate candidate);

    UserResponseDto toUserDto(User user);

    CandidateResponseDto toCandidateDto(Candidate candidate);


}
