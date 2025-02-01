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
@Table(name = "book_detail")
public class BookDetailEntity implements BatchItem, IdempotentBatchItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "detail")
    private String detail;

    @JoinColumn(name = "book_id", referencedColumnName = "id")
    @OneToOne(fetch = FetchType.LAZY)
    private BookEntity book;

    @Override
    public String getIdempotentKey() {
        if (id != null) {
            return id.toString();
        }
        return detail;
    }
}
