package com.cnpm.bottomcv.service.impl;

import com.cnpm.bottomcv.ai.TFIDFVectorizer;
import com.cnpm.bottomcv.config.RabbitMQConfig;
import com.cnpm.bottomcv.dto.request.JobRequest;
import com.cnpm.bottomcv.dto.request.JobSearchRequest;
import com.cnpm.bottomcv.dto.response.CategoryResponse;
import com.cnpm.bottomcv.dto.response.CompanyResponse;
import com.cnpm.bottomcv.dto.response.JobResponse;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.exception.ResourceNotFoundException;
import com.cnpm.bottomcv.model.*;
import com.cnpm.bottomcv.model.ai.Recommendation;
import com.cnpm.bottomcv.repository.*;
import com.cnpm.bottomcv.repository.ai.RecommendationRepository;
import com.cnpm.bottomcv.service.JobService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.shade.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class JobServiceImpl implements JobService {

  private final JobRepository jobRepository;
  private final CompanyRepository companyRepository;
  private final CategoryRepository categoryRepository;
  private final CVRepository cvRepository;
  private final UserRepository userRepository;
  private final SavedJobRepository savedJobRepository;
  private final TFIDFVectorizer tfidfVectorizer;
  private MultiLayerNetwork recommendationModel;
  private final RabbitTemplate rabbitTemplate;
  private final RecommendationRepository recommendationRepository;
  private boolean modelAvailable = false;

  @EventListener(ApplicationReadyEvent.class)
  public void init() {
    // Try to load the trained model
    try {
      File modelFile = new File("recommendation-model.zip");
      if (modelFile.exists()) {
        recommendationModel = ModelSerializer.restoreMultiLayerNetwork(modelFile);
        modelAvailable = true;
        log.info("Recommendation model loaded successfully");
      } else {
        log.warn("Recommendation model not found. Recommendation features will be disabled.");
        modelAvailable = false;
      }
    } catch (Exception e) {
      log.error("Failed to load recommendation model: {}", e.getMessage());
      modelAvailable = false;
    }

    // Build TF-IDF index on startup if possible
    try {
      List<User> users = userRepository.findAll();
      List<Job> jobs = jobRepository.findAll();
      tfidfVectorizer.buildIndex(users, jobs);
      log.info("TF-IDF index built successfully");
    } catch (Exception e) {
      log.error("Failed to build TF-IDF index: {}", e.getMessage());
    }
  }

  @Override
  public void requestRecommendation(Long userId) {
    if (!modelAvailable) {
      log.warn("Recommendation model not available. Cannot process recommendation request for user {}", userId);
      return;
    }
    rabbitTemplate.convertAndSend(RabbitMQConfig.RECOMMENDATION_QUEUE, userId);
  }

  public void processRecommendation(Long userId) throws Exception {
    if (!modelAvailable) {
      log.warn("Recommendation model not available. Cannot process recommendation for user {}", userId);
      return;
    }

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User id", "id", userId.toString()));

    double[] userFeatures = extractUserFeatures(user);
    INDArray userInput = Nd4j.create(userFeatures);

    // Dự đoán điểm số cho từng công việc
    List<Job> allJobs = jobRepository.findAll();
    List<JobScore> jobScores = allJobs.stream().map(job -> {
      try {
        double[] jobFeatures = extractJobFeatures(job);
        INDArray jobInput = Nd4j.create(jobFeatures);
        INDArray combinedInput = Nd4j.hstack(userInput, jobInput);
        INDArray output = recommendationModel.output(combinedInput);
        return new JobScore(job, output.getDouble(0));
      } catch (Exception e) {
        throw new RuntimeException("Error extracting job features", e);
      }
    }).sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
        .toList();

    // Lấy top 10 công việc gợi ý
    List<Long> recommendedJobIds = jobScores.stream()
        .limit(10)
        .map(jobScore -> jobScore.getJob().getId())
        .collect(Collectors.toList());

    // Lưu kết quả gợi ý vào cơ sở dữ liệu
    Recommendation recommendation = recommendationRepository.findByUserId(userId);
    if (recommendation == null) {
      recommendation = new Recommendation();
      recommendation.setUserId(userId);
    }
    recommendation.setJobIds(new ObjectMapper().writeValueAsString(recommendedJobIds));
    recommendation.setCreatedAt(LocalDateTime.now());
    recommendationRepository.save(recommendation);
  }

  @Override
  public JobResponse createJob(JobRequest request) {
    Job job = new Job();
    mapRequestToEntity(job, request);
    job.setCreatedAt(LocalDateTime.now());
    job.setCreatedBy("system");
    jobRepository.save(job);
    return mapToResponse(job);
  }

  @Override
  public JobResponse getJobById(Long id) {
    Job job = jobRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Job id", "id", id.toString()));
    return mapToResponse(job);
  }

  @Override
  public ListResponse<JobResponse> getAllJobs(JobSearchRequest jobSearchRequest) {
    Specification<Job> spec = (root, query, cb) -> {
      var predicates = new java.util.ArrayList<Predicate>();

      if (jobSearchRequest.getKeyword() != null && !jobSearchRequest.getKeyword().isEmpty()) {
        String keyword = "%" + jobSearchRequest.getKeyword().toLowerCase() + "%";
        predicates.add(cb.or(
            cb.like(cb.lower(root.get("title")), keyword),
            cb.like(cb.lower(root.get("jobDescription")), keyword)));
      }

      if (jobSearchRequest.getLocation() != null && !jobSearchRequest.getLocation().isEmpty()) {
        predicates.add(cb.equal(root.get("location"), jobSearchRequest.getLocation()));
      }

      if (jobSearchRequest.getJobType() != null) {
        predicates.add(cb.equal(root.get("jobType"), jobSearchRequest.getJobType()));
      }

      if (jobSearchRequest.getMinSalary() != null) {
        predicates.add(cb.greaterThanOrEqualTo(root.get("salary"), jobSearchRequest.getMinSalary()));
      }

      if (jobSearchRequest.getMaxSalary() != null) {
        predicates.add(cb.lessThanOrEqualTo(root.get("salary"), jobSearchRequest.getMaxSalary()));
      }

      if (jobSearchRequest.getCategoryId() != null) {
        predicates.add(cb.isMember(
            categoryRepository.findById(jobSearchRequest.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category id", "id",
                    jobSearchRequest.getCategoryId().toString())),
            root.get("categories")));
      }

      if (jobSearchRequest.getStatus() != null) {
        predicates.add(cb.equal(root.get("status"), jobSearchRequest.getStatus()));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };

    Sort sort = Sort.unsorted();
    if (jobSearchRequest.getSortBy() != null && jobSearchRequest.getSortDirection() != null) {
      sort = Sort.by(Sort.Direction.fromString(jobSearchRequest.getSortDirection()), jobSearchRequest.getSortBy());
    }

    Pageable pageable = PageRequest.of(jobSearchRequest.getPage(), jobSearchRequest.getSize(), sort);

    Page<Job> pageJob = jobRepository.findAll(spec, pageable);
    List<Job> jobs = pageJob.getContent();

    return ListResponse.<JobResponse>builder()
        .data(jobs.stream().map(this::mapToResponse).collect(Collectors.toList()))
        .pageNo(pageJob.getNumber())
        .pageSize(pageJob.getSize())
        .totalElements((int) pageJob.getTotalElements())
        .totalPages(pageJob.getTotalPages())
        .isLast(pageJob.isLast())
        .build();
  }

  @Override
  public JobResponse updateJob(Long id, JobRequest request) {
    Job job = jobRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Job id", "id", id.toString()));

    mapRequestToEntity(job, request);
    job.setUpdatedAt(LocalDateTime.now());
    job.setUpdatedBy("system");
    jobRepository.save(job);
    return mapToResponse(job);
  }

  @Override
  @Transactional
  public void deleteJob(Long id) {
    Job job = jobRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Job id", "id", id.toString()));
    // Delete related saved_jobs first to avoid foreign key constraint
    savedJobRepository.deleteByJobId(id);
    jobRepository.delete(job);
  }

  @Override
  public ListResponse<JobResponse> getRecommendedJobs(Long userId, int pageNo, int pageSize) throws IOException {
    if (!modelAvailable) {
      log.warn("Recommendation model not available. Returning regular jobs instead.");
      // Fall back to returning regular jobs when recommendation model isn't available
      JobSearchRequest defaultSearchRequest = new JobSearchRequest();
      defaultSearchRequest.setPage(pageNo);
      defaultSearchRequest.setSize(pageSize);
      return getAllJobs(defaultSearchRequest);
    }

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User id", "id", userId.toString()));

    double[] userFeatures = extractUserFeatures(user);
    INDArray userInput = Nd4j.create(userFeatures);

    // Dự đoán điểm số cho từng công việc
    List<Job> allJobs = jobRepository.findAll();
    List<JobScore> jobScores = allJobs.stream().map(job -> {
      try {
        double[] jobFeatures = extractJobFeatures(job);
        INDArray jobInput = Nd4j.create(jobFeatures);
        INDArray combinedInput = Nd4j.hstack(userInput, jobInput);
        INDArray output = recommendationModel.output(combinedInput);
        return new JobScore(job, output.getDouble(0));
      } catch (Exception e) {
        throw new RuntimeException("Error extracting job features", e);
      }
    }).sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
        .toList();

    // Phân trang
    Pageable pageable = PageRequest.of(pageNo, pageSize);
    int start = (int) pageable.getOffset();
    int end = Math.min((start + pageable.getPageSize()), jobScores.size());
    List<Job> recommendedJobs = jobScores.subList(start, end).stream()
        .map(JobScore::getJob)
        .collect(Collectors.toList());

    Page<Job> jobPage = new PageImpl<>(recommendedJobs, pageable, jobScores.size());
    List<JobResponse> jobResponses = jobPage.getContent().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());

    return ListResponse.<JobResponse>builder()
        .data(jobResponses)
        .pageNo(jobPage.getNumber())
        .pageSize(jobPage.getSize())
        .totalElements((int) jobPage.getTotalElements())
        .totalPages(jobPage.getTotalPages())
        .isLast(jobPage.isLast())
        .build();
  }

  private double[] extractCVFeatures(CV cv) throws IOException {
    String cvText = (cv.getSkills() != null ? cv.getSkills() : "") + " "
        + (cv.getExperience() != null ? cv.getExperience() : "");
    return tfidfVectorizer.vectorize(cvText);
  }

  private double[] extractUserFeatures(User user) throws IOException {
    // Lấy CV của người dùng
    List<CV> cvs = cvRepository.findByUserId(user.getId());
    if (cvs.isEmpty()) {
      throw new ResourceNotFoundException("CV id", "userId", user.getId().toString());
    }

    // Chọn CV đầu tiên (có thể thay đổi logic để chọn CV mới nhất hoặc CV chính)
    CV cv = cvs.get(0);
    return extractCVFeatures(cv);
  }

  private double[] extractJobFeatures(Job job) throws IOException {
    String jobText = job.getJobDescription() + " " + job.getJobRequirement();
    return tfidfVectorizer.vectorize(jobText);
  }

  private void mapRequestToEntity(Job job, JobRequest request) {
    job.setTitle(request.getTitle());
    job.setJobDescription(request.getJobDescription());
    job.setJobRequirement(request.getJobRequirement());
    job.setJobBenefit(request.getJobBenefit());
    job.setJobType(request.getJobType());
    job.setLocation(request.getLocation());
    job.setWorkTime(request.getWorkTime());
    job.setSalary(request.getSalary());
    job.setCareerLevel(request.getCareerLevel());
    job.setQualification(request.getQualification());
    job.setExperience(request.getExperience());
    job.setExpiryDate(request.getExpiryDate());
    job.setStatus(request.getStatus());

    Company company = companyRepository.findById(request.getCompanyId())
        .orElseThrow(() -> new ResourceNotFoundException("Company id", "id", request.getCompanyId().toString()));
    job.setCompany(company);

    if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
      Set<Category> categories = new HashSet<>();
      for (Long categoryId : request.getCategoryIds()) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException("Category id", "id", categoryId.toString()));
        categories.add(category);
      }
      job.setCategories(categories);
    } else {
      job.setCategories(new HashSet<>());
    }
  }

  private JobResponse mapToResponse(Job job) {
    JobResponse response = new JobResponse();
    response.setId(job.getId());
    response.setTitle(job.getTitle());
    response.setJobDescription(job.getJobDescription());
    response.setJobRequirement(job.getJobRequirement());
    response.setJobBenefit(job.getJobBenefit());
    response.setJobType(job.getJobType());
    response.setLocation(job.getLocation());
    response.setWorkTime(job.getWorkTime());
    response.setSalary(job.getSalary());
    response.setCareerLevel(job.getCareerLevel());
    response.setQualification(job.getQualification());
    response.setExperience(job.getExperience());
    response.setExpiryDate(job.getExpiryDate());
    response.setStatus(job.getStatus());
    response.setCreatedAt(job.getCreatedAt());
    response.setCreatedBy(job.getCreatedBy());
    response.setUpdatedAt(job.getUpdatedAt());
    response.setUpdatedBy(job.getUpdatedBy());

    response.setCompany(mapToCompanyResponse(job.getCompany()));
    response.setCategories(job.getCategories().stream()
        .map(this::mapToCategoryResponse)
        .collect(Collectors.toSet()));

    return response;
  }

  private CompanyResponse mapToCompanyResponse(Company company) {
    CompanyResponse companyResponse = new CompanyResponse();
    companyResponse.setId(company.getId());
    companyResponse.setName(company.getName());
    companyResponse.setSlug(company.getSlug());
    companyResponse.setIntroduce(company.getIntroduce());
    companyResponse.setSocialMediaLinks(company.getSocialMediaLinks());
    companyResponse.setAddresses(company.getAddresses());
    companyResponse.setPhone(company.getPhone());
    companyResponse.setEmail(company.getEmail());
    companyResponse.setWebsite(company.getWebsite());
    companyResponse.setLogo(company.getLogo());
    companyResponse.setCover(company.getCover());
    companyResponse.setIndustry(company.getIndustry());
    companyResponse.setCompanySize(company.getCompanySize());
    companyResponse.setFoundedYear(company.getFoundedYear());
    return companyResponse;
  }

  private CategoryResponse mapToCategoryResponse(Category category) {
    CategoryResponse categoryResponse = new CategoryResponse();
    categoryResponse.setId(category.getId());
    categoryResponse.setName(category.getName());
    categoryResponse.setSlug(category.getSlug());
    categoryResponse.setDescription(category.getDescription());
    return categoryResponse;
  }
}

class JobScore {
  private Job job;
  private double score;

  public JobScore(Job job, double score) {
    this.job = job;
    this.score = score;
  }

  public Job getJob() {
    return job;
  }

  public double getScore() {
    return score;
  }
}
