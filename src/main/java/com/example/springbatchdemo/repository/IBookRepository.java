package com.example.springbatchdemo.repository;

import com.example.springbatchdemo.entity.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IBookRepository extends JpaRepository<BookEntity, Long> {
}
