package com.example.polls.repository;

import com.example.polls.codegen.tables.daos.VotesDao;
import com.example.polls.codegen.tables.pojos.Votes;
import com.example.polls.codegen.tables.records.VotesRecord;
import com.example.polls.mapper.ChoiceVoteCountMapper;
import com.example.polls.mapper.VotesMapper;
import com.example.polls.model.ChoiceVoteCount;
import com.example.polls.model.Vote;
import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.Result;
import org.jooq.SortField;
import org.jooq.TableField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.polls.codegen.tables.Polls.POLLS;
import static com.example.polls.codegen.tables.Votes.VOTES;
import static org.jooq.impl.DSL.count;
import static org.jooq.impl.DSL.user;

@Repository
public class VoteRepoService {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private VotesDao votesDao;

    @Autowired
    private VotesMapper votesMapper;

    @Autowired
    private ChoiceVoteCountMapper choiceVoteCountMapper;

    @Autowired
    private PollRepoService pollRepoService;

    @Autowired
    private UserRepoService userRepoService;

    @Query("SELECT NEW com.example.polls.model.ChoiceVoteCount(v.choice.id, count(v.id)) FROM Vote v WHERE v.poll.id in :pollIds GROUP BY v.choice.id")
    public List<ChoiceVoteCount> countByPollIdInGroupByChoiceId(@Param("pollIds") List<Long> pollIds){
        Result<Record2<Long, Integer>> result = dslContext.select(VOTES.CHOICE_ID, count(VOTES.ID))
                .from(VOTES).where(VOTES.POLL_ID.in(pollIds)).groupBy(VOTES.CHOICE_ID).fetch();

        return result.stream().map(choiceVoteCountMapper::toChoiceVoteCount).collect(Collectors.toList());
    }

    @Query("SELECT NEW com.example.polls.model.ChoiceVoteCount(v.choice.id, count(v.id)) FROM Vote v WHERE v.poll.id = :pollId GROUP BY v.choice.id")
    public List<ChoiceVoteCount> countByPollIdGroupByChoiceId(@Param("pollId") Long pollId){
        Result<Record2<Long, Integer>> result = dslContext.select(VOTES.CHOICE_ID, count(VOTES.ID))
                .from(VOTES).where(VOTES.POLL_ID.eq(pollId)).groupBy(VOTES.CHOICE_ID).fetch();

        return result.stream().map(choiceVoteCountMapper::toChoiceVoteCount).collect(Collectors.toList());
    }

    @Query("SELECT v FROM Vote v where v.user.id = :userId and v.poll.id in :pollIds")
    public List<Vote> findByUserIdAndPollIdIn(@Param("userId") Long userId, @Param("pollIds") List<Long> pollIds){
        return dslContext.select(VOTES).where(VOTES.USER_ID.eq(userId).and(VOTES.POLL_ID.in(pollIds)))
                .fetchInto(Votes.class).stream().map(votes -> votesMapper.toVote(votes, pollRepoService, userRepoService)).collect(Collectors.toList());
    }

    @Query("SELECT v FROM Vote v where v.user.id = :userId and v.poll.id = :pollId")
    public Vote findByUserIdAndPollId(@Param("userId") Long userId, @Param("pollId") Long pollId){
        return votesMapper.toVote(dslContext.select(VOTES).where(VOTES.USER_ID.eq(userId).and(VOTES.POLL_ID.eq(pollId)))
                .fetchOneInto(Votes.class), pollRepoService, userRepoService);
    }

    @Query("SELECT COUNT(v.id) from Vote v where v.user.id = :userId")
    public long countByUserId(@Param("userId") Long userId){
        return votesDao.fetchByUserId(userId).size();
    }

    @Query("SELECT v.poll.id FROM Vote v WHERE v.user.id = :userId")
    public Page<Long> findVotedPollIdsByUserId(@Param("userId") Long userId, Pageable pageable){
        List<Long> pollIds = dslContext.select(count(VOTES.POLL_ID)).where(VOTES.USER_ID.eq(userId))
                .orderBy(getSortFields(pageable.getSort()))
                .limit(pageable.getPageSize()).offset(pageable.getOffset())
                .fetchInto(Long.class);

        return new PageImpl<>(pollIds, pageable, pollIds.size());
    }

    public Vote save(Vote vote){
        VotesRecord record = dslContext.newRecord(VOTES, votesMapper.fromVote(vote));
        vote = votesMapper.toVote(dslContext.insertInto(VOTES).set(record).returning().fetchOneInto(Votes.class), pollRepoService, userRepoService);
        return vote;
    }

    private Collection<SortField<?>> getSortFields(Sort sortSpecification) {
        Collection<SortField<?>> querySortFields = new ArrayList<>();

        if (sortSpecification == null) {
            return querySortFields;
        }

        for (Sort.Order specifiedField : sortSpecification) {
            String sortFieldName = specifiedField.getProperty();
            Sort.Direction sortDirection = specifiedField.getDirection();

            TableField tableField = getTableField(sortFieldName);
            SortField<?> querySortField = convertTableFieldToSortField(tableField, sortDirection);
            querySortFields.add(querySortField);
        }

        return querySortFields;
    }

    private TableField getTableField(String sortFieldName) {
        TableField sortField;
        try {
            Field tableField = POLLS.getClass().getField(sortFieldName);
            sortField = (TableField) tableField.get(POLLS);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            String errorMessage = String.format("Could not find table field: {}", sortFieldName);
            throw new InvalidDataAccessApiUsageException(errorMessage, ex);
        }

        return sortField;
    }

    private SortField<?> convertTableFieldToSortField(TableField tableField, Sort.Direction sortDirection) {
        if (sortDirection == Sort.Direction.ASC) {
            return tableField.asc();
        } else {
            return tableField.desc();
        }
    }
}

