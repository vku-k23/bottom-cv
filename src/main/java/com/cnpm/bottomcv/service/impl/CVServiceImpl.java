package com.cnpm.bottomcv.service.impl;

import com.cnpm.bottomcv.dto.request.CVRequest;
import com.cnpm.bottomcv.dto.response.CVResponse;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.model.CV;
import com.cnpm.bottomcv.model.User;
import com.cnpm.bottomcv.repository.CVRepository;
import com.cnpm.bottomcv.repository.UserRepository;
import com.cnpm.bottomcv.service.CVService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CVServiceImpl implements CVService {

    private final CVRepository cvRepository;
    private final UserRepository userRepository;

    private final String uploadDir = "uploads/cvs/";

    @Override
    public CVResponse createCV(CVRequest request) {
        String filePath = saveFile(request.getCvFile());

        CV cv = new CV();
        mapRequestToEntity(cv, request, filePath);
        cv.setCreatedAt(LocalDateTime.now());
        cv.setCreatedBy("system");
        cvRepository.save(cv);
        return mapToResponse(cv);
    }

    @Override
    public CVResponse getCVById(Long id) {
        CV cv = cvRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CV not found"));
        return mapToResponse(cv);
    }

    @Override
    public ListResponse<CVResponse> getAllCVs(int pageNo, int pageSize, String sortBy, String sortType) {
        Sort sortObj = sortBy.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNo, pageSize, sortObj);
        Page<CV> pageCompany = cvRepository.findAll(pageable);
        List<CV> companyContent = pageCompany.getContent();

        return ListResponse.<CVResponse>builder()
                .data(mapToResponseList(companyContent))
                .pageNo(pageCompany.getNumber())
                .pageSize(pageCompany.getSize())
                .totalElements((int) pageCompany.getTotalElements())
                .totalPages(pageCompany.getTotalPages())
                .isLast(pageCompany.isLast())
                .build();
    }

    private List<CVResponse> mapToResponseList(List<CV> companyContent) {
        return companyContent.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public CVResponse updateCV(Long id, CVRequest request) {
        CV cv = cvRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CV not found"));

        if (request.getCvFile() != null && !request.getCvFile().isEmpty()) {
            deleteFile(cv.getCvFile());
            String newFilePath = saveFile(request.getCvFile());
            cv.setCvFile(newFilePath);
        }

        cv.setTitle(request.getTitle());
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        cv.setUser(user);

        cv.setUpdatedAt(LocalDateTime.now());
        cv.setUpdatedBy("system");
        cvRepository.save(cv);
        return mapToResponse(cv);
    }

    @Override
    public void deleteCV(Long id) {
        CV cv = cvRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CV not found"));
        deleteFile(cv.getCvFile());
        cvRepository.delete(cv);
    }

    private void mapRequestToEntity(CV cv, CVRequest request, String filePath) {
        cv.setTitle(request.getTitle());
        cv.setCvFile(filePath);

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        cv.setUser(user);
    }

    private CVResponse mapToResponse(CV cv) {
        CVResponse response = new CVResponse();
        response.setId(cv.getId());
        response.setTitle(cv.getTitle());
        response.setCvFile(cv.getCvFile());
        response.setUserId(cv.getUser().getId());
        response.setCreatedAt(cv.getCreatedAt());
        response.setCreatedBy(cv.getCreatedBy());
        response.setUpdatedAt(cv.getUpdatedAt());
        response.setUpdatedBy(cv.getUpdatedBy());
        return response;
    }

    private String saveFile(MultipartFile file) {
        try {
            Files.createDirectories(Paths.get(uploadDir));

            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, fileName);

            Files.write(filePath, file.getBytes());

            return filePath.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file: " + e.getMessage());
        }
    }

    private void deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Files.delete(path);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + e.getMessage());
        }
    }

    @Override
    public ListResponse<CVResponse> getAllCVsByUserId(Long userId, int pageNo, int pageSize, String sortBy, String sortType) {
        return null;
    }
}
