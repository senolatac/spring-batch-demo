package com.aril.arilbatchsdk.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(BatchJobResultSummary.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class BatchJobResultSummary_ {

	public static final String SUMMARY = "summary";
	public static final String LAST_UPDATED = "lastUpdated";
	public static final String JOB_INSTANCE_ID = "jobInstanceId";
	public static final String FAIL_COUNT = "failCount";
	public static final String CREATE_TIME = "createTime";
	public static final String SUCCESS_COUNT = "successCount";
	public static final String VERSION = "version";

	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobResultSummary#summary
	 **/
	public static volatile SingularAttribute<BatchJobResultSummary, BatchJobSummary> summary;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobResultSummary#lastUpdated
	 **/
	public static volatile SingularAttribute<BatchJobResultSummary, Long> lastUpdated;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobResultSummary#jobInstanceId
	 **/
	public static volatile SingularAttribute<BatchJobResultSummary, Long> jobInstanceId;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobResultSummary#failCount
	 **/
	public static volatile SingularAttribute<BatchJobResultSummary, Long> failCount;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobResultSummary#createTime
	 **/
	public static volatile SingularAttribute<BatchJobResultSummary, Long> createTime;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobResultSummary#successCount
	 **/
	public static volatile SingularAttribute<BatchJobResultSummary, Long> successCount;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobResultSummary
	 **/
	public static volatile EntityType<BatchJobResultSummary> class_;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobResultSummary#version
	 **/
	public static volatile SingularAttribute<BatchJobResultSummary, Long> version;

}

