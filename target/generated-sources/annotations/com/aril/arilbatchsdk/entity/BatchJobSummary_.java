package com.aril.arilbatchsdk.entity;

import com.aril.arilbatchsdk.core.BatchType;
import com.aril.valhala.product.ModuleType;
import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.util.Map;
import org.springframework.batch.core.BatchStatus;

@StaticMetamodel(BatchJobSummary.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class BatchJobSummary_ {

	public static final String JOB_NAME = "jobName";
	public static final String METADATA = "metadata";
	public static final String JOB_INSTANCE_ID = "jobInstanceId";
	public static final String MODULE = "module";
	public static final String JOB_OWNER = "jobOwner";
	public static final String CONSUMER_TRACKER = "consumerTracker";
	public static final String RESULT_SUMMARY = "resultSummary";
	public static final String READ_COUNT = "readCount";
	public static final String VERSION = "version";
	public static final String JOB_LABEL = "jobLabel";
	public static final String LAST_UPDATED = "lastUpdated";
	public static final String CREATE_TIME = "createTime";
	public static final String EXPORT_URL = "exportUrl";
	public static final String WRITE_COUNT = "writeCount";
	public static final String START_TIME = "startTime";
	public static final String END_TIME = "endTime";
	public static final String BATCH_TYPE = "batchType";
	public static final String SKIP_COUNT = "skipCount";
	public static final String STATUS = "status";

	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobSummary#jobName
	 **/
	public static volatile SingularAttribute<BatchJobSummary, String> jobName;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobSummary#metadata
	 **/
	public static volatile SingularAttribute<BatchJobSummary, Map<String,Object>> metadata;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobSummary#jobInstanceId
	 **/
	public static volatile SingularAttribute<BatchJobSummary, Long> jobInstanceId;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobSummary#module
	 **/
	public static volatile SingularAttribute<BatchJobSummary, ModuleType> module;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobSummary#jobOwner
	 **/
	public static volatile SingularAttribute<BatchJobSummary, String> jobOwner;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobSummary#consumerTracker
	 **/
	public static volatile SingularAttribute<BatchJobSummary, BatchJobConsumerTracker> consumerTracker;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobSummary#resultSummary
	 **/
	public static volatile SingularAttribute<BatchJobSummary, BatchJobResultSummary> resultSummary;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobSummary#readCount
	 **/
	public static volatile SingularAttribute<BatchJobSummary, Long> readCount;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobSummary#version
	 **/
	public static volatile SingularAttribute<BatchJobSummary, Long> version;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobSummary#jobLabel
	 **/
	public static volatile SingularAttribute<BatchJobSummary, String> jobLabel;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobSummary#lastUpdated
	 **/
	public static volatile SingularAttribute<BatchJobSummary, Long> lastUpdated;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobSummary#createTime
	 **/
	public static volatile SingularAttribute<BatchJobSummary, Long> createTime;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobSummary#exportUrl
	 **/
	public static volatile SingularAttribute<BatchJobSummary, String> exportUrl;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobSummary#writeCount
	 **/
	public static volatile SingularAttribute<BatchJobSummary, Long> writeCount;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobSummary#startTime
	 **/
	public static volatile SingularAttribute<BatchJobSummary, Long> startTime;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobSummary#endTime
	 **/
	public static volatile SingularAttribute<BatchJobSummary, Long> endTime;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobSummary#batchType
	 **/
	public static volatile SingularAttribute<BatchJobSummary, BatchType> batchType;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobSummary
	 **/
	public static volatile EntityType<BatchJobSummary> class_;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobSummary#skipCount
	 **/
	public static volatile SingularAttribute<BatchJobSummary, Long> skipCount;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobSummary#status
	 **/
	public static volatile SingularAttribute<BatchJobSummary, BatchStatus> status;

}

