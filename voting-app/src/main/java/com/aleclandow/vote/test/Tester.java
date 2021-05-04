package com.aleclandow.vote.test;

import static com.aleclandow.util.ApplicationProperties.applicationProperties;


import com.aleclandow.vote.voter.Voter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class Tester {
    public static List<String> candidateIds = Arrays.asList("M001", "T001");

    private final Integer numberOfTestVoters;

    private Integer testId;

    public Tester(Integer numberOfTestVoters) throws Exception {
        // https://www.baeldung.com/java-generating-random-numbers-in-range
        this.numberOfTestVoters = numberOfTestVoters;
        this.testId = (new Random())
            .ints(1000000, 9999999)
            .findFirst()
            .getAsInt();
    }

    public List<Long> testBlocking() throws Exception {
        // https://www.baeldung.com/java-random-list-element
        Random rand = new Random();
        List<Long> voteTimesInMillis = new ArrayList<>();
        for (int i = 0; i < numberOfTestVoters; i++) {
            String voterId = testId + "." + i;
            Voter voter = new Voter(voterId);

            String randomCandidateToVoteFor = candidateIds.get(rand.nextInt(candidateIds.size()));
            Long voteTimeInMillis = voter.voteForCandidate(randomCandidateToVoteFor);
            voteTimesInMillis.add(voteTimeInMillis);
        }

        return voteTimesInMillis;
    }

    public List<Long> testParallel() throws Exception {
        // https://www.baeldung.com/java-random-list-element
        Random rand = new Random();
        List<Voter> votersWithCandidatesToVoteFor = new ArrayList<>();

        for (int i = 0; i < numberOfTestVoters; i++) {
            String voterId = testId + "." + i;
            Voter voter = new Voter(voterId);

            String randomCandidateToVoteFor = candidateIds.get(rand.nextInt(candidateIds.size()));
            voter.setCandidateToVoteFor(randomCandidateToVoteFor);
            votersWithCandidatesToVoteFor.add(voter);
        }

        // https://www.baeldung.com/spring-webclient-simultaneous-calls
        return Flux.fromIterable(votersWithCandidatesToVoteFor)
            .parallel(numberOfTestVoters)
            .runOn(Schedulers.boundedElastic())
            .flatMap(voter -> Mono.fromCallable(voter::vote))
            .sequential()
            .collectList()
            .block();

    }


}
