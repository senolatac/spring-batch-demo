package com.aril.arilbatchsdk.filter.specification;

import com.aril.arilbatchsdk.entity.BatchJobSummary;
import com.aril.arilbatchsdk.entity.BatchJobSummary_;
import com.aril.arilbatchsdk.filter.predicate.FilterBatchJobSummaryPredicates;
import com.aril.arilbatchsdk.filter.usecase.FilterBatchJobUseCase;
import com.aril.arilbatchsdk.util.SpecificationUtils;
import jakarta.persistence.criteria.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
public class BatchJobSummarySpecification implements Specification<BatchJobSummary> {
    private final transient FilterBatchJobUseCase filterBatchJobUseCase;

    @Override
    public Predicate toPredicate(@NonNull Root<BatchJobSummary> root,
                                 @NonNull CriteriaQuery<?> query,
                                 CriteriaBuilder criteriaBuilder) {
        Map<String, Join<?, ?>> joinMap = joinMap(root, query);

        List<Predicate> predicates = FilterBatchJobSummaryPredicates._VALUES.stream()
                .map(field -> field.toPredicate(filterBatchJobUseCase, root, criteriaBuilder, joinMap))
                .filter(Objects::nonNull)
                .toList();

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    private Map<String, Join<?, ?>> joinMap(Root<BatchJobSummary> root, CriteriaQuery<?> query) {
        return Map.of(
                BatchJobSummary_.CONSUMER_TRACKER, SpecificationUtils.getJoinOrFetch(root, query, BatchJobSummary_.CONSUMER_TRACKER, JoinType.LEFT),
                BatchJobSummary_.RESULT_SUMMARY, SpecificationUtils.getJoinOrFetch(root, query, BatchJobSummary_.RESULT_SUMMARY, JoinType.LEFT)
        );
    }
}
