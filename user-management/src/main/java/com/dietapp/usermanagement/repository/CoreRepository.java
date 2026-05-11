package com.dietapp.usermanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface CoreRepository<T, ID> extends JpaRepository<T, ID> {
}
