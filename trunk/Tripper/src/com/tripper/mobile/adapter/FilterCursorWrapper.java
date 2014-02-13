package com.tripper.mobile.adapter;

import com.tripper.mobile.utils.ContactsListSingleton;
import com.tripper.mobile.utils.Queries;

import android.database.Cursor;
import android.database.CursorWrapper;

public class FilterCursorWrapper extends CursorWrapper {
    private int[] index;
    private int count = 0;
    private int pos = 0;
    
    public boolean isHidden(String phoneNum) {

      // the logic to check where this item should be hidden

      if(ContactsListSingleton.getInstance().contains(phoneNum))
    	  return true;
      else
    	  return false;

    }

    public FilterCursorWrapper(Cursor cursor, boolean doFilter, int column) {
        super(cursor);
        if (doFilter) {
            this.count = super.getCount();
            this.index = new int[this.count];
            for (int i = 0; i < this.count; i++) {
                super.moveToPosition(i);
    	        String phoneNum = cursor.getString(Queries.PHONE_NUM);                
                if (!isHidden(phoneNum))
                    this.index[this.pos++] = i;
            }
            this.count = this.pos;
            this.pos = 0;
            super.moveToFirst();
        } else {
            this.count = super.getCount();
            this.index = new int[this.count];
            for (int i = 0; i < this.count; i++) {
                this.index[i] = i;
            }
        }
    }

    @Override
    public boolean move(int offset) {
        return this.moveToPosition(this.pos + offset);
    }

    @Override
    public boolean moveToNext() {
        return this.moveToPosition(this.pos + 1);
    }

    @Override
    public boolean moveToPrevious() {
        return this.moveToPosition(this.pos - 1);
    }

    @Override
    public boolean moveToFirst() {
        return this.moveToPosition(0);
    }

    @Override
    public boolean moveToLast() {
        return this.moveToPosition(this.count - 1);
    }

    @Override
    public boolean moveToPosition(int position) {
        if (position >= this.count || position < 0)
            return false;
        return super.moveToPosition(this.index[position]);
    }

    @Override
    public int getCount() {
        return this.count;
    }

    @Override
    public int getPosition() {
        return this.pos;
    }
}