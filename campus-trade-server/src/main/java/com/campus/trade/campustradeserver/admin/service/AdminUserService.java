package com.campus.trade.campustradeserver.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.trade.campustradeserver.admin.dto.AdminUserQuery;
import com.campus.trade.campustradeserver.admin.vo.AdminUserResponse;
import com.campus.trade.campustradeserver.common.api.PageResponse;
import com.campus.trade.campustradeserver.user.entity.SysUser;
import com.campus.trade.campustradeserver.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final SysUserMapper sysUserMapper;

    public PageResponse<AdminUserResponse> listUsers(AdminUserQuery query){
        String username = normalize(query.getUsername());
        String email = normalize(query.getEmail());
        LambdaQueryWrapper<SysUser> wrapper= new LambdaQueryWrapper<>();
        if(username != null){
            wrapper.like(SysUser::getUsername,username);
        }
        if(email != null){
            wrapper.like(SysUser::getEmail,email);
        }
        if(query.getRole() != null){
            wrapper.eq(SysUser::getRole,query.getRole());
        }
        if(query.getStatus() != null){
            wrapper.eq(SysUser::getStatus,query.getStatus());
        }
        wrapper.orderByDesc(SysUser::getCreatedAt).orderByDesc(SysUser::getId);
        Page<SysUser> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<SysUser> result = sysUserMapper.selectPage(page,wrapper);
        PageResponse<AdminUserResponse>  response = new PageResponse<>();
        response.setPage(Math.toIntExact(result.getCurrent()));
        response.setPageSize(Math.toIntExact(result.getSize()));
        response.setTotal(result.getTotal());
        response.setRecords(result.getRecords().stream().map(this::toResponse).toList());
        return response;
    }
    private AdminUserResponse toResponse(SysUser user) {
        AdminUserResponse response = new AdminUserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }

    private String normalize(String value){
        if (value == null){
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

}
