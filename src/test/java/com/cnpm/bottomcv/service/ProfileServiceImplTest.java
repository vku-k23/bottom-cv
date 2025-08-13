package com.cnpm.bottomcv.service;

import com.cnpm.bottomcv.model.Profile;
import com.cnpm.bottomcv.repository.ProfileRepository;
import com.cnpm.bottomcv.service.impl.ProfileServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private ProfileServiceImpl profileService;

    @Test
    void allProfiles_usesSortTypeParameter() {
        Page<Profile> page = new PageImpl<>(Collections.emptyList());
        when(profileRepository.findAll(any(Pageable.class))).thenReturn(page);

        profileService.allProfiles(0, 10, "id", "ASC");

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(profileRepository).findAll(captor.capture());
        Pageable pageable = captor.getValue();

        assertEquals(Sort.by("id").ascending(), pageable.getSort());
    }
}
