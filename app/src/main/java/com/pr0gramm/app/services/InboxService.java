package com.pr0gramm.app.services;

import com.pr0gramm.app.api.pr0gramm.Api;
import com.pr0gramm.app.api.pr0gramm.response.Message;
import com.pr0gramm.app.api.pr0gramm.response.MessageFeed;
import com.pr0gramm.app.api.pr0gramm.response.PrivateMessage;
import com.pr0gramm.app.api.pr0gramm.response.PrivateMessageFeed;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;

/**
 * Service for receiving and sending private messages
 * as well as the combined inbox (comments and private messages)
 */
@Singleton
public class InboxService {
    private final Api api;

    @Inject
    public InboxService(Api api) {
        this.api = api;
    }

    /**
     * Gets the list of unread messages. You can not call this multiple times, because
     * it will mark the messages as read immediately.
     */
    public Observable<List<Message>> getUnreadMessages() {
        return api.inboxUnread().map(MessageFeed::getMessages);
    }

    /**
     * Gets the list of inbox messages
     */
    public Observable<List<Message>> getInbox() {
        return api.inboxAll().map(MessageFeed::getMessages);
    }

    /**
     * Gets the list of private messages.
     */
    public Observable<List<PrivateMessage>> getPrivateMessages() {
        return api.inboxPrivateMessages().map(PrivateMessageFeed::getMessages);
    }
}
