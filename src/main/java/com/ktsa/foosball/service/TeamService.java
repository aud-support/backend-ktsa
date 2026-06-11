package com.ktsa.foosball.service;

import com.ktsa.foosball.dto.ApiResponse;
import com.ktsa.foosball.dto.TeamRequestDto;
import com.ktsa.foosball.dto.TeamResponseDto;
import com.ktsa.foosball.exception.ResourceNotFoundException;
import com.ktsa.foosball.model.Teams;
import com.ktsa.foosball.model.Users;
import com.ktsa.foosball.repository.TeamsRepository;
import com.ktsa.foosball.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import org.modelmapper.ModelMapper;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TeamService {


    private final ModelMapper modelMapper;
    private final TeamsRepository teamsRepository;
    private final UserRepository userRepository;


    public TeamResponseDto  createTeam(TeamRequestDto teamRequestDTO) {

        String emailPlayerOne = teamRequestDTO.getEmailPlayerOne();
        String emailPlayerTwo = teamRequestDTO.getEmailPlayerTwo();

        Users userOne = userRepository.findByEmail(emailPlayerOne)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid credentials for Player One"));

        Users userTwo = userRepository.findByEmail(emailPlayerTwo)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid credentials for Player Two"));

        if (userOne.getId().equals(userTwo.getId())) {
            throw new RuntimeException("Please partner with a different player");
        }

//        if (teamsRepository.existsByPlayerOneAndPlayerTwo(userOne, userTwo) ||
//                teamsRepository.existsByPlayerOneAndPlayerTwo(userTwo, userOne)) {
//            return ResponseEntity.badRequest().body(
//                    ApiResponse.error(400, "This team pairing already exists")
//            );
//        }

        Teams teams = modelMapper.map(teamRequestDTO, Teams.class);

        teams.setTeamName(userOne.getName() + " & " + userTwo.getName());
        teams.setPlayerOne(userOne);
        teams.setPlayerTwo(userTwo);



        Teams createdTeam = teamsRepository.save(teams);

        return modelMapper.map(createdTeam,TeamResponseDto.class);
    }


    public List<TeamResponseDto> getAllTeams() {
        return teamsRepository.findAll().stream()
                .map(team -> modelMapper.map(team, TeamResponseDto.class))
                .collect(Collectors.toList());
    }

    public Teams getPlayerById(Long id) {
        return (Teams) teamsRepository.findByPlayerOneId(id).stream()
                .map(team -> modelMapper.map(team, TeamResponseDto.class));
    }

    public Teams getTeamByPlayers(Long id, Long id1) {
        return teamsRepository
                .findByPlayerOneIdAndPlayerTwoId(id, id1)
                .orElseThrow(() ->
                        new RuntimeException("Team not found"));
    }
}
