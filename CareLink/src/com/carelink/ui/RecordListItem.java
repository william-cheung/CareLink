package com.carelink.ui;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.carelink.model.Record;
import com.carelink.model.Reminder;

public abstract class RecordListItem {
	public static final int LIST_ITEM_TYPE_DATE 	= 0;
	public static final int LIST_ITEM_TYPE_RECORD 	= 1;
	public static final int LIST_ITEM_TYPE_TASK 	= 2;
	private int itemType;
	
	
	public RecordListItem(int itemType) {
		super();
		this.itemType = itemType;
	}
	
	public int getItemType() {
		return itemType;
	}
	
	public abstract Date getDate();
}


class DateRecordListItem extends RecordListItem {	
	private Date date;
	public DateRecordListItem(Date date) {
		super(LIST_ITEM_TYPE_DATE);
		this.date = date;
	}
	
	public String getDateString() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		return simpleDateFormat.format(date);
	}

	@Override
	public Date getDate() {
		return date;
	}
}


class NormalRecordListItem extends RecordListItem {
	Record record;
	
	public NormalRecordListItem(Record record) {
		super(LIST_ITEM_TYPE_RECORD);
		this.record = record;
	}
	
	public String getDateString() {
		// TODO Fix it 
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd hh:mm a");
		return simpleDateFormat.format(record.getDate());
	}
	
	public String getDescription() {
		return record.getDescription();
	}
	
	public Record getRecord() {
		return record;
	}

	@Override
	public Date getDate() {
		// TODO Auto-generated method stub
		return record.getDate();
	}
}

class TaskListItem extends RecordListItem {
	private Reminder reminder;
	
	public TaskListItem(Reminder reminder) {
		super(LIST_ITEM_TYPE_TASK);
		this.reminder = reminder;
	}

	/**
	 * NO USE !!!
	 */
	@Override
	public Date getDate() {
		return null;
	}

	public Reminder getReminder() {
		return reminder;
	}
}
