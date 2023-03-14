package com.example.polls.mapper;

import com.example.polls.codegen.tables.pojos.Roles;
import com.example.polls.model.Role;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
@Component
public interface RolesMapper {

    RolesMapper INSTANCE = Mappers.getMapper(RolesMapper.class);

    @Mapping(target = "", source = "")
    public Role toRole(Roles jOOQRoles);

    @InheritInverseConfiguration
    public Roles fromRole(Role role);

}
