package com.ktsa.foosball.service;

import com.ktsa.foosball.dto.*;
import com.ktsa.foosball.model.Registration;
import com.ktsa.foosball.model.Teams;
import com.ktsa.foosball.model.Tournaments;
import com.ktsa.foosball.model.Users;
import com.ktsa.foosball.repository.RegistrationRepository;
import com.ktsa.foosball.repository.TeamsRepository;
import com.ktsa.foosball.repository.TournamentRepository;
import com.ktsa.foosball.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
//@RequiredArgsConstructor
public class RegistrationService {

    private TournamentRepository tournamentRepository;
    private UserRepository userRepository;
    private  ModelMapper modelMapper;
    private TeamService teamService;

//    private RegistrationService registrationService;
//    private RegistrationRepository registrationRepository;

    private  RegistrationRepository registrationRepository;

    public String registerPlayer(RegistrationRequestDto registrationRequestDto, Long tournamentId) {

        Tournaments tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found with id: " + tournamentId));

        Users player = userRepository.findByEmail(registrationRequestDto.getPlayerOneEmail())
                .orElseThrow(() -> new RuntimeException("Player not found with id: " + registrationRequestDto.getPlayerOneEmail()));

        // Check if player is already registered
        if (tournament.getPlayers().contains(player)) {
            throw new RuntimeException("Player is already registered in this tournament");
        }

        // Check max participants
        if (tournament.getMaxParticipants() != null &&
                tournament.getPlayers().size() >= tournament.getMaxParticipants()) {
            throw new RuntimeException("Tournament is full. Max participants: " + tournament.getMaxParticipants());
        }

        // Add player to tournament
        tournament.getPlayers().add(player);

        // Save tournament
        tournamentRepository.save(tournament);

        // Create registration entry
        Registration registration = new Registration();
        registration.setPlayer(player);
        registration.setTournamentId(tournamentId);
        registration.setCategory("Single");
        registration.setStatus("REGISTERED");
        registration.setRegisteredAt(String.valueOf(System.currentTimeMillis()));

        registrationRepository.save(registration);

        return "Player registered successfully";
    }

    public String registerTeam(RegistrationRequestDto registrationRequestDto, Long tournamentId) {
        Tournaments tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found with id: " + tournamentId));


        Users player1 = userRepository.findByEmail(registrationRequestDto.getPlayerOneEmail())
                .orElseThrow(() -> new RuntimeException("Player not found with id: " + registrationRequestDto.getPlayerOneEmail()));
        Users player2 = userRepository.findByEmail(registrationRequestDto.getPlayerTwoEmail())
                .orElseThrow(() -> new RuntimeException("Player not found with id: " + registrationRequestDto.getPlayerTwoEmail()));
        Long teamId;
        try {
            teamId = teamService.getTeamByPlayers(player1.getId(),player2.getId()).getTeamId();

        } catch (Exception e){

            TeamRequestDto teamRequestDto = new TeamRequestDto();
            teamRequestDto.setEmailPlayerOne(player1.getEmail());
            teamRequestDto.setEmailPlayerTwo(player2.getEmail());

            TeamResponseDto team = teamService.createTeam(teamRequestDto);
            teamId = teamService.getTeamByPlayers(player1.getId(),player2.getId()).getTeamId();


        }

        Registration registration = new Registration();
        registration.setTeam(teamService.getTeamByPlayers(player1.getId(),player2.getId()));
        registration.setTournamentId(tournamentId);
        registration.setStatus("REGISTERED");
        registration.setRegisteredAt(String.valueOf(System.currentTimeMillis()));

        registrationRepository.save(registration);

        return "Team has been registered successfully";

    }

//    public ResponseEntity<ApiResponse<?>> registerTeam(RegistrationRequestDto dto, Long tournamentId) {
//
//        Tournaments tournament = tournamentRepository.findById(tournamentId)
//                .orElseThrow(() -> new RuntimeException("Tournament not found with id: " + tournamentId));
//
//        Teams team = teamsRepository.findById(dto.getTeamId())
//                .orElseThrow(() -> new RuntimeException("team not found with id: " + dto.getTeamId()));
//
//        // Check if team is already registered
//        if (tournament.getTeams().contains(team)) {
//            throw new RuntimeException("Team is already registered in this tournament");
//        }
//
//        // Check max participants
//        if (tournament.getMaxParticipants() != null &&
//                tournament.getPlayers().size() >= tournament.getMaxParticipants()) {
//            throw new RuntimeException("Tournament is full. Max participants: " + tournament.getMaxParticipants());
//        }
//
//        tournament.getTeams().add(team);
//        tournamentRepository.save(tournament);
//
//        Registration registration= modelMapper.map(dto, Registration.class);
//        registration.setTournamentId(tournamentId);
//
//        Registration registeredPlayerOrTeam = registrationRepository.save(registration);
//        return ResponseEntity.ok(
//                ApiResponse.success(200, "Team registered successfully", modelMapper.map(registeredPlayerOrTeam, RegistrationResponseDto.class))
//        );
//
//    }
}
