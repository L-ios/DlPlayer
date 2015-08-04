package com.tpw.homeshell.cardprovider;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tpw.homeshell.R;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.text.TextUtils;

class QueryFactory {

    private QueryFactory() {}
    private static QueryFactory mFactory = new QueryFactory();

    static final int TYPE_MMS = 0;
    static final int TYPE_CALLS = 1;

    static QueryFactory getFactory() {
        return mFactory;
    }

    static IQuery getQuery(int type) {
        switch(type) {
        case TYPE_MMS:
            return MmsQuery.getQuery();
        case TYPE_CALLS:
            return CallsQuery.getQuery();
        }
        return DefaultQuery.getQuery();
    }

    private static class MmsQuery implements IQuery {
        private static final Uri URI_THREADS = Uri.parse("content://mms-sms/conversations?simple=true");
        private static final Uri URI_ADDRESS = Uri.parse("content://mms-sms/canonical-addresses");
        private static final String COL_THREAD_ID = "thread_id";
        private static final String COL_ADDRESS = "address";
        private static final String COL_SNIPPET = "snippet";
        private static final String COL_SNIPPET_CS = "snippet_cs";
        private static final String COL_HAS_ATTACH = "has_attachment";
        private static final String COL_DATE = "date";
        private static final String COL_RECIPIENT_IDS = "recipient_ids";
        private static final String COL_ID_ = "_id";
        private static final String COL_MESSAGE_COUNT = "message_count";
        private static MmsQuery mQuery = new MmsQuery();

        private MmsQuery() {}

        static MmsQuery getQuery() {
            return mQuery;
        }

        @Override
        public Cursor doQuery(Context context, String[] projection, String selection,
                String[] selectionArgs, String sortOrder) {
            String[] cols = new String[]{COL_ADDRESS, COL_SNIPPET, COL_DATE, COL_MESSAGE_COUNT, COL_THREAD_ID};
            MatrixCursor res = new MatrixCursor(cols);
            Cursor c1 = context.getContentResolver().query(
                    URI_THREADS,
                    new String[] {COL_RECIPIENT_IDS ,  COL_SNIPPET,COL_DATE, COL_MESSAGE_COUNT,
                            COL_HAS_ATTACH, COL_SNIPPET_CS, COL_ID_}, COL_MESSAGE_COUNT + ">0", null, sortOrder);
            Cursor c2 = context.getContentResolver().query(
                    URI_ADDRESS,
                    new String[] {COL_ID_, COL_ADDRESS}, null, null, null);
            Map<Long, String> addMap = new HashMap<Long, String>();

            if (c2 != null) {
                while (c2.moveToNext()) {
                    long id = c2.getLong(0);
                    String address = c2.getString(1);
                    addMap.put(id, address);
                }
                c2.close();
            }

            if (c1 != null) {
                String attachmentInfo = context.getString(R.string.card_msg_attarchment);
                while(c1.moveToNext()) {
                    long id = c1.getLong(0);
                    String snippet = c1.getString(1);
                    long date = c1.getLong(2);
                    long msgCount = c1.getLong(3);
                    long hasAttachment = c1.getLong(4);
                    long snippet_cs = c1.getLong(5);
                    long threadId = c1.getLong(6);
                    if (snippet_cs == 106) {
                        if (TextUtils.isEmpty(snippet)) {
                            snippet = attachmentInfo;
                        } else {
                            try {
                                snippet = new String(snippet.getBytes("ISO-8859-1"), "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                    } else if (snippet_cs == 1 || (hasAttachment == 1 && TextUtils.isEmpty(snippet))) {
                        snippet = attachmentInfo;
                    }
                    String add = addMap.get(id);
                    if (add == null || "".equals(add)) {
                        add = "";
                    }
                    res.addRow(new Object[]{add, snippet, date, msgCount, threadId});
                }
                c1.close();
            }
            return res;
        }
    }

    private static class CallsQuery implements IQuery {

        private static final Uri URI_CALL_LOG = Uri.parse("content://call_log/calls");
        private static final String COL_NUMBER = "number";
        private static final String COL_DATE = "date";
        private static final String COL_COUNT = "count";
        private static final String ORDER = "date DESC";
        private static CallsQuery mQuery = new CallsQuery();

        private CallsQuery() {}

        static CallsQuery getQuery() {
            return mQuery;
        }

        @Override
        public Cursor doQuery(Context context, String[] projection,
                String selection, String[] selectionArgs, String sortOrder) {
            Cursor c = context.getContentResolver().query(
                    URI_CALL_LOG,
                    new String[]{COL_NUMBER, COL_DATE}, null, null, ORDER);
            String[] cols = new String[]{COL_NUMBER, COL_DATE, COL_COUNT};
            MatrixCursor res = new MatrixCursor(cols);
            if (c != null) {
                List<String> keys = new ArrayList<String>();
                Map<String, Long> dateMap = new HashMap<String, Long>();
                Map<String, Long> countMap = new HashMap<String, Long>();
                while (c.moveToNext()) {
                    String number = c.getString(0);
                    if (keys.contains(number)) {
                        countMap.put(number, countMap.get(number)+1);
                    } else {
                        keys.add(number);
                        long date = c.getLong(1);
                        dateMap.put(number, date);
                        countMap.put(number, 1l);
                    }
                }

                for (String k : keys) {
                    res.addRow(new Object[]{k, dateMap.get(k), countMap.get(k)});
                }
                c.close();
            }
            return res;
        }
    }

    private static class DefaultQuery implements IQuery {
        private static DefaultQuery mQuery = new DefaultQuery();

        private DefaultQuery() {}

        static DefaultQuery getQuery() {
            return mQuery;
        }
        @Override
        public Cursor doQuery(Context context, String[] projection,
                String selection, String[] selectionArgs, String sortOrder) {
            return null;
        }
    }
}
