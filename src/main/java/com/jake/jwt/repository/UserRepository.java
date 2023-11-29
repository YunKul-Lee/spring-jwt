package com.jake.jwt.repository;

import com.jake.jwt.common.repository.RefreshableCRUDRepository;
import com.jake.jwt.model.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends RefreshableCRUDRepository<UserInfo, Long> {

    public UserInfo findByUsername(String username);

    UserInfo findFirstById(Long id);
}
