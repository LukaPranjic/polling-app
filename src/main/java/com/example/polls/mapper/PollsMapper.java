package com.example.polls.mapper;


import com.example.polls.codegen.tables.pojos.Polls;
import com.example.polls.model.Choice;
import com.example.polls.model.Poll;
import com.example.polls.repository.PollRepoService;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Mapper(uses = {ChoicesMapper.class}, componentModel = "spring")
@Component

public abstract class PollsMapper {

    @Autowired
    private PollRepoService pollRepoService;

    PollsMapper INSTANCE = Mappers.getMapper(PollsMapper.class);

    @Mapping(target = "choices", expression = "java( retrieveChoices(jooqPolls) )")
    public abstract Poll toPoll(Polls jooqPolls);

    @InheritInverseConfiguration
    public abstract Polls fromPoll(Poll poll);


    public Instant toInstant(LocalDateTime localDateTime){
        return localDateTime.toInstant(ZoneOffset.UTC);
    }

    public List<Choice> retrieveChoices(Polls jooqPolls){
        return pollRepoService.fetchChoices(jooqPolls.getId());
    }
}
