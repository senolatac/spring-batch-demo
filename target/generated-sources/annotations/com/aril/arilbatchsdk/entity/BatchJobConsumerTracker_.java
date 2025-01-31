package com.aril.arilbatchsdk.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.util.Map;
import org.springframework.batch.core.BatchStatus;

@StaticMetamodel(BatchJobConsumerTracker.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class BatchJobConsumerTracker_ {

	public static final String SUMMARY = "summary";
	public static final String JOB_INSTANCE_ID = "jobInstanceId";
	public static final String PRODUCE_SKIP_COUNT = "produceSkipCount";
	public static final String NOTIFIER_TOPIC = "notifierTopic";
	public static final String READ_COUNT = "readCount";
	public static final String VERSION = "version";
	public static final String CONSUME_PERCENTAGE = "consumePercentage";
	public static final String LAST_UPDATED = "lastUpdated";
	public static final String CREATE_TIME = "createTime";
	public static final String TOPIC = "topic";
	public static final String START_TIME = "startTime";
	public static final String END_TIME = "endTime";
	public static final String PRODUCE_COUNT = "produceCount";
	public static final String PRODUCE_SNAPSHOT = "produceSnapshot";
	public static final String STATUS = "status";

	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobConsumerTracker#summary
	 **/
	public static volatile SingularAttribute<BatchJobConsumerTracker, BatchJobSummary> summary;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobConsumerTracker#jobInstanceId
	 **/
	public static volatile SingularAttribute<BatchJobConsumerTracker, Long> jobInstanceId;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobConsumerTracker#produceSkipCount
	 **/
	public static volatile SingularAttribute<BatchJobConsumerTracker, Long> produceSkipCount;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobConsumerTracker#notifierTopic
	 **/
	public static volatile SingularAttribute<BatchJobConsumerTracker, String> notifierTopic;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobConsumerTracker#readCount
	 **/
	public static volatile SingularAttribute<BatchJobConsumerTracker, Long> readCount;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobConsumerTracker#version
	 **/
	public static volatile SingularAttribute<BatchJobConsumerTracker, Long> version;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobConsumerTracker#consumePercentage
	 **/
	public static volatile SingularAttribute<BatchJobConsumerTracker, Integer> consumePercentage;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobConsumerTracker#lastUpdated
	 **/
	public static volatile SingularAttribute<BatchJobConsumerTracker, Long> lastUpdated;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobConsumerTracker#createTime
	 **/
	public static volatile SingularAttribute<BatchJobConsumerTracker, Long> createTime;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobConsumerTracker#topic
	 **/
	public static volatile SingularAttribute<BatchJobConsumerTracker, String> topic;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobConsumerTracker#startTime
	 **/
	public static volatile SingularAttribute<BatchJobConsumerTracker, Long> startTime;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobConsumerTracker#endTime
	 **/
	public static volatile SingularAttribute<BatchJobConsumerTracker, Long> endTime;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobConsumerTracker#produceCount
	 **/
	public static volatile SingularAttribute<BatchJobConsumerTracker, Long> produceCount;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobConsumerTracker#produceSnapshot
	 **/
	public static volatile SingularAttribute<BatchJobConsumerTracker, Map<Integer,Long>> produceSnapshot;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobConsumerTracker
	 **/
	public static volatile EntityType<BatchJobConsumerTracker> class_;
	
	/**
	 * @see com.aril.arilbatchsdk.entity.BatchJobConsumerTracker#status
	 **/
	public static volatile SingularAttribute<BatchJobConsumerTracker, BatchStatus> status;

}

