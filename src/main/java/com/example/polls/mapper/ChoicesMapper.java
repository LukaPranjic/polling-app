package com.example.polls.mapper;

import com.example.polls.codegen.tables.pojos.Choices;
import com.example.polls.codegen.tables.pojos.Votes;
import com.example.polls.model.Choice;
import com.example.polls.model.Poll;
import com.example.polls.model.Vote;
import com.example.polls.repository.PollRepoService;
import org.mapstruct.Context;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
@Component
public interface ChoicesMapper {

    ChoicesMapper INSTANCE = Mappers.getMapper(ChoicesMapper.class);

    Choice toChoice(Choices jooqChoices);

    @InheritInverseConfiguration
    Choices fromChoice(Choice choice);


    @ObjectFactory
    default Choice lookup(Choices jooqChoices, @Context PollRepoService repo) {
        Choice choice = new Choice();
        Poll poll = repo.findById(jooqChoices.getPollId()).get();
        choice.setPoll(poll);
        choice.setId(jooqChoices.getId());
        choice.setText(jooqChoices.getText());

        return choice;
    }
}
