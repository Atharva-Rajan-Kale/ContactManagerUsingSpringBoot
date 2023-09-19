package com.example.springbootproject.dao;

import com.example.springbootproject.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface Repository extends JpaRepository<User, Integer> {

    public User findByEmail(String name);
}
