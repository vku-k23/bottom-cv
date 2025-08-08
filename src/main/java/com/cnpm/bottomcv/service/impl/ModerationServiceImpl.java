package com.cnpm.bottomcv.service.impl;

import com.cnpm.bottomcv.service.ModerationService;
import org.springframework.stereotype.Service;

@Service
public class ModerationServiceImpl implements ModerationService {
    @Override
    public void approveJob(Long jobId) {
        // TODO: implement job approval
    }

    @Override
    public void rejectJob(Long jobId, String reason) {
        // TODO: implement job rejection with reason
    }

    @Override
    public void approveReview(Long reviewId) {
        // TODO: implement review approval
    }

    @Override
    public void rejectReview(Long reviewId, String reason) {
        // TODO: implement review rejection with reason
    }
}