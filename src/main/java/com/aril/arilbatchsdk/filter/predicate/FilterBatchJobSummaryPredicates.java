package com.aril.arilbatchsdk.filter.predicate;

import com.aril.arilbatchsdk.entity.BatchJobSummary;
import com.aril.arilbatchsdk.entity.BatchJobSummary_;
import com.aril.arilbatchsdk.filter.specification.SpecificationFieldOperation;
import com.aril.arilbatchsdk.filter.usecase.FilterBatchJobUseCase;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@RequiredArgsConstructor
public enum FilterBatchJobSummaryPredicates {

    EQ_MODULE(BatchJobSummary_.MODULE, SpecificationFieldOperation.EQ, FilterBatchJobUseCase::getModule),
    LIKE_JOB_NAME(BatchJobSummary_.JOB_NAME, SpecificationFieldOperation.LIKE, FilterBatchJobUseCase::getJobName),
    LIKE_JOB_OWNER(BatchJobSummary_.JOB_OWNER, SpecificationFieldOperation.LIKE, FilterBatchJobUseCase::getJobOwner),
    GTE_START_TIME(BatchJobSummary_.START_TIME, SpecificationFieldOperation.GTE, FilterBatchJobUseCase::getStartTime),
    LTE_END_TIME(BatchJobSummary_.END_TIME, SpecificationFieldOperation.LTE, FilterBatchJobUseCase::getEndTime);

    private final String field;
    private final SpecificationFieldOperation operation;
    private final Function<FilterBatchJobUseCase, Object> fieldGetFunction;

    public static final List<FilterBatchJobSummaryPredicates> _VALUES = List.of(values());

    /***
     *
     * @param useCase FilterBatchJobUseCase
     * @param root Root<BatchJobSummary>
     * @param criteriaBuilder CriteriaBuilder
     * @param joinMap Map<String, Join<?, ?>>
     * @return Predicate
     */
    public Predicate toPredicate(FilterBatchJobUseCase useCase,
                                 Root<BatchJobSummary> root,
                                 CriteriaBuilder criteriaBuilder,
                                 Map<String, Join<?, ?>> joinMap) {
        return operation.toPredicate(useCase, root.get(field), criteriaBuilder, fieldGetFunction, joinMap);
    }
}
