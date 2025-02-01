package com.aril.arilbatchsdk.testadapter.entity;

import com.aril.valhala.batch.BatchItem;
import com.aril.valhala.batch.IdempotentBatchItem;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "book_table")
public class BookEntity implements BatchItem, IdempotentBatchItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "book_title")
    private String title;

    @Column(name = "book_author")
    private String author;

    @Column(name = "book_year")
    private int year;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private BookDetailEntity bookDetail;

    @Override
    public String getIdempotentKey() {
        if (id != null) {
            return id.toString();
        }
        return title;
    }
}
