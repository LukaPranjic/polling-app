package com.example.polls.repository;

import com.example.polls.codegen.tables.daos.RolesDao;
import com.example.polls.mapper.RolesMapper;
import com.example.polls.model.Role;
import com.example.polls.model.RoleName;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class RoleRepoService {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private RolesDao rolesDao;
    @Autowired
    private RolesMapper rolesMapper;

    public Optional<Role> findByName(RoleName roleName){
        return Optional.of(rolesMapper.toRole(rolesDao.fetchOneByName(roleName.name())));
    }
}
