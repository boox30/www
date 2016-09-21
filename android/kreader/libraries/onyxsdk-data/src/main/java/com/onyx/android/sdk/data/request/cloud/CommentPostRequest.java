package com.onyx.android.sdk.data.request.cloud;

import com.onyx.android.sdk.data.CloudManager;
import com.onyx.android.sdk.data.model.Comment;
import com.onyx.android.sdk.data.v1.ServiceFactory;

import retrofit2.Response;

/**
 * Created by suicheng on 2016/9/20.
 */
public class CommentPostRequest extends BaseCloudRequest {

    private String bookId;
    private String sessionToken;
    private Comment comment;
    private Comment resultComment;

    public CommentPostRequest(String bookId, Comment comment, String sessionToken) {
        this.bookId = bookId;
        this.sessionToken = sessionToken;
        this.comment = comment;
    }

    public Comment getResultComment() {
        return resultComment;
    }

    @Override
    public void execute(CloudManager parent) throws Exception {
        Response<Comment> response = ServiceFactory.getBookStoreService(parent.getCloudConf().getApiBase())
                .postBookComment(bookId, comment, sessionToken).execute();
        if (response.isSuccessful()) {
            resultComment = response.body();
        }
    }
}
