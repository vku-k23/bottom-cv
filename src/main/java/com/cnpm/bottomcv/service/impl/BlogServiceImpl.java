package com.cnpm.bottomcv.service.impl;

import com.cnpm.bottomcv.constant.BlogStatus;
import com.cnpm.bottomcv.dto.request.BlogRequest;
import com.cnpm.bottomcv.dto.request.BlogSearchRequest;
import com.cnpm.bottomcv.dto.response.BlogResponse;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.exception.ResourceNotFoundException;
import com.cnpm.bottomcv.model.Blog;
import com.cnpm.bottomcv.model.Category;
import com.cnpm.bottomcv.model.Profile;
import com.cnpm.bottomcv.model.User;
import com.cnpm.bottomcv.repository.BlogRepository;
import com.cnpm.bottomcv.repository.CategoryRepository;
import com.cnpm.bottomcv.service.BlogService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlogServiceImpl implements BlogService {

    private final BlogRepository blogRepository;
    private final CategoryRepository categoryRepository;

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    @Override
    @Transactional
    public BlogResponse createBlog(BlogRequest request, Authentication authentication) {
        log.info("Creating new blog: {}", request.getTitle());

        User author = (User) authentication.getPrincipal();

        // Generate or validate slug
        String slug = generateSlug(request.getTitle(), request.getSlug(), null);

        // Set status to DRAFT if not provided
        BlogStatus status = request.getStatus() != null ? request.getStatus() : BlogStatus.DRAFT;

        // Set publishedAt if status is PUBLISHED
        LocalDateTime publishedAt = request.getPublishedAt();
        if (status == BlogStatus.PUBLISHED && publishedAt == null) {
            publishedAt = LocalDateTime.now();
        }

        Blog blog = Blog.builder()
                .title(request.getTitle())
                .slug(slug)
                .thumbnail(request.getThumbnail())
                .content(request.getContent())
                .excerpt(request.getExcerpt())
                .status(status)
                .metaTitle(request.getMetaTitle())
                .metaDescription(request.getMetaDescription())
                .metaKeywords(request.getMetaKeywords())
                .publishedAt(publishedAt)
                .author(author)
                .viewCount(0L)
                .build();

        // Set category if provided
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Category", "id", request.getCategoryId().toString()));
            blog.setCategory(category);
        }

        Blog savedBlog = blogRepository.save(blog);
        log.info("Blog created with id: {}", savedBlog.getId());

        return mapToResponse(savedBlog);
    }

    @Override
    @Transactional
    public BlogResponse updateBlog(Long id, BlogRequest request, Authentication authentication) {
        log.info("Updating blog with id: {}", id);

        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog", "id", id.toString()));

        // Generate or validate slug
        String slug = generateSlug(request.getTitle(), request.getSlug(), id);

        blog.setTitle(request.getTitle());
        blog.setSlug(slug);
        blog.setThumbnail(request.getThumbnail());
        blog.setContent(request.getContent());
        blog.setExcerpt(request.getExcerpt());
        blog.setMetaTitle(request.getMetaTitle());
        blog.setMetaDescription(request.getMetaDescription());
        blog.setMetaKeywords(request.getMetaKeywords());

        // Handle status change
        if (request.getStatus() != null) {
            BlogStatus previousStatus = blog.getStatus();
            blog.setStatus(request.getStatus());

            // Set publishedAt when transitioning to PUBLISHED
            if (request.getStatus() == BlogStatus.PUBLISHED && previousStatus == BlogStatus.DRAFT) {
                blog.setPublishedAt(request.getPublishedAt() != null ? request.getPublishedAt() : LocalDateTime.now());
            } else if (request.getPublishedAt() != null) {
                blog.setPublishedAt(request.getPublishedAt());
            }
        }

        // Update category
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Category", "id", request.getCategoryId().toString()));
            blog.setCategory(category);
        } else {
            blog.setCategory(null);
        }

        Blog updatedBlog = blogRepository.save(blog);
        log.info("Blog updated successfully");

        return mapToResponse(updatedBlog);
    }

    @Override
    @Transactional
    public void deleteBlog(Long id) {
        log.info("Deleting blog with id: {}", id);

        if (!blogRepository.existsById(id)) {
            throw new ResourceNotFoundException("Blog", "id", id.toString());
        }

        blogRepository.deleteById(id);
        log.info("Blog deleted successfully");
    }

    @Override
    public BlogResponse getBlogById(Long id) {
        log.info("Getting blog by id: {}", id);

        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog", "id", id.toString()));

        return mapToResponse(blog);
    }

    @Override
    public ListResponse<BlogResponse> getAllBlogs(BlogSearchRequest request) {
        log.info("Getting all blogs with filters");

        Pageable pageable = createPageable(request);
        Specification<Blog> spec = createSpecification(request, false);

        Page<Blog> blogPage = blogRepository.findAll(spec, pageable);

        List<BlogResponse> blogResponses = blogPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ListResponse.<BlogResponse>builder()
                .data(blogResponses)
                .pageNo(blogPage.getNumber())
                .pageSize(blogPage.getSize())
                .totalElements((int) blogPage.getTotalElements())
                .totalPages(blogPage.getTotalPages())
                .isLast(blogPage.isLast())
                .build();
    }

    @Override
    @Transactional
    public BlogResponse getPublishedBlogBySlug(String slug) {
        log.info("Getting published blog by slug: {}", slug);

        Blog blog = blogRepository.findBySlugAndStatus(slug, BlogStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Blog", "slug", slug));

        // Increment view count
        blogRepository.incrementViewCount(blog.getId());
        blog.setViewCount(blog.getViewCount() + 1);

        return mapToResponse(blog);
    }

    @Override
    public ListResponse<BlogResponse> getPublishedBlogs(BlogSearchRequest request) {
        log.info("Getting published blogs");

        // Force status to PUBLISHED for public API
        request.setStatus(BlogStatus.PUBLISHED);

        Pageable pageable = createPageable(request);
        Specification<Blog> spec = createSpecification(request, true);

        Page<Blog> blogPage = blogRepository.findAll(spec, pageable);

        List<BlogResponse> blogResponses = blogPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ListResponse.<BlogResponse>builder()
                .data(blogResponses)
                .pageNo(blogPage.getNumber())
                .pageSize(blogPage.getSize())
                .totalElements((int) blogPage.getTotalElements())
                .totalPages(blogPage.getTotalPages())
                .isLast(blogPage.isLast())
                .build();
    }

    @Override
    public List<BlogResponse> getRecentBlogs() {
        log.info("Getting recent blogs");

        List<Blog> recentBlogs = blogRepository.findTop5ByStatusOrderByPublishedAtDesc(BlogStatus.PUBLISHED);

        return recentBlogs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BlogResponse> getRelatedBlogs(Long blogId) {
        log.info("Getting related blogs for blog id: {}", blogId);

        List<Blog> relatedBlogs = blogRepository.findTop3ByStatusAndIdNotOrderByPublishedAtDesc(BlogStatus.PUBLISHED,
                blogId);

        return relatedBlogs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Long countPublishedBlogs() {
        return blogRepository.countByStatus(BlogStatus.PUBLISHED);
    }

    @Override
    public Long countDraftBlogs() {
        return blogRepository.countByStatus(BlogStatus.DRAFT);
    }

    // Helper methods
    private String generateSlug(String title, String customSlug, Long existingId) {
        String slug = StringUtils.hasText(customSlug) ? customSlug : toSlug(title);

        // Check uniqueness
        boolean slugExists = existingId == null
                ? blogRepository.existsBySlug(slug)
                : blogRepository.existsBySlugAndIdNot(slug, existingId);

        if (slugExists) {
            // Append timestamp to make unique
            slug = slug + "-" + System.currentTimeMillis();
        }

        return slug;
    }

    private String toSlug(String input) {
        if (input == null)
            return "";

        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String slug = WHITESPACE.matcher(normalized).replaceAll("-");
        slug = NON_LATIN.matcher(slug).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH).replaceAll("-+", "-").replaceAll("^-|-$", "");
    }

    private Pageable createPageable(BlogSearchRequest request) {
        String sortBy = StringUtils.hasText(request.getSortBy()) ? request.getSortBy() : "createdAt";
        String sortDirection = StringUtils.hasText(request.getSortDirection()) ? request.getSortDirection() : "desc";

        Sort sort = sortDirection.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        return PageRequest.of(request.getPage(), request.getSize(), sort);
    }

    private Specification<Blog> createSpecification(BlogSearchRequest request, boolean publishedOnly) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Status filter
            if (publishedOnly || request.getStatus() != null) {
                BlogStatus status = publishedOnly ? BlogStatus.PUBLISHED : request.getStatus();
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            // Keyword search
            if (StringUtils.hasText(request.getKeyword())) {
                String keyword = "%" + request.getKeyword().toLowerCase() + "%";
                Predicate titlePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), keyword);
                Predicate contentPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("content")), keyword);
                Predicate excerptPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("excerpt")), keyword);
                predicates.add(criteriaBuilder.or(titlePredicate, contentPredicate, excerptPredicate));
            }

            // Category filter
            if (request.getCategoryId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), request.getCategoryId()));
            }

            // Author filter
            if (request.getAuthorId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("author").get("id"), request.getAuthorId()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private BlogResponse mapToResponse(Blog blog) {
        BlogResponse.AuthorInfo authorInfo = null;
        if (blog.getAuthor() != null) {
            String fullName = null;
            String avatar = null;
            if (blog.getAuthor().getProfile() != null) {
                Profile profile = blog.getAuthor().getProfile();
                fullName = (profile.getFirstName() != null ? profile.getFirstName() : "")
                        + " "
                        + (profile.getLastName() != null ? profile.getLastName() : "");
                fullName = fullName.trim();
                if (fullName.isEmpty()) {
                    fullName = null;
                }
                avatar = profile.getAvatar();
            }
            authorInfo = BlogResponse.AuthorInfo.builder()
                    .id(blog.getAuthor().getId())
                    .username(blog.getAuthor().getUsername())
                    .fullName(fullName)
                    .avatar(avatar)
                    .build();
        }

        BlogResponse.CategoryInfo categoryInfo = null;
        if (blog.getCategory() != null) {
            categoryInfo = BlogResponse.CategoryInfo.builder()
                    .id(blog.getCategory().getId())
                    .name(blog.getCategory().getName())
                    .build();
        }

        return BlogResponse.builder()
                .id(blog.getId())
                .title(blog.getTitle())
                .slug(blog.getSlug())
                .thumbnail(blog.getThumbnail())
                .content(blog.getContent())
                .excerpt(blog.getExcerpt())
                .status(blog.getStatus())
                .metaTitle(blog.getMetaTitle())
                .metaDescription(blog.getMetaDescription())
                .metaKeywords(blog.getMetaKeywords())
                .publishedAt(blog.getPublishedAt())
                .viewCount(blog.getViewCount())
                .author(authorInfo)
                .category(categoryInfo)
                .createdAt(blog.getCreatedAt())
                .updatedAt(blog.getUpdatedAt())
                .build();
    }
}
