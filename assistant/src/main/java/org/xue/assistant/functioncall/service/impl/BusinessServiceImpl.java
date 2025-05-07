package org.xue.assistant.functioncall.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xue.assistant.functioncall.entity.Business;
import org.xue.assistant.functioncall.repository.BusinessRepository;
import org.xue.assistant.functioncall.service.BusinessService;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 业务管理服务实现类
 */
@Service
public class BusinessServiceImpl implements BusinessService {

    @Autowired
    private BusinessRepository businessRepository;

    @Override
    @Transactional
    public Business saveBusiness(Business business) {
        // 新增业务时设置创建时间
        if (business.getId() == null) {
            business.setCreateTime(LocalDateTime.now());
        }
        return businessRepository.save(business);
    }

    @Override
    public Optional<Business> findById(Long id) {
        return businessRepository.findById(id);
    }

    @Override
    public Page<Business> findBusinesses(String name, String type, String status, Pageable pageable) {
        return businessRepository.findByConditions(name, type, status, pageable);
    }

    @Override
    @Transactional
    public void deleteBusiness(Long id) {
        businessRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Business updateBusinessStatus(Long id, String status) {
        Optional<Business> businessOpt = businessRepository.findById(id);
        if (businessOpt.isPresent()) {
            Business business = businessOpt.get();
            business.setStatus(status);
            return businessRepository.save(business);
        }
        throw new RuntimeException("业务不存在，ID: " + id);
    }
}