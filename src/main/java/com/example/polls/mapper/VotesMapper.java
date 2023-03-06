package com.example.polls.mapper;

import com.amplibit.codegen.tables.pojos.Votes;
import com.example.polls.model.Choice;
import com.example.polls.model.Poll;
import com.example.polls.model.Vote;
import com.example.polls.repository.PollRepoService;
import com.example.polls.repository.UserRepoService;
import org.mapstruct.Context;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;
import org.mapstruct.factory.Mappers;

import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface VotesMapper {

    VotesMapper INSTANCE = Mappers.getMapper(VotesMapper.class);


    public Vote toVote(Votes jOOQVotes, @Context PollRepoService pollRepo, @Context UserRepoService userRepo);

    @InheritInverseConfiguration
    public Votes fromVote(Vote vote);

    @ObjectFactory
    default Vote lookup(Votes jOOQVotes, @Context PollRepoService pollRepo, @Context UserRepoService userRepo) {
        Vote vote = new Vote();
        Poll poll = pollRepo.findById(jOOQVotes.getPollId()).get();
        vote.setPoll(poll);
        vote.setId(jOOQVotes.getId());
        for (Choice choice : poll.getChoices()) {
            if(choice.getId().equals(jOOQVotes.getChoiceId())){
                vote.setChoice(choice);
                break;
            }
        }

        vote.setCreatedAt(jOOQVotes.getCreatedAt().toInstant(ZoneOffset.UTC));
        vote.setUpdatedAt(jOOQVotes.getUpdatedAt().toInstant(ZoneOffset.UTC));
        vote.setUser(userRepo.findById(jOOQVotes.getUserId()).get());

        return vote;
    }
}
