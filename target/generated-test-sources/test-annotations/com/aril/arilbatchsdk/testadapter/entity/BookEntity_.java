package com.aril.arilbatchsdk.testadapter.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(BookEntity.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class BookEntity_ {

	public static final String YEAR = "year";
	public static final String AUTHOR = "author";
	public static final String ID = "id";
	public static final String TITLE = "title";
	public static final String BOOK_DETAIL = "bookDetail";

	
	/**
	 * @see com.aril.arilbatchsdk.testadapter.entity.BookEntity#year
	 **/
	public static volatile SingularAttribute<BookEntity, Integer> year;
	
	/**
	 * @see com.aril.arilbatchsdk.testadapter.entity.BookEntity#author
	 **/
	public static volatile SingularAttribute<BookEntity, String> author;
	
	/**
	 * @see com.aril.arilbatchsdk.testadapter.entity.BookEntity#id
	 **/
	public static volatile SingularAttribute<BookEntity, Long> id;
	
	/**
	 * @see com.aril.arilbatchsdk.testadapter.entity.BookEntity#title
	 **/
	public static volatile SingularAttribute<BookEntity, String> title;
	
	/**
	 * @see com.aril.arilbatchsdk.testadapter.entity.BookEntity
	 **/
	public static volatile EntityType<BookEntity> class_;
	
	/**
	 * @see com.aril.arilbatchsdk.testadapter.entity.BookEntity#bookDetail
	 **/
	public static volatile SingularAttribute<BookEntity, BookDetailEntity> bookDetail;

}

