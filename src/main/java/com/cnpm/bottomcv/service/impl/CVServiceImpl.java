package com.cnpm.bottomcv.service.impl;

import com.cnpm.bottomcv.dto.request.CVRequest;
import com.cnpm.bottomcv.dto.response.CVResponse;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.exception.ResourceNotFoundException;
import com.cnpm.bottomcv.model.CV;
import com.cnpm.bottomcv.model.User;
import com.cnpm.bottomcv.repository.CVRepository;
import com.cnpm.bottomcv.repository.UserRepository;
import com.cnpm.bottomcv.service.CVService;
import com.cnpm.bottomcv.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final FileStorageService fileStorageService;

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
                .orElseThrow(() -> new ResourceNotFoundException("CV id", "cvId", id.toString()));
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

    @Override
    public ListResponse<CVResponse> getAllMyCVs(String username, int pageNo, int pageSize, String sortBy, String sortType) {
        Sort sortObj = sortBy.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNo, pageSize, sortObj);
        Page<CV> pageCompany = cvRepository.findAllByUserUsername(username, pageable);
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
                .orElseThrow(() -> new ResourceNotFoundException("CV id", "cvId", id.toString()));

        if (request.getCvFile() != null && !request.getCvFile().isEmpty()) {
            deleteFile(cv.getCvFile());
            String newFilePath = saveFile(request.getCvFile());
            cv.setCvFile(newFilePath);
        }

        cv.setTitle(request.getTitle());
        cv.setSkills(request.getSkills());
        cv.setContent(request.getContent());
        cv.setExperience(request.getExperience());
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User id", "userId", request.getUserId().toString()));
        cv.setUser(user);

        cv.setUpdatedAt(LocalDateTime.now());
        cv.setUpdatedBy("system");
        cvRepository.save(cv);
        return mapToResponse(cv);
    }

    @Override
    public void deleteCV(Long id) {
        CV cv = cvRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CV id", "cvId", id.toString()));
        deleteFile(cv.getCvFile());
        cvRepository.delete(cv);
    }

    private void mapRequestToEntity(CV cv, CVRequest request, String filePath) {
        cv.setTitle(request.getTitle());
        cv.setCvFile(filePath);
        cv.setSkills(request.getSkills());
        cv.setContent(request.getContent());
        cv.setExperience(request.getExperience());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User id", "userId", request.getUserId().toString()));
        cv.setUser(user);
    }

    private CVResponse mapToResponse(CV cv) {
        CVResponse response = new CVResponse();
        response.setId(cv.getId());
        response.setTitle(cv.getTitle());
        response.setCvFile(cv.getCvFile());
        response.setUserId(cv.getUser().getId());
        response.setSkills(cv.getSkills());
        response.setContent(cv.getContent());
        response.setExperience(cv.getExperience());
        response.setCreatedAt(cv.getCreatedAt());
        response.setCreatedBy(cv.getCreatedBy());
        response.setUpdatedAt(cv.getUpdatedAt());
        response.setUpdatedBy(cv.getUpdatedBy());
        return response;
    }

    private String saveFile(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            String filePath = "cv/" + fileName;
            fileStorageService.uploadCV(file);
            return filePath;
        } catch (Exception e) {
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
