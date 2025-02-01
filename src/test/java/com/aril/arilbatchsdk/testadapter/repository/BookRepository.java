package com.aril.arilbatchsdk.testadapter.repository;

import com.aril.arilbatchsdk.testadapter.entity.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<BookEntity, Long> {
}
