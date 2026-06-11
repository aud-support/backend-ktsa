package com.ktsa.foosball.service;

import com.ktsa.foosball.dto.ApiResponse;
import com.ktsa.foosball.dto.TournamentRequestDTO;
import com.ktsa.foosball.dto.TournamentResponseDTO;
import com.ktsa.foosball.exception.ResourceNotFoundException;
import com.ktsa.foosball.model.Media;
import com.ktsa.foosball.model.Tournaments;
import com.ktsa.foosball.repository.MediaRepository;
import com.ktsa.foosball.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TournamentService {

    private final TournamentRepository tournamentRepository;
    private final ModelMapper modelMapper;
    private final S3Service s3Service;
    private final MediaRepository mediaRepository;

    @Value("${aws.s3.buckets.tournaments}")
    private String tournamentsBucket;


    public ResponseEntity<ApiResponse<?>> createTournament(TournamentRequestDTO dto, MultipartFile banner) {

        Tournaments tournament = modelMapper.map(dto, Tournaments.class);


        // Upload first, set URL on entity before saving
        if (banner != null && !banner.isEmpty()) {
            String bannerUrl = s3Service.uploadFile(banner, tournamentsBucket, "banners");

            Tournaments saved = tournamentRepository.save(tournament);
            saveTournamentMedia(saved, bannerUrl, "banner"); // ← also log in media table
            return ResponseEntity.ok(ApiResponse.success(200, "Tournament created successfully", modelMapper.map(saved, TournamentResponseDTO.class)));
        }


        Tournaments createdTournament = tournamentRepository.save(tournament);


        return ResponseEntity.ok(ApiResponse.success(200, "Team created successfully", modelMapper.map(createdTournament, TournamentResponseDTO.class)));

    }

    public List<TournamentResponseDTO> getAllTournaments() {
        return tournamentRepository.findAll().stream().map(tournament -> {
            TournamentResponseDTO response = modelMapper.map(tournament, TournamentResponseDTO.class);
// Fetch banner URL for each tournament
            mediaRepository.findByTournamentIdAndLabel(tournament.getId(), "banner").ifPresent(media -> response.setBannerUrl(media.getUrl()));

            return response;

        }).collect(Collectors.toList());
    }

    //
    public TournamentResponseDTO getTournamentById(Long tournamentId) {

        Tournaments tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found with id: " + tournamentId));
        TournamentResponseDTO response = modelMapper.map(tournament, TournamentResponseDTO.class);

        // fetch banner
        mediaRepository.findByTournamentIdAndLabel(tournamentId, "banner")
                .ifPresent(media -> response.setBannerUrl(media.getUrl()));

        return response;
    }


    public TournamentResponseDTO updateTournament(Long tournamentId, TournamentRequestDTO dto, MultipartFile banner) {
        Tournaments existingTournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found with id: " + tournamentId));

        modelMapper.map(dto, existingTournament);
        Tournaments updatedTournament = tournamentRepository.save(existingTournament);

        // update banner if present
        if (banner != null && !banner.isEmpty()) {

            String bannerUrl = s3Service.uploadFile(banner, tournamentsBucket, "banners");

            Media media = mediaRepository.findByTournamentIdAndLabel(tournamentId, "banner")
                    .orElse(new Media());

            media.setTournament(updatedTournament);
            media.setLabel("banner");
            media.setFileType("IMAGE");
            media.setUrl(bannerUrl);
            mediaRepository.save(media);
        }

        TournamentResponseDTO response = modelMapper.map(updatedTournament, TournamentResponseDTO.class);

        mediaRepository.findByTournamentIdAndLabel(tournamentId, "banner")
                .ifPresent(media -> response.setBannerUrl(media.getUrl()));

        return response;

    }

    public void deleteTournament(Long tournamentId) {

        Tournaments tournament = tournamentRepository.findById(tournamentId).orElseThrow(() -> new ResourceNotFoundException("Tournament not found with id: " + tournamentId));

        tournamentRepository.delete(tournament);

    }


    private void saveTournamentMedia(Tournaments tournament, String url, String label) {
        Media media = new Media();
        media.setUrl(url);
        media.setLabel(label);
        media.setFileType("IMAGE");
        media.setTournament(tournament);
        mediaRepository.save(media);
    }

}
