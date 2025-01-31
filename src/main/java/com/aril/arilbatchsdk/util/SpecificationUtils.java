package com.aril.arilbatchsdk.util;

import jakarta.persistence.criteria.*;
import lombok.experimental.UtilityClass;

@UtilityClass
public class SpecificationUtils {

    public static final String PERCENTAGE_CHARACTER = "%";

    @SuppressWarnings("unchecked")
    public static <X, Y> Join<X, Y> getJoinOrFetch(Root<?> root, CriteriaQuery<?> criteriaQuery, String column, JoinType joinType) {
        return currentQueryIsCountRecords(criteriaQuery) ? root.join(column, joinType) : (Join<X, Y>) root.fetch(column, joinType);
    }

    public boolean currentQueryIsCountRecords(CriteriaQuery<?> criteriaQuery) {
        Class<?> resultType = criteriaQuery.getResultType();
        return resultType == Long.class || resultType == long.class;
    }

    public static Predicate toEqualPredicate(CriteriaBuilder cb, Path<?> path, Object fieldValue) {
        if (fieldValue != null) {
            return cb.equal(path, fieldValue);
        }
        return null;
    }

    public static Predicate toLikePredicate(CriteriaBuilder cb, Path<String> path, Object fieldValue) {
        if (fieldValue != null) {
            return cb.like(cb.lower(path), PERCENTAGE_CHARACTER + String.valueOf(fieldValue).toLowerCase() + PERCENTAGE_CHARACTER);
        }
        return null;
    }

    public static Predicate toGTEPredicate(CriteriaBuilder cb, Path<Long> path, Object fieldValue) {
        if (fieldValue != null) {
            return cb.greaterThanOrEqualTo(path, Long.valueOf(fieldValue.toString()));
        }
        return null;
    }

    public static Predicate toLTEPredicate(CriteriaBuilder cb, Path<Long> path, Object fieldValue) {
        if (fieldValue != null) {
            return cb.lessThanOrEqualTo(path, Long.valueOf(fieldValue.toString()));
        }
        return null;
    }
}

