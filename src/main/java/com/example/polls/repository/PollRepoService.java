package com.example.polls.repository;


import com.example.polls.codegen.tables.daos.ChoicesDao;
import com.example.polls.codegen.tables.daos.PollsDao;
import com.example.polls.codegen.tables.pojos.Choices;
import com.example.polls.codegen.tables.pojos.Polls;
import com.example.polls.codegen.tables.records.ChoicesRecord;
import com.example.polls.codegen.tables.records.PollsRecord;
import com.example.polls.mapper.ChoicesMapper;
import com.example.polls.mapper.PollsMapper;
import com.example.polls.model.Choice;
import com.example.polls.model.Poll;
import org.jooq.DSLContext;
import org.jooq.Rows;
import org.jooq.SortField;
import org.jooq.TableField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.polls.codegen.Tables.CHOICES;
import static com.example.polls.codegen.Tables.POLLS;

@Service
public class PollRepoService {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private PollsDao pollsDao;
    @Autowired
    private PollsMapper pollsMapper;

    @Autowired
    private ChoicesDao choicesDao;
    @Autowired
    private ChoicesMapper choicesMapper;


    public Optional<Poll> findById(Long pollId) {

        /*Optional<Poll> test = dslContext.select(Tables.POLLS,
                multiset(
                        select(Tables.CHOICES)
                        .from(Tables.CHOICES)
                        .where(Tables.CHOICES.POLL_ID.eq(Tables.POLLS.ID))
                ).convertFrom(r -> ChoicesMapper.INSTANCE.toChoice(r.into(Choices.class)))
        ).from(Tables.POLLS).fetchOptional(record -> PollsMapper.INSTANCE.toPoll(record.into(Polls.class)));*/

        Optional<Poll> poll = dslContext.selectFrom(POLLS).where(POLLS.ID.eq(pollId))
                .fetchOptional(record -> pollsMapper.toPoll(record.into(Polls.class)));
        if (poll.isEmpty()) {
            return poll;
        }
        List<Choice> choices = fetchChoices(pollId);

        poll.get().setChoices(choices);

        return poll;

    }

    public Page<Poll> findByCreatedBy(Long userId, Pageable pageable) {
        List<Poll> polls = dslContext.select(POLLS).where(POLLS.CREATED_BY.eq(userId))
                .orderBy(getSortFields(pageable.getSort()))
                .limit(pageable.getPageSize()).offset(pageable.getOffset())
                .fetchInto(Polls.class).stream().map(pollsMapper::toPoll).collect(Collectors.toList());

        attachChoices(polls);

        return new PageImpl<>(polls, pageable, polls.size());
    }

    public long countByCreatedBy(Long userId) {
        return dslContext.fetchCount(POLLS, POLLS.CREATED_BY.eq(userId));
    }

    public List<Poll> findByIdIn(List<Long> pollIds) {
        return pollsDao.fetchById(pollIds.toArray(new Long[0])).stream().map(pollsMapper::toPoll).collect(Collectors.toList());
    }

    public List<Poll> findByIdIn(List<Long> pollIds, Sort sort) {

        List<Poll> polls = dslContext.select(POLLS).where(POLLS.ID.in(pollIds)).orderBy(getSortFields(sort))
                .fetchInto(Polls.class).stream().map(pollsMapper::toPoll).collect(Collectors.toList());

        attachChoices(polls);

        return polls;

    }

    public Page<Poll> findAll(Pageable pageable) {
        List<Poll> polls = dslContext.select(POLLS).orderBy(getSortFields(pageable.getSort()))
                .limit(pageable.getPageSize()).offset(pageable.getOffset())
                .fetchInto(Polls.class).stream().map(pollsMapper::toPoll).collect(Collectors.toList());

        attachChoices(polls);

        return new PageImpl<>(polls, pageable, polls.size());
    }

    public Poll save(Poll poll) {
        Stream<ChoicesRecord> choices = poll.getChoices().stream().map(choicesMapper::fromChoice).map(choice -> dslContext.newRecord(CHOICES, choice));
        List<Choice> saved = dslContext.insertInto(CHOICES, CHOICES.POLL_ID, CHOICES.TEXT)
                .valuesOfRows(choices
                        .collect(Rows.toRowList(ChoicesRecord::field3, ChoicesRecord::field2))) //not the best solution since methods are called "fields"
                .returning().stream().map(record -> record.into(Choices.class)).map(choicesMapper::toChoice)
                .collect(Collectors.toList());

        PollsRecord record = dslContext.newRecord(POLLS, pollsMapper.fromPoll(poll));

        poll = pollsMapper.toPoll(dslContext.insertInto(POLLS).set(record).returning().fetchOneInto(Polls.class));

        poll.setChoices(saved);

        return poll;
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

    public List<Choice> fetchChoices(Long pollId) {
        return choicesDao.fetchById(pollId).stream().map(choicesMapper::toChoice).collect(Collectors.toList());
    }

/*    private ChoicesRecord createRecord(Choices jOOQChoices){
        ChoicesRecord record = new ChoicesRecord();

        record.setPollId(jOOQChoices.getPollId());
        record.setText(jOOQChoices.getText());

        return record;
    }*/

    private void attachChoices(List<Poll> polls) {
        List<Choice> choices;

        for (Poll poll : polls) {
            choices = fetchChoices(poll.getId());
            poll.setChoices(choices);
        }
    }
}
