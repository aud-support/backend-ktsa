package com.ktsa.foosball.service;

import com.ktsa.foosball.dto.MatchRequestDto;
import com.ktsa.foosball.dto.MatchResponseDto;
import com.ktsa.foosball.exception.BadRequestException;
import com.ktsa.foosball.model.Matches;
import com.ktsa.foosball.model.Teams;
import com.ktsa.foosball.model.Tournaments;
import com.ktsa.foosball.model.Users;
import com.ktsa.foosball.repository.MatchRepository;
import com.ktsa.foosball.repository.TeamsRepository;
import com.ktsa.foosball.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class MatchService {


    private final TournamentRepository tournamentRepository;
    private final ModelMapper modelMapper;
    private final MatchRepository matchRepository;
    private final TeamsRepository teamsRepository;



    public MatchResponseDto createMatch(Long tournamentId, MatchRequestDto request) {

        Tournaments tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found"));

        Matches match = new Matches();
        match.setTournament(tournament);

        /*
         * SINGLES MATCH
         */
        if (request.getPlayerOne() != null && request.getPlayerTwo() != null) {

            if (request.getPlayerOne().equals(request.getPlayerTwo())) {
                throw new BadRequestException("Player cannot play against themselves.");
            }

            boolean exists =
                    matchRepository.existsByTournamentIdAndPlayerOneIdAndPlayerTwoId(
                            tournamentId,
                            request.getPlayerOne(),
                            request.getPlayerTwo())
                            ||
                            matchRepository.existsByTournamentIdAndPlayerOneIdAndPlayerTwoId(
                                    tournamentId,
                                    request.getPlayerTwo(),
                                    request.getPlayerOne());

            if (exists) {
                throw new BadRequestException("This match already exists.");
            }

            Users playerOne = tournament.getPlayers()
                    .stream()
                    .filter(u -> u.getId().equals(request.getPlayerOne()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Player One not found in tournament"));

            Users playerTwo = tournament.getPlayers()
                    .stream()
                    .filter(u -> u.getId().equals(request.getPlayerTwo()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Player Two not found in tournament"));

            match.setPlayerOne(playerOne);
            match.setPlayerTwo(playerTwo);
        }

        /*
         * TEAM MATCH
         */
        else if (request.getTeamOne() != null && request.getTeamTwo() != null) {

            if (request.getTeamOne().equals(request.getTeamTwo())) {
                throw new BadRequestException("Team cannot play against itself.");
            }

            boolean exists =
                    matchRepository.existsByTournamentIdAndTeamOneTeamIdAndTeamTwoTeamId(
                            tournamentId,
                            request.getTeamOne(),
                            request.getTeamTwo())
                            ||
                            matchRepository.existsByTournamentIdAndTeamOneTeamIdAndTeamTwoTeamId(
                                    tournamentId,
                                    request.getTeamTwo(),
                                    request.getTeamOne());

            if (exists) {
                throw new BadRequestException("This team match already exists.");
            }

            Teams teamOne = teamsRepository.findById(request.getTeamOne())
                    .orElseThrow(() -> new RuntimeException("Team One not found"));

            Teams teamTwo = teamsRepository.findById(request.getTeamTwo())
                    .orElseThrow(() -> new RuntimeException("Team Two not found"));

            match.setTeamOne(teamOne);
            match.setTeamTwo(teamTwo);
        }

        else {
            throw new BadRequestException("Either players or teams must be selected.");
        }

        match.setStage(request.getStage());
        match.setStatus(request.getStatus());
        match.setScheduledAt(request.getScheduledAt());
        match.setRoundNumber(request.getRoundNumber());

        Matches saved = matchRepository.save(match);

        MatchResponseDto response = modelMapper.map(saved, MatchResponseDto.class);

        response.setTournamentId(saved.getTournament().getId());

        if (saved.getPlayerOne() != null) {
            Users playerOne = saved.getPlayerOne();
            response.setPlayerOneName(playerOne.getName());
            response.setPlayerTwoName(saved.getPlayerTwo().getName());
        }

        if (saved.getTeamOne() != null) {
            response.setTeamOneName(saved.getTeamOne().getTeamName());
            response.setTeamTwoName(saved.getTeamTwo().getTeamName());
        }

        return response;
    }

    public List<MatchResponseDto> getAllMatches(Long tournamentId) {

        tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found"));

        return matchRepository.findByTournamentId(tournamentId).stream().map(
                matches -> modelMapper.map(matches, MatchResponseDto.class)).toList();
    }
}
