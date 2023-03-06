package com.example.polls.mapper;

import com.amplibit.codegen.tables.pojos.Roles;
import com.example.polls.model.Role;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface RolesMapper {

    RolesMapper INSTANCE = Mappers.getMapper(RolesMapper.class);

    @Mapping(target = "", source = "")
    public Role toRole(Roles jOOQRoles);

    @InheritInverseConfiguration
    public Roles fromRole(Role role);

}
