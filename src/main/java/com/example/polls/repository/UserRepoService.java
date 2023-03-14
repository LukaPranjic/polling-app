package com.example.polls.repository;

import com.example.polls.codegen.tables.daos.UsersDao;
import com.example.polls.codegen.tables.pojos.Users;
import com.example.polls.codegen.tables.records.UsersRecord;
import com.example.polls.mapper.UsersMapper;
import com.example.polls.model.User;
import org.jooq.DSLContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.polls.codegen.Tables.USERS;

@Service
@Order(2)
public class UserRepoService {


    private final DSLContext dslContext;


    private final UsersDao usersDao;


    private final UsersMapper usersMapper;

    public UserRepoService(DSLContext dslContext, UsersDao usersDao, UsersMapper usersMapper) {
        this.dslContext = dslContext;
        this.usersDao = usersDao;
        this.usersMapper = usersMapper;
    }

    Optional<User> findByEmail(String email) {
        return Optional.of(usersMapper.toUser(usersDao.fetchOneByEmail(email)));
    }

    public Optional<User> findByUsernameOrEmail(String username, String email) {
        return Optional.of(usersMapper.toUser(Objects.requireNonNull(dslContext.fetchOne(USERS, USERS.USERNAME.eq(username).or(USERS.EMAIL.eq(email))))
                .into(Users.class)));
    }

    public List<User> findByIdIn(List<Long> userIds) {
        return usersDao.fetchById(userIds.toArray(new Long[0])).stream().map(usersMapper::toUser).collect(Collectors.toList());
    }

    public Optional<User> findByUsername(String username) {
        return Optional.of(usersMapper.toUser(usersDao.fetchOneByUsername(username)));
    }

    public Boolean existsByUsername(String username) {
        return dslContext.fetchExists(USERS, USERS.USERNAME.eq(username));
    }

    public Boolean existsByEmail(String email) {
        return dslContext.fetchExists(USERS, USERS.EMAIL.eq(email));
    }

    public Optional<User> findById(Long createdBy) {
        return Optional.of(usersMapper.toUser(usersDao.findById(createdBy)));
    }

    public User getOne(Long id) {
        return usersMapper.toUser(usersDao.findById(id));
    }

    public User save(User user) {
        UsersRecord record = dslContext.newRecord(USERS, usersMapper.fromUser(user));
        user = usersMapper.toUser(dslContext.insertInto(USERS).set(record).returning().fetchOneInto(Users.class));
        return user;
    }
}
