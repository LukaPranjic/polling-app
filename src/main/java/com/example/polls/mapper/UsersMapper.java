package com.example.polls.mapper;

import com.example.polls.codegen.tables.daos.RolesDao;
import com.example.polls.codegen.tables.daos.UserRolesDao;
import com.example.polls.codegen.tables.pojos.Users;
import com.example.polls.model.Role;
import com.example.polls.model.RoleName;
import com.example.polls.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Mapper()
@Order()
public abstract class UsersMapper {

    private final RolesDao rolesDao;

    private final UserRolesDao userRolesDao;

    UsersMapper INSTANCE = Mappers.getMapper(UsersMapper.class);

    protected UsersMapper(RolesDao rolesDao, UserRolesDao userRolesDao) {
        this.rolesDao = rolesDao;
        this.userRolesDao = userRolesDao;
    }

    @Mapping(target = "roles", expression = "java( mapAuthorities(jooqUsers) )")
    public abstract User toUser(Users jooqUsers);


    public abstract Users fromUser(User user);

    public Instant toInstant(LocalDateTime localDateTime){
        return localDateTime.toInstant(ZoneOffset.UTC);
    }

    private Set<Role> mapAuthorities(Users jooqUsers){
        Long userId = jooqUsers.getId();
        List<Long> roleIds = userRolesDao.fetchByUserId(userId).stream().map(pair -> pair.getRoleId()).toList();
        Set<Role> roles = rolesDao.fetchById(roleIds.toArray(new Long[0])).stream().map(jooqRole -> {
            Role role = new Role();
            role.setId(jooqRole.getId());
            role.setName(RoleName.valueOf(jooqRole.getName()));
            return role;
        }).collect(Collectors.toSet());
        return roles;
    }
}
