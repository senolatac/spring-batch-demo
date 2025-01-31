package com.aril.arilbatchsdk.filter.specification;

import com.aril.arilbatchsdk.util.SpecificationUtils;
import com.aril.valhala.hexagonal.UseCase;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.function.Function;

@Getter
@RequiredArgsConstructor
public enum SpecificationFieldOperation {
    EQ {
        @Override
        public <T extends UseCase> Predicate toPredicate(T useCase, Path<?> path, CriteriaBuilder criteriaBuilder, Function<T, Object> fieldGetFunction, Map<String, Join<?, ?>> joinMap) {
            return SpecificationUtils.toEqualPredicate(criteriaBuilder, path, fieldGetFunction.apply(useCase));
        }
    },
    LIKE {
        @Override
        @SuppressWarnings("unchecked")
        public <T extends UseCase> Predicate toPredicate(T useCase, Path<?> path, CriteriaBuilder criteriaBuilder, Function<T, Object> fieldGetFunction, Map<String, Join<?, ?>> joinMap) {
            return SpecificationUtils.toLikePredicate(criteriaBuilder, (Path<String>) path, fieldGetFunction.apply(useCase));
        }
    },
    GTE {
        @Override
        @SuppressWarnings("unchecked")
        public <T extends UseCase> Predicate toPredicate(T useCase, Path<?> path, CriteriaBuilder criteriaBuilder, Function<T, Object> fieldGetFunction, Map<String, Join<?, ?>> joinMap) {
            return SpecificationUtils.toGTEPredicate(criteriaBuilder, (Path<Long>) path, fieldGetFunction.apply(useCase));
        }
    },
    LTE {
        @Override
        @SuppressWarnings("unchecked")
        public <T extends UseCase> Predicate toPredicate(T useCase, Path<?> path, CriteriaBuilder criteriaBuilder, Function<T, Object> fieldGetFunction, Map<String, Join<?, ?>> joinMap) {
            return SpecificationUtils.toLTEPredicate(criteriaBuilder, (Path<Long>) path, fieldGetFunction.apply(useCase));
        }
    };

    public abstract <T extends UseCase> Predicate toPredicate(T useCase, Path<?> path, CriteriaBuilder criteriaBuilder, Function<T, Object> fieldGetFunction, Map<String, Join<?, ?>> joinMap);
}
