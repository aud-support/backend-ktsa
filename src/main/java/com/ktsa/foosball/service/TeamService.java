package com.ktsa.foosball.service;

import com.ktsa.foosball.dto.ApiResponse;
import com.ktsa.foosball.dto.TeamRequestDto;
import com.ktsa.foosball.dto.TeamResponseDto;
import com.ktsa.foosball.dto.UserTeamResponseDto;
import com.ktsa.foosball.exception.BadRequestException;
import com.ktsa.foosball.exception.ResourceNotFoundException;
import com.ktsa.foosball.model.Teams;
import com.ktsa.foosball.model.Tournaments;
import com.ktsa.foosball.model.Users;
import com.ktsa.foosball.repository.RegistrationRepository;
import com.ktsa.foosball.repository.TeamsRepository;
import com.ktsa.foosball.repository.TournamentRepository;
import com.ktsa.foosball.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import org.modelmapper.ModelMapper;

import java.lang.reflect.Type;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TeamService {


    private final ModelMapper modelMapper;
    private final TeamsRepository teamsRepository;
    private final UserRepository userRepository;
    private final RegistrationRepository registrationRepository;
    private final TournamentRepository tournamentRepository;


    public TeamResponseDto  createTeam(TeamRequestDto teamRequestDTO) {

        String emailPlayerOne = teamRequestDTO.getEmailPlayerOne();
        String emailPlayerTwo = teamRequestDTO.getEmailPlayerTwo();

        Users userOne = userRepository.findByEmail(emailPlayerOne)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid credentials for Player One"));

        Users userTwo = userRepository.findByEmail(emailPlayerTwo)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid credentials for Player Two"));

        if (userOne.getId().equals(userTwo.getId())) {
            throw new BadRequestException("You cannot create a team with yourself");
        }

        // If a team already exists for this player pair, return it instead of creating a duplicate
        Teams existingTeam = getTeamByPlayers(userOne.getId(), userTwo.getId());
        if (existingTeam != null) {
            // Player pair already has a team — enforce the uniqueness rule
            throw new BadRequestException(
                    "A team already exists for these two players: \"" + existingTeam.getTeamName() +
                    "\". A player pair can only have one team."
            );
        }

        // Determine team name: use provided name or auto-generate
        String teamName;
        if (teamRequestDTO.getTeamName() != null && !teamRequestDTO.getTeamName().isBlank()) {
            teamName = teamRequestDTO.getTeamName().trim();
            if (teamsRepository.existsByTeamName(teamName)) {
                throw new BadRequestException("Team name '" + teamName + "' is already taken. Please choose a different name.");
            }
        } else {
            teamName = userOne.getName() + " & " + userTwo.getName();
        }

        Teams teams = modelMapper.map(teamRequestDTO, Teams.class);

        teams.setTeamName(teamName);
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

    public List<TeamResponseDto> searchTeams(String query) {
        return teamsRepository.findByTeamNameContainingIgnoreCase(query).stream()
                .map(team -> modelMapper.map(team, TeamResponseDto.class))
                .collect(Collectors.toList());
    }

    public Teams getPlayerById(Long id) {
        return (Teams) teamsRepository.findByPlayerOneId(id).stream()
                .map(team -> modelMapper.map(team, TeamResponseDto.class));
    }

    public Teams getTeamById(Long id) {
        return teamsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + id));
    }

//    public Teams getTeamByPlayers(Long id, Long id1) {
//        return teamsRepository
//                .findByPlayerOneIdAndPlayerTwoId(id, id1)
//                .orElseThrow(() ->
//                        new RuntimeException("Team not found"));
//    }

    public Teams getTeamByPlayers(Long playerOneId, Long playerTwoId) {

        return teamsRepository
                .findTeamByPlayers(playerOneId, playerTwoId)
                .orElse(null);
    }

    /**
     * Looks up a team by two player emails (order-independent).
     * Returns null if no team exists for this pair — used by the frontend before registration.
     */
    public TeamResponseDto getTeamByPlayerEmails(String emailOne, String emailTwo) {
        Users userOne = userRepository.findByEmail(emailOne)
                .orElseThrow(() -> new ResourceNotFoundException("No account found for email: " + emailOne));
        Users userTwo = userRepository.findByEmail(emailTwo)
                .orElseThrow(() -> new ResourceNotFoundException("No account found for email: " + emailTwo));

        Teams team = getTeamByPlayers(userOne.getId(), userTwo.getId());
        return team != null ? modelMapper.map(team, TeamResponseDto.class) : null;
    }

    /**
     * Returns an existing team for the given player pair, or creates a new one.
     * This is the safe entry point used during registration to prevent duplicate teams.
     */
    public Teams findOrCreateTeam(Users playerOne, Users playerTwo, String teamName) {
        Teams existing = getTeamByPlayers(playerOne.getId(), playerTwo.getId());
        if (existing != null) {
            return existing;
        }

        String resolvedName;
        if (teamName != null && !teamName.isBlank()) {
            resolvedName = teamName.trim();
            if (teamsRepository.existsByTeamName(resolvedName)) {
                throw new BadRequestException(
                        "Team name \"" + resolvedName + "\" is already taken. Please choose a different name."
                );
            }
        } else {
            resolvedName = playerOne.getName()+"_"+ playerOne.getId() + " & " + playerTwo.getName()+"_"+playerTwo.getId();
        }

        Teams team = new Teams();
        team.setTeamName(resolvedName);
        team.setPlayerOne(playerOne);
        team.setPlayerTwo(playerTwo);
        return teamsRepository.save(team);
    }

    /** Returns all teams a user is part of, with tournament registration info. */
    public List<UserTeamResponseDto> getTeamsByUser(Long userId) {
        return teamsRepository.findAllByUserId(userId).stream()
                .map(team -> toUserTeamDto(team, userId))
                .toList();
    }

    private UserTeamResponseDto toUserTeamDto(Teams team, Long userId) {
        UserTeamResponseDto dto = new UserTeamResponseDto();
        dto.setId(team.getTeamId());
        dto.setTeamName(team.getTeamName());

        boolean isPlayerOne = team.getPlayerOne() != null
                && userId.equals(team.getPlayerOne().getId());
        dto.setUserRole(isPlayerOne ? "playerOne" : "playerTwo");

        Users partner = isPlayerOne ? team.getPlayerTwo() : team.getPlayerOne();
        if (partner != null) {
            UserTeamResponseDto.PartnerDto p = new UserTeamResponseDto.PartnerDto();
            p.setId(partner.getId());
            p.setName(partner.getName());
            p.setEmail(partner.getEmail());
            dto.setPartner(p);
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<UserTeamResponseDto.TeamTournamentDto> tournaments =
                registrationRepository.findAllByTeamId(team.getTeamId()).stream()
                        .map(r -> {
                            UserTeamResponseDto.TeamTournamentDto t = new UserTeamResponseDto.TeamTournamentDto();
                            t.setTournamentId(r.getTournamentId());
                            t.setCategory(r.getCategory());
                            Tournaments tournament = tournamentRepository.findById(r.getTournamentId()).orElse(null);
                            if (tournament != null) {
                                t.setTournamentName(tournament.getTournamentName());
                                t.setStartDate(tournament.getStartDate() != null
                                        ? tournament.getStartDate().format(fmt) : null);
                                t.setStatus(tournament.getStatus() != null
                                        ? tournament.getStatus().name() : "UPCOMING");
                            }
                            return t;
                        })
                        .toList();
        dto.setTournaments(tournaments);
        return dto;
    }
}
