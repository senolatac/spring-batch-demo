package com.aril.arilbatchsdk.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import org.springframework.batch.core.BatchStatus;

@StaticMetamodel(BatchJobItemResult.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class BatchJobItemResult_ {

	public static final String RESULT_VALUE = "resultValue";
	public static final String IDEMPOTENT_KEY = "idempotentKey";
	public static final String JOB_INSTANCE_ID = "jobInstanceId";
	public static final String RESOURCE_NAME = "resourceName";
	public static final String VERSION = "version";
	public static final String RESULT_NAME = "resultName";
	public static final String CREATE_TIME = "createTime";
	public static final String RESPONSE = "response";
	public static final String ERROR_DETAIL = "errorDetail";
	public static final String ID = "id";
	public static final String END_TIME = "endTime";
	public static final String STATUS = "status";
	public static final String RESOURCE_VALUE = "resourceValue";

	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobItemResult#resultValue
	 **/
	public static volatile SingularAttribute<BatchJobItemResult, String> resultValue;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobItemResult#idempotentKey
	 **/
	public static volatile SingularAttribute<BatchJobItemResult, String> idempotentKey;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobItemResult#jobInstanceId
	 **/
	public static volatile SingularAttribute<BatchJobItemResult, Long> jobInstanceId;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobItemResult#resourceName
	 **/
	public static volatile SingularAttribute<BatchJobItemResult, String> resourceName;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobItemResult#version
	 **/
	public static volatile SingularAttribute<BatchJobItemResult, Long> version;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobItemResult#resultName
	 **/
	public static volatile SingularAttribute<BatchJobItemResult, String> resultName;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobItemResult#createTime
	 **/
	public static volatile SingularAttribute<BatchJobItemResult, Long> createTime;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobItemResult#response
	 **/
	public static volatile SingularAttribute<BatchJobItemResult, String> response;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobItemResult#errorDetail
	 **/
	public static volatile SingularAttribute<BatchJobItemResult, String> errorDetail;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobItemResult#id
	 **/
	public static volatile SingularAttribute<BatchJobItemResult, Long> id;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobItemResult#endTime
	 **/
	public static volatile SingularAttribute<BatchJobItemResult, Long> endTime;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobItemResult
	 **/
	public static volatile EntityType<BatchJobItemResult> class_;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobItemResult#status
	 **/
	public static volatile SingularAttribute<BatchJobItemResult, BatchStatus> status;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobItemResult#resourceValue
	 **/
	public static volatile SingularAttribute<BatchJobItemResult, String> resourceValue;

}

