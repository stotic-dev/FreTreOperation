package com.stotic_dev.FreTreOperation.firebase.repository;

import com.stotic_dev.FreTreOperation.firebase.collection.FirestoreCollection;
import com.stotic_dev.FreTreOperation.firebase.collection.RootCollection;
import com.stotic_dev.FreTreOperation.firebase.constant.CollectionType;
import com.stotic_dev.FreTreOperation.firebase.data.ChatMessageData;
import com.stotic_dev.FreTreOperation.firebase.query.AnyQuery;
import com.stotic_dev.FreTreOperation.firebase.query.EqualFieldQuery;
import com.stotic_dev.FreTreOperation.firebase.query.FirestoreQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Repository
public class ChatMessageRepositoryImpl {

    @Autowired FirestoreRepositoryImpl firestoreRepository;

    public List<ChatMessageData> fetchAllChatMessage() throws ExecutionException, InterruptedException, TimeoutException {
        return firestoreRepository.fetchDocuments(getChatMessageCollection())
                .stream()
                .map(data -> new ChatMessageData(data.getData()))
                .toList();
    }

    public void deleteAllChatMessage() throws ExecutionException, InterruptedException, TimeoutException {
        firestoreRepository.deleteDocuments(getChatMessageCollection(), new AnyQuery());
    }

    public void deleteChatMessageByTeamUUID(String teamUUID) throws ExecutionException, InterruptedException, TimeoutException {
        FirestoreQuery deleteChatMessageQuery = new EqualFieldQuery<>("teamUUID", teamUUID);
        firestoreRepository.deleteDocumentsWithTransaction(getChatMessageCollection(), deleteChatMessageQuery);
    }

    public List<String> insertChatMessageData(List<ChatMessageData> chatMessageDataList) throws ExecutionException, InterruptedException {
        List<String> documentIdList = new ArrayList<>();
        for (ChatMessageData chatMessageData : chatMessageDataList) {
            documentIdList.add(firestoreRepository.insertDocument(new RootCollection(CollectionType.CHAT), chatMessageData.getFirestoreData()));
        }
        return documentIdList;
    }

    private FirestoreCollection getChatMessageCollection() {
        return new RootCollection(CollectionType.CHAT);
    }
}
