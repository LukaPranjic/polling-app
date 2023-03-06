package com.example.polls.mapper;

import com.example.polls.model.ChoiceVoteCount;
import org.jooq.Record2;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ChoiceVoteCountMapper {

    ChoiceVoteCountMapper INSTANCE = Mappers.getMapper(ChoiceVoteCountMapper.class);

    @Mapping(target = "choiceId", expression = "java( jooqResult.value1() )")
    @Mapping(target = "voteCount", expression = "java( jooqResult.value2() )")
    public ChoiceVoteCount toChoiceVoteCount(Record2<Long, Integer> jooqResult);
}
