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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;
import java.util.Map;
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

    public Map<String, Object> getAllTournaments(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

//        Page<Tournaments> tournaments = tournamentRepository.findAll(pageable);

//        Page<Tournaments> tournaments = tournamentRepository.findUpcomingTournaments(pageable);


//        Page<Tournaments> tournaments = tournamentRepository.findByStartDateGreaterThanEqual(
//                LocalDateTime.now(),
//                pageable
//        );



        Page<Tournaments> tournaments = tournamentRepository.findAllOrdered(pageable);


        List<TournamentResponseDTO> content = tournaments.getContent().stream().map(tournament -> {
                    TournamentResponseDTO response = modelMapper.map(tournament, TournamentResponseDTO.class);
                    // Fetch banner URL for each tournament
                    mediaRepository.findByTournamentIdAndLabel(tournament.getId(), "banner")
                            .ifPresent(media -> response.setBannerUrl(media.getUrl()));
                    return response;
                }) .collect(Collectors.toList());

        return buildPageResponse(content, tournaments);
    }

    /**
     * Filter tournaments by month (1–12) and/or year.
     * Uses a date-range query (portable across all databases).
     * Either parameter is optional; when both are absent, returns all tournaments.
     */
    public java.util.Map<String, Object> getTournamentsByFilter(Integer month, Integer year, Pageable pageable) {
        Page<Tournaments> tournaments;

        if (month != null || year != null) {
            // Resolve the year to filter on (default to current year when only month is given)
            int resolvedYear = (year != null) ? year : java.time.LocalDateTime.now().getYear();

            if (month != null) {
                // Exact month + year: from 1st of month to 1st of next month
                java.time.LocalDateTime from = java.time.LocalDateTime.of(resolvedYear, month, 1, 0, 0);
                java.time.LocalDateTime to = from.plusMonths(1);
                tournaments = tournamentRepository.findByDateRange(from, to,pageable);
            } else {
                // Year only: from Jan 1 to Jan 1 of next year
                java.time.LocalDateTime from = java.time.LocalDateTime.of(resolvedYear, 1, 1, 0, 0);
                java.time.LocalDateTime to = java.time.LocalDateTime.of(resolvedYear + 1, 1, 1, 0, 0);
                tournaments = tournamentRepository.findByDateRange(from, to,pageable);
            }
        } else {
            tournaments = tournamentRepository.findAll(pageable);
        }

        List<TournamentResponseDTO> content = tournaments.getContent().stream().map(tournament -> {
                    TournamentResponseDTO response = modelMapper.map(tournament, TournamentResponseDTO.class);
                    mediaRepository.findByTournamentIdAndLabel(tournament.getId(), "banner")
                            .ifPresent(media -> response.setBannerUrl(media.getUrl()));
                    return response;
                }) .collect(Collectors.toList());

        return buildPageResponse(content, tournaments);
    }

    /** Builds a consistent paginated response envelope shared by all list endpoints. */
    private java.util.Map<String, Object> buildPageResponse(List<TournamentResponseDTO> content, Page<Tournaments> page) {
        java.util.Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("content", content);
        response.put("page", page.getNumber());
        response.put("size", page.getSize());
        response.put("totalElements", page.getTotalElements());
        response.put("totalPages", page.getTotalPages());
        response.put("hasNext", !page.isLast());
        response.put("last", page.isLast());
        return response;
    }

    /**
     * Returns distinct years from all tournament startDates, sorted ascending.
     * The frontend uses this to build a dynamic year dropdown.
     */
    public List<Integer> getAvailableYears() {
        List<Integer> years = tournamentRepository.findDistinctYears();
        System.out.println("[TournamentService] Available years: " + years);
        return years;
    }

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

    /** Manually open or close registrations for a tournament. */
    public TournamentResponseDTO setRegistrationClosed(Long tournamentId, boolean closed) {
        Tournaments tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found with id: " + tournamentId));
        tournament.setRegistrationClosed(closed);
        Tournaments saved = tournamentRepository.save(tournament);
        TournamentResponseDTO response = modelMapper.map(saved, TournamentResponseDTO.class);
        mediaRepository.findByTournamentIdAndLabel(tournamentId, "banner")
                .ifPresent(media -> response.setBannerUrl(media.getUrl()));
        return response;
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
