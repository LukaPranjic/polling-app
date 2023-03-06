package com.example.polls.repository;

import com.amplibit.codegen.tables.daos.UsersDao;
import com.amplibit.codegen.tables.pojos.Users;
import com.amplibit.codegen.tables.records.UsersRecord;
import com.example.polls.mapper.UsersMapper;
import com.example.polls.model.User;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.amplibit.codegen.Tables.USERS;

@Service
public class UserRepoService {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private UsersDao usersDao;

    @Autowired
    private UsersMapper usersMapper;

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
