package com.aril.arilbatchsdk.testadapter.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(BookDetailEntity.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class BookDetailEntity_ {

	public static final String BOOK = "book";
	public static final String ID = "id";
	public static final String DETAIL = "detail";

	
	/**
	 * @see com.aril.arilbatchsdk.testadapter.entity.BookDetailEntity#book
	 **/
	public static volatile SingularAttribute<BookDetailEntity, BookEntity> book;
	
	/**
	 * @see com.aril.arilbatchsdk.testadapter.entity.BookDetailEntity#id
	 **/
	public static volatile SingularAttribute<BookDetailEntity, Long> id;
	
	/**
	 * @see com.aril.arilbatchsdk.testadapter.entity.BookDetailEntity#detail
	 **/
	public static volatile SingularAttribute<BookDetailEntity, String> detail;
	
	/**
	 * @see com.aril.arilbatchsdk.testadapter.entity.BookDetailEntity
	 **/
	public static volatile EntityType<BookDetailEntity> class_;

}

